package com.epoll.server_v1.controller;

import com.epoll.server_v1.security.UserService;
import com.epoll.server_v1.security.Users;
import com.epoll.server_v1.security.dto.UserResponse;
import com.epoll.server_v1.service.FriendService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/friends")
public class FriendController {

    private final FriendService friendService;
    private final UserService userService;

    public FriendController(FriendService friendService,
                            UserService userService) {
        this.friendService = friendService;
        this.userService = userService;
    }

    @GetMapping
    public List<UserResponse> myFriends(Authentication auth) {

        Users me = userService.findByEmail(auth.getName());

        return friendService.getFriendIds(me.getId())
                .stream()
                .map(id -> userService.findById(id))
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getEmail()
                ))
                .toList();
    }

    @GetMapping("/api/friends")
    public List<UserResponse> friends(Authentication auth) {

        Users me = userService.findByEmail(auth.getName());

        return friendService.getFriendIds(me.getId())
                .stream()
                .map(userService::findById)   // 👈 friendId → Users
                .map(u -> new UserResponse(
                        u.getId(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getEmail()
                ))
                .toList();
    }

}
