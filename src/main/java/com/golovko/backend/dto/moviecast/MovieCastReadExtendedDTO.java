package com.golovko.backend.dto.moviecast;

import com.golovko.backend.domain.PartType;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import lombok.Data;

import java.util.UUID;

@Data
public class MovieCastReadExtendedDTO {

    private UUID id;

    private String partInfo;

    private PartType partType;

    private Double averageRating;

    private MovieReadDTO movie;

    private PersonReadDTO person;
}
