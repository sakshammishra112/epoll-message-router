package com.epoll.server_v1.service;

import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PresenceService {

    private final Set<Long> onlineUsers =
            ConcurrentHashMap.newKeySet();

    public void userOnline(long userId) {
        onlineUsers.add(userId);
    }

    public void userOffline(long userId) {
        onlineUsers.remove(userId);
    }

    public boolean isOnline(long userId) {
        return onlineUsers.contains(userId);
    }

    public Set<Long> getOnlineUsers() {
        return Set.copyOf(onlineUsers);
    }
}

