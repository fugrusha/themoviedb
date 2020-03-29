package com.golovko.backend.dto.person;

import com.golovko.backend.domain.Gender;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class PersonCreateDTO {

    @NotNull
    @Size(min = 1, max = 128)
    private String firstName;

    @NotNull
    @Size(min = 1, max = 128)
    private String lastName;

    @NotNull
    @Size(min = 1, max = 1000)
    private String bio;

    @NotNull
    private Gender gender;
}
