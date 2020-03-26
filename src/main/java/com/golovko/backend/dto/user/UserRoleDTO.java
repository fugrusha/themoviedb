package com.golovko.backend.dto.user;

import com.golovko.backend.domain.UserRole;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class UserRoleDTO {

    @NotNull
    private UserRole userRole;
}
