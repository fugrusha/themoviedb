package com.golovko.backend.dto.user;

import com.golovko.backend.domain.Gender;
import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.Pattern;

@Data
public class UserPatchDTO {

    private String username;

    @Pattern(regexp = "^(?=\\S+$).{8,}$", message = "Password should contain at least 8 characters without spaces")
    private String password;

    @Pattern(regexp = "^(?=\\S+$).{8,}$", message = "Password should contain at least 8 characters without spaces")
    private String passwordConfirmation;

    @Email
    private String email;

    private Gender gender;
}
