package com.golovko.backend.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Data
public class MoviePatchDTO {

    private String movieTitle;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;

    private String description;

    private Boolean isReleased;

    private Double averageRating;
}
