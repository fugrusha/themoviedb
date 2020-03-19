package com.golovko.backend.dto.user;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserReadExtendedDTO {

    private UUID id;

    private String username;

    private String email;

    private Boolean isBlocked;

    private Double trustLevel;

    private Instant createdAt;

    private Instant updatedAt;

    // TODO add liked movies
    // TODO rated movies
}
