package com.golovko.backend.dto.movie;

import lombok.Data;

import java.time.LocalDate;

@Data
public class MovieCreateDTO {

    private String movieTitle;

    private LocalDate releaseDate;

    private String description;

    private Boolean isReleased;
}
