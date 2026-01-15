package com.epoll.server_v1.service;

import com.epoll.server_v1.entity.Friend;
import com.epoll.server_v1.repository.FriendRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FriendService {

    private final FriendRepository repo;

    public FriendService(FriendRepository repo) {
        this.repo = repo;
    }

    public boolean areFriends(long u1, long u2) {
        return repo.existsByUserIdAndFriendIdAndStatus(
                u1, u2, Friend.Status.ACCEPTED
        );
    }

    @Transactional
    public void createFriendshipIfNotExists(long u1, long u2) {

        // 🚨 HARD GUARD: prevent self-friendship
        if (u1 == u2) {
            return;
        }

        // already friends? do nothing
        if (areFriends(u1, u2)) {
            return;
        }

        // create bidirectional friendship
        repo.save(new Friend(u1, u2, Friend.Status.ACCEPTED));
        repo.save(new Friend(u2, u1, Friend.Status.ACCEPTED));
    }


    public List<Long> getFriendIds(long userId) {
        return repo.findFriendIds(userId);
    }

}



