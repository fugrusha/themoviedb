package com.golovko.backend.dto.movie;

import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class MovieCreateDTO {

    @NotNull
    @Size(min = 1, max = 128)
    private String movieTitle;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    @Size(min = 1, max = 1000)
    private String description;

    @NotNull
    private Boolean isReleased;

    @Size(min = 1, max = 100)
    private String posterUrl;

    @Size(min = 1, max = 100)
    private String trailerUrl;

    @PositiveOrZero
    private Integer runtime;

    @PositiveOrZero
    private Integer revenue;
}
