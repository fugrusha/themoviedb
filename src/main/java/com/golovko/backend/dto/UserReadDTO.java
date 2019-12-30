package com.golovko.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserReadDTO {

    private UUID id;
    private String username;
    private String password;
    private String email;
}
