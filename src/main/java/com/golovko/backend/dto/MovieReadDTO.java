package com.golovko.backend.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.UUID;

@Data
public class MovieReadDTO {
    private UUID id;

    private String movieTitle;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    private String description;

    private boolean isReleased;

    private Double averageRating;
}
