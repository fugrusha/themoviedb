package com.golovko.backend.security;

import com.golovko.backend.domain.ApplicationUser;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.stream.Collectors;

public class UserDetailsImpl extends User {

    public UserDetailsImpl(ApplicationUser user) {
        super(user.getEmail(), user.getEncodedPassword(),
                user.getUserRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getType().toString()))
                        .collect(Collectors.toList()));
    }

}
