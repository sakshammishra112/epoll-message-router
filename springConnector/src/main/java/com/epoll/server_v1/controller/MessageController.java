package com.epoll.server_v1.controller;

import com.epoll.server_v1.security.UserService;
import com.epoll.server_v1.security.Users;
import com.epoll.server_v1.service.FriendService;
import com.epoll.server_v1.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserService userService;

    @Autowired
    private FriendService friendService;

    public MessageController(MessageService messageService,
                             UserService userService) {
        this.messageService = messageService;
        this.userService = userService;
    }

    @GetMapping("/{email}")
    public List<MessageDto> getConversation(
            @PathVariable String email,
            Authentication auth
    ) {
        Users me = userService.findByEmail(auth.getName());
        Users other = userService.findByEmail(email);

        // 🔥 mark as read
        messageService.markConversationAsRead(me.getId(), other.getId());

        return messageService
                .getConversation(me.getId(), other.getId())
                .stream()
                .map(m -> new MessageDto(
                        m.getSenderId().equals(me.getId())
                                ? "You"
                                : other.getEmail(),
                        m.getContent()
                ))
                .toList();
    }

    @GetMapping("/unread")
    public Map<String, Long> unreadCounts(Authentication auth) {

        Users me = userService.findByEmail(auth.getName());

        Map<String, Long> result = new HashMap<>();

        for (Long friendId : friendService.getFriendIds(me.getId())) {
            long count = messageService.getUnreadCount(me.getId(), friendId);
            if (count > 0) {
                Users friend = userService.findById(friendId);
                result.put(friend.getEmail(), count);
            }
        }
        return result;
    }


    public record MessageDto(String from, String text) {}

}
