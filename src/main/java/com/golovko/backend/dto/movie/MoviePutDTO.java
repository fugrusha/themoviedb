package com.golovko.backend.dto.movie;

import lombok.Data;

import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class MoviePutDTO {

    @Size(min = 1, max = 128)
    private String movieTitle;

    private LocalDate releaseDate;

    @Size(min = 1, max = 1000)
    private String description;

    private Boolean isReleased;

    @Size(min = 1, max = 100)
    private String posterUrl;

    @Size(min = 1, max = 100)
    private String trailerUrl;
}
