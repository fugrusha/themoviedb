package com.golovko.backend.dto.user;

import com.golovko.backend.domain.Gender;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

@Data
public class UserPutDTO {

    @NotNull
    private String username;

    @NotNull
    @Email
    private String email;

    private Gender gender;
}
