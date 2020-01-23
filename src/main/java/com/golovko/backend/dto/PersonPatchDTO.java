package com.golovko.backend.dto;

import com.golovko.backend.domain.Gender;
import lombok.Data;

@Data
public class PersonPatchDTO {

    private String firstName;

    private String lastName;

    private Gender gender;
}
