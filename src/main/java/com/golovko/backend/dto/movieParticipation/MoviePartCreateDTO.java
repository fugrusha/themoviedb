package com.golovko.backend.dto.movieParticipation;

import com.golovko.backend.domain.PartType;
import lombok.Data;

import java.util.Set;

@Data
public class MoviePartCreateDTO {

    private String partInfo;

    private Set<PartType> partTypes;
}
