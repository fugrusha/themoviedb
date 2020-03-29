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

    private Double averageRating;

    private Integer likesCount;

    private Integer dislikesCount;

    private Instant createdAt;

    private Instant updatedAt;
}
