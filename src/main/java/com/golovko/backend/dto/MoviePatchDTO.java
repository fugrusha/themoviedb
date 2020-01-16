package com.golovko.backend.dto;

import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
public class MoviePatchDTO {

    private String movieTitle;

    @DateTimeFormat(pattern = "dd-MM-yyyy")
    private Date releaseDate;

    private String description;

    private boolean isReleased;

    private double averageRating;
}
