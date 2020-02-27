package com.golovko.backend.dto.moviecrew;

import com.golovko.backend.domain.MovieCrewType;
import lombok.Data;

import java.util.UUID;

@Data
public class MovieCrewCreateDTO {

    private UUID personId;

    private String description;

    private MovieCrewType movieCrewType;
}
