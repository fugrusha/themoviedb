package com.golovko.backend.dto.person;

import com.golovko.backend.domain.Gender;
import lombok.Data;

@Data
public class PersonCreateDTO {

    private String firstName;

    private String lastName;

    private String bio;

    private Gender gender;
}
