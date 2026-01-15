package com.epoll.server_v1.repository;

import com.epoll.server_v1.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Existing conversation query
    @Query("""
        SELECT m FROM Message m
        WHERE
          (m.senderId = :u1 AND m.receiverId = :u2)
          OR
          (m.senderId = :u2 AND m.receiverId = :u1)
        ORDER BY m.createdAt
    """)
    List<Message> findConversation(Long u1, Long u2);

    // 🔥 Unread count per sender
    @Query("""
        SELECT COUNT(m)
        FROM Message m
        WHERE m.receiverId = :me
          AND m.senderId = :friend
          AND m.isRead = false
    """)
    long countUnread(Long me, Long friend);

    // 🔥 Mark messages as read
    @Modifying
    @Query("""
        UPDATE Message m
        SET m.isRead = true
        WHERE m.receiverId = :me
          AND m.senderId = :friend
          AND m.isRead = false
    """)
    void markAsRead(Long me, Long friend);
}


