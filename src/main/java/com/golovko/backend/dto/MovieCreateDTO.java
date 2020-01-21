package com.golovko.backend.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieCreateDTO {
    private String movieTitle;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate releaseDate;

    private String description;

    private boolean isReleased;

    private Double averageRating;
}
