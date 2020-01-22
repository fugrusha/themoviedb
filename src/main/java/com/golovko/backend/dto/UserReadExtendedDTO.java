package com.golovko.backend.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class UserReadExtendedDTO {

    private UUID id;

    private String username;

    private String email;

    private ComplaintReadDTO complaint;
}
