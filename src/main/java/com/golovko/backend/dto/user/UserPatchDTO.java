package com.golovko.backend.dto.user;

import lombok.Data;

@Data
public class UserPatchDTO {

    private String username;

    private String password;

    private String email;
}
