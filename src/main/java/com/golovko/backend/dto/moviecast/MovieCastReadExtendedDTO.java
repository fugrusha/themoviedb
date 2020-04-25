package com.golovko.backend.dto.moviecast;

import com.golovko.backend.domain.Gender;
import com.golovko.backend.domain.MovieCrewType;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class MovieCastReadExtendedDTO {

    private UUID id;

    private String description;

    private MovieCrewType movieCrewType;

    private Gender gender;

    private Integer orderNumber;

    private Double averageRating;

    private MovieReadDTO movie;

    private PersonReadDTO person;

    private Instant createdAt;

    private Instant updatedAt;
}
