package com.golovko.backend.dto.user;

import com.golovko.backend.dto.userrole.UserRoleReadDTO;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class UserReadDTO {

    private UUID id;

    private String username;

    private String email;

    private Boolean isBlocked;

    private Double trustLevel;

    private List<UserRoleReadDTO> userRoles;

    private Instant createdAt;

    private Instant updatedAt;
}
