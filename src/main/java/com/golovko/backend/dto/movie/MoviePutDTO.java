package com.golovko.backend.dto.movie;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MoviePutDTO {

    private String movieTitle;

    @JsonFormat(pattern = "yyyy-MM-dd", shape = JsonFormat.Shape.STRING)
    private LocalDate releaseDate;

    private String description;

    private Boolean isReleased;

    private Double averageRating;
}
