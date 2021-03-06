package com.golovko.backend.dto.user;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Data
public class ResetPasswordDTO {

    @NotNull
    @Pattern(regexp = "^(?=\\S+$).{8,}$", message = "Password should contain at least 8 characters without spaces")
    private String password;

    @NotNull
    @Pattern(regexp = "^(?=\\S+$).{8,}$", message = "Password should contain at least 8 characters without spaces")
    private String passwordConfirmation;
}
