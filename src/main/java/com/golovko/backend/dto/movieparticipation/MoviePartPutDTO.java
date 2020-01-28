package com.golovko.backend.dto.movieparticipation;

import com.golovko.backend.domain.PartType;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class MoviePartPutDTO {

    private String partInfo;

    private Set<PartType> partTypes;

    private UUID movieId;

    private UUID personId;
}
