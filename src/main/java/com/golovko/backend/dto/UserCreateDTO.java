package com.golovko.backend.dto;

import lombok.Data;

@Data
public class UserCreateDTO {

    private String username;
    private String password;
    private String email;
}
