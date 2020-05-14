package com.golovko.backend.security;

import com.golovko.backend.domain.ApplicationUser;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.UUID;
import java.util.stream.Collectors;

@Setter
@Getter
public class UserDetailsImpl extends User {

    private UUID id;

    public UserDetailsImpl(ApplicationUser user) {
        super(user.getEmail(), user.getEncodedPassword(),
                true,
                true,
                true,
                !user.getIsBlocked(),
                user.getUserRoles().stream()
                        .map(role -> new SimpleGrantedAuthority(role.getType().toString()))
                        .collect(Collectors.toList()));

        this.id = user.getId();
    }
}
