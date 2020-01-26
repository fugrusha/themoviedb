package com.golovko.backend.dto.person;

import com.golovko.backend.domain.Gender;
import lombok.Data;

@Data
public class PersonUpdateDTO {

    private String firstName;

    private String lastName;

    private Gender gender;
}
