package com.golovko.backend.dto.moviecast;

import com.golovko.backend.domain.MovieCrewType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class MovieCastReadDTO {

    private UUID id;

    private String description;

    private String character;

    private MovieCrewType movieCrewType;

    private Double averageRating;

    private UUID movieId;

    private UUID personId;

    private Instant createdAt;

    private Instant updatedAt;
}
