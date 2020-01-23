package com.golovko.backend.dto;

import com.golovko.backend.domain.Gender;
import lombok.Data;

@Data
public class PersonCreateDTO {

    private String firstName;

    private String lastName;

    private Gender gender;
}
