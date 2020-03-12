package com.golovko.backend.dto.user;

import com.golovko.backend.domain.UserRole;
import lombok.Data;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Data
public class UserReadDTO {

    private UUID id;

    private String username;

    private String email;

    private Boolean isBlocked;

    private Set<UserRole> userRole;

    private Instant createdAt;

    private Instant updatedAt;
}
