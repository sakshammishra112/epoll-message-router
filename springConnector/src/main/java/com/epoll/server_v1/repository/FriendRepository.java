package com.epoll.server_v1.repository;

import com.epoll.server_v1.entity.Friend;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    Optional<Friend> findByUserIdAndFriendId(Long userId, Long friendId);

    @Query("""
        SELECT f.friendId
        FROM Friend f
        WHERE f.userId = :userId
          AND f.status = 'ACCEPTED'
    """)
    List<Long> findFriendIds(@Param("userId") long userId);

    boolean existsByUserIdAndFriendIdAndStatus(
            Long userId,
            Long friendId,
            Friend.Status status
    );
}

