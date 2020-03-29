package com.golovko.backend.dto.moviecrew;

import com.golovko.backend.domain.MovieCrewType;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class MovieCrewPatchDTO {

    @Size(min = 1, max = 1000)
    private String description;

    private MovieCrewType movieCrewType;

    private UUID personId;
}
