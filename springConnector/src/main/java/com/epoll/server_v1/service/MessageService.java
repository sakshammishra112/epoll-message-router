package com.epoll.server_v1.service;

import com.epoll.server_v1.entity.Message;
import com.epoll.server_v1.repository.MessageRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class MessageService {

    private final MessageRepository repo;

    public MessageService(MessageRepository repo) {
        this.repo = repo;
    }

    public void save(long from, long to, String text) {
        Message msg = new Message();
        msg.setSenderId(from);
        msg.setReceiverId(to);
        msg.setContent(text);
        msg.setRead(false);
        repo.save(msg);
    }

    public List<Message> getConversation(long u1, long u2) {
        return repo.findConversation(u1, u2);
    }

    public long getUnreadCount(long me, long friend) {
        return repo.countUnread(me, friend);
    }

    public void markConversationAsRead(long me, long friend) {
        repo.markAsRead(me, friend);
    }
}



