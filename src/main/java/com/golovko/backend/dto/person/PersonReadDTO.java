package com.golovko.backend.dto.person;

import com.golovko.backend.domain.Gender;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class PersonReadDTO {

    private UUID id;

    private String firstName;

    private String lastName;

    private String bio;

    private LocalDate birthday;

    private String placeOfBirth;

    private String imageUrl;

    private Gender gender;

    private Double averageRatingByRoles;

    private Double averageRatingByMovies;

    private Instant createdAt;

    private Instant updatedAt;
}
