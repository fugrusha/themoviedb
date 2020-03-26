package com.golovko.backend.dto.person;

import com.golovko.backend.domain.Gender;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class PersonPutDTO {

    @Size(min = 1, max = 128)
    private String firstName;

    @Size(min = 1, max = 128)
    private String lastName;

    @Size(min = 1, max = 1000)
    private String bio;

    private Gender gender;
}
