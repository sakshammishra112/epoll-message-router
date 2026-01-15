package com.epoll.server_v1;

import com.epoll.server_v1.security.UserService;
import com.epoll.server_v1.security.Users;
import com.epoll.server_v1.service.FriendService;
import com.epoll.server_v1.service.MessageService;
import com.epoll.server_v1.service.PresenceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Map;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;
    private final PresenceService presenceService;
    private final WebSocketSessionRegistry sessionRegistry;

    @Autowired
    private MessageService messageService;

    @Autowired
    private FriendService friendService;

    public ChatWebSocketHandler(
            UserService userService,
            PresenceService presenceService,
            WebSocketSessionRegistry sessionRegistry
    ) {
        this.userService = userService;
        this.presenceService = presenceService;
        this.sessionRegistry = sessionRegistry;
    }

    /* =========================
       CONNECTION OPEN
    ========================= */
    @Override
    public void afterConnectionEstablished(WebSocketSession session)
            throws Exception {

        sessionRegistry.add(session);

        // 🔐 Extract authenticated email from JWT handshake
        String email = (String) session.getAttributes().get("email");
        if (email == null) {
            session.close();
            return;
        }

        Users user = userService.findByEmail(email);
        long userId = user.getId();

        // store identity
        session.getAttributes().put("userId", userId);
        session.getAttributes().put("email", email);

        // email → id cache
        session.getAttributes().put(
                "emailToIdCache",
                new HashMap<String, Integer>()
        );

        // 🔵 MARK ONLINE
        presenceService.userOnline(userId);
        broadcastPresence(email, true);

        // 🔵 Send presence snapshot to the newly connected user
        for (Long onlineUserId : presenceService.getOnlineUsers()) {

            if (onlineUserId.equals(userId)) continue;

            Users onlineUser = userService.findById(onlineUserId);

            PresenceEvent event =
                    new PresenceEvent(onlineUser.getEmail(), true);

            session.sendMessage(
                    new TextMessage(objectMapper.writeValueAsString(event))
            );
        }

        // TCP connection to C++ engine
        SocketChannel channel = SocketChannel.open();
        channel.connect(new InetSocketAddress("localhost", 9000));
        channel.configureBlocking(true);

        channel.write(BinaryProtocol.loginFrame((int) userId));

        TcpReader reader = new TcpReader(channel, session, userService);
        Thread readerThread = Thread.startVirtualThread(reader);

        session.getAttributes().put(
                "tcp",
                new UserTcpConnection(channel, readerThread)
        );

        System.out.println("WebSocket connected → " + email);
    }

    /* =========================
       MESSAGE HANDLING (UNCHANGED)
    ========================= */
    @Override
    protected void handleTextMessage(
            WebSocketSession session,
            TextMessage message
    ) throws Exception {

        SendRequest req =
                objectMapper.readValue(
                        message.getPayload(),
                        SendRequest.class
                );

        String toEmail = req.toEmail();
        String text = req.text();

        if (toEmail == null || text == null || text.isBlank()) return;

        @SuppressWarnings("unchecked")
        Map<String, Integer> emailToIdCache =
                (Map<String, Integer>) session.getAttributes()
                        .get("emailToIdCache");

        Integer toUserId = emailToIdCache.get(toEmail);
        if (toUserId == null) {
            Users target = userService.findByEmail(toEmail);
            toUserId = target.getId().intValue();
            emailToIdCache.put(toEmail, toUserId);
        }

        Long fromUserId =
                (Long) session.getAttributes().get("userId");

        if (fromUserId.equals(toUserId.longValue())) {
            throw new IllegalArgumentException("Cannot message yourself");
        }

        // auto-add friend
        friendService.createFriendshipIfNotExists(
                fromUserId,
                toUserId.longValue()
        );

        UserTcpConnection tcp =
                (UserTcpConnection) session.getAttributes().get("tcp");

        tcp.channel.write(
                BinaryProtocol.sendFrame(toUserId, text)
        );

        messageService.save(fromUserId, toUserId.longValue(), text);
    }

    /* =========================
       CONNECTION CLOSE
    ========================= */
    @Override
    public void afterConnectionClosed(
            WebSocketSession session,
            CloseStatus status
    ) throws Exception {

        sessionRegistry.remove(session);

        Long userId = (Long) session.getAttributes().get("userId");
        String email = (String) session.getAttributes().get("email");

        if (userId != null) {
            presenceService.userOffline(userId);
            broadcastPresence(email, false);
        }

        UserTcpConnection tcp =
                (UserTcpConnection) session.getAttributes().get("tcp");

        if (tcp != null) {
            tcp.channel.close();
            tcp.readerThread.interrupt();
        }

        System.out.println("WebSocket closed → " + email);
    }

    /* =========================
       PRESENCE BROADCAST
    ========================= */
    private void broadcastPresence(String email, boolean online)
            throws IOException {

        PresenceEvent event = new PresenceEvent(email, online);
        String json = objectMapper.writeValueAsString(event);

        for (WebSocketSession s : sessionRegistry.all()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(json));
            }
        }
    }

    /* DTO */
    record SendRequest(String toEmail, String text) {}
    public record PresenceEvent(String email, boolean online) {}

}





