package com.golovko.backend.dto.movieParticipation;

import com.golovko.backend.domain.PartType;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class MoviePartPatchDTO {

    private String partInfo;

    private Set<PartType> partTypes;

    private Double averageRating;

    private UUID movieId;

    private UUID personId;
}
