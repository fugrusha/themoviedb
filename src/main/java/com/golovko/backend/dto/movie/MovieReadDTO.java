package com.golovko.backend.dto.movie;

import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MovieReadDTO {
    private UUID id;

    private String movieTitle;

    private LocalDate releaseDate;

    private String description;

    private boolean isReleased;

    private Double averageRating;
}
