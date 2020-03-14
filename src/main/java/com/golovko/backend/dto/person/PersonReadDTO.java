package com.golovko.backend.dto.person;

import com.golovko.backend.domain.Gender;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class PersonReadDTO {

    private UUID id;

    private String firstName;

    private String lastName;

    private String bio;

    private Gender gender;

    private Instant createdAt;

    private Instant updatedAt;
}
