package com.golovko.backend.dto.movieParticipation;

import com.golovko.backend.domain.PartType;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class MoviePartReadExtendedDTO {

    private UUID id;

    private String partInfo;

    private Set<PartType> partTypes;

    private Double averageRating;

    private MovieReadDTO movie;

    private PersonReadDTO person;
}
