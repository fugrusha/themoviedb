package com.golovko.backend.dto.user;

import com.golovko.backend.domain.Gender;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class UserReadDTO {

    private UUID id;

    private String username;

    private String email;

    private Gender gender;

    private Boolean isBlocked;

    private Double trustLevel;

    private Instant createdAt;

    private Instant updatedAt;
}
