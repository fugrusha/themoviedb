package com.golovko.backend.dto.user;

import com.golovko.backend.domain.Gender;
import lombok.Data;

import javax.validation.constraints.Email;

@Data
public class UserPatchDTO {

    private String username;

    @Email
    private String email;

    private Gender gender;
}
