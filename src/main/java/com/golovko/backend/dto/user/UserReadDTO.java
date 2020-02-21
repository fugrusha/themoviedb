package com.golovko.backend.dto.user;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserReadDTO {

    private UUID id;

    private String username;

    private String email;

    private Boolean isBlocked;

    private Instant createdAt;

    private Instant updatedAt;
}
