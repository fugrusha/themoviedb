package com.golovko.backend.dto.moviecrew;

import com.golovko.backend.domain.MovieCrewType;
import lombok.Data;

import java.util.UUID;

@Data
public class MovieCrewPutDTO {

    private String description;

    private MovieCrewType movieCrewType;

    private UUID personId;
}
