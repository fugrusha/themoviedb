package com.golovko.backend.dto.movie;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MoviePutDTO {

    private String movieTitle;

    private LocalDate releaseDate;

    private String description;

    private Boolean isReleased;

    private Double averageRating;
}
