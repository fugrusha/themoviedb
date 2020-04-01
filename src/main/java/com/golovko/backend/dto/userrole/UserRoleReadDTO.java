package com.golovko.backend.dto.userrole;

import com.golovko.backend.domain.UserRoleType;
import lombok.Data;

import java.util.UUID;

@Data
public class UserRoleReadDTO {

    private UUID id;

    private UserRoleType type;
}
