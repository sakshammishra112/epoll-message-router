package com.epoll.server_v1.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "friends")
public class Friend {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private Long friendId;

    @Enumerated(EnumType.STRING)
    private Status status;

    public Friend(Long userId, Long friendId, Status status) {
        this.userId = userId;
        this.friendId = friendId;
        this.status = status;
    }

    public enum Status {
        PENDING,
        ACCEPTED,
        BLOCKED
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getFriendId() {
        return friendId;
    }

    public void setFriendId(Long friendId) {
        this.friendId = friendId;
    }

    public Friend() {}
}

