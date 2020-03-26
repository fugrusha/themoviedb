package com.golovko.backend.dto.moviecrew;

import com.golovko.backend.domain.MovieCrewType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class MovieCrewCreateDTO {

    @NotNull
    @Size(min = 1, max = 1000)
    private String description;

    @NotNull
    private MovieCrewType movieCrewType;

    private UUID personId;
}
