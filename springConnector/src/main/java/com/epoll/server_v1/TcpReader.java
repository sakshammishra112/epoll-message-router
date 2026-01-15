package com.epoll.server_v1;

import com.epoll.server_v1.security.UserService;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;

public class TcpReader implements Runnable {

    private final SocketChannel channel;
    private final WebSocketSession session;
    private final UserService userService;   // 👈 ADD

    public TcpReader(
            SocketChannel channel,
            WebSocketSession session,
            UserService userService
    ) {
        this.channel = channel;
        this.session = session;
        this.userService = userService;
    }


    @Override
    public void run() {
        ByteBuffer buf = ByteBuffer.allocate(8192);
        buf.order(ByteOrder.BIG_ENDIAN);

        try {
            while (!Thread.currentThread().isInterrupted()) {

                int n = channel.read(buf);
                if (n <= 0) continue;

                buf.flip();

                while (buf.remaining() >= 4) {
                    buf.mark();
                    int len = buf.getInt();

                    if (buf.remaining() < len) {
                        buf.reset();
                        break;
                    }

                    short type = buf.getShort();
                    long msgId = buf.getLong();

                    if (type == BinaryProtocol.MSG) {
                        byte[] payload = new byte[len - 10];
                        buf.get(payload);

                        ByteBuffer p = ByteBuffer.wrap(payload);
                        p.order(ByteOrder.BIG_ENDIAN);

                        int to = p.getInt();
                        int from = p.getInt();
                        byte[] msgBytes = new byte[p.remaining()];
                        p.get(msgBytes);

                        String text =
                                new String(msgBytes, StandardCharsets.UTF_8);

                        ObjectMapper mapper = new ObjectMapper();

                        String fromEmail;

                        try {
                            fromEmail = userService.findById((long) from).getEmail();
                        } catch (Exception e) {
                            fromEmail = "unknown@" + from; // fallback safety
                        }

                        IncomingMessage msg =
                                new IncomingMessage(fromEmail, text);


                        String json = mapper.writeValueAsString(msg);

                        synchronized (session) {
                            if (session.isOpen()) {
                                session.sendMessage(new TextMessage(json));
                            }
                        }

                        channel.write(
                                BinaryProtocol.ackFrame(msgId)
                        );
                    } else {
                        buf.position(buf.position() + len - 10);
                    }
                }

                buf.compact();
            }
        } catch (IOException e) {
            // connection closed
        }
    }
}


