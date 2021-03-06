package com.golovko.backend.dto.movie;

import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class MovieReadDTO {

    private UUID id;

    private String movieTitle;

    private LocalDate releaseDate;

    private String description;

    private Boolean isReleased;

    private String posterUrl;

    private String trailerUrl;

    private Integer runtime;

    private Integer revenue;

    private Double averageRating;

    private Double predictedAverageRating;

    private Integer likesCount;

    private Integer dislikesCount;

    private Instant createdAt;

    private Instant updatedAt;
}
