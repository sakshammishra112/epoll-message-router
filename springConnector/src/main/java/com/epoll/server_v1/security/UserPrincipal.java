package com.epoll.server_v1.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {

    private final Users user;

    public UserPrincipal(Users user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // For now: single role
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // implement later if needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return user.isAccountNotLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // implement later if needed
    }

    @Override
    public boolean isEnabled() {
        return user.isEnabled();
    }
}
