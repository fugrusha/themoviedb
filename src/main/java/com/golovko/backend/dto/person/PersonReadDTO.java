package com.golovko.backend.dto.person;

import com.golovko.backend.domain.Gender;
import lombok.Data;

import java.util.UUID;

@Data
public class PersonReadDTO {

    private UUID id;

    private String firstName;

    private String lastName;

    private Gender gender;
}
