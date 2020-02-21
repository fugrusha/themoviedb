package com.golovko.backend.dto.movieparticipation;

import com.golovko.backend.domain.PartType;
import lombok.Data;

import java.util.UUID;

@Data
public class MoviePartCreateDTO {

    private UUID personId;

    private String partInfo;

    private PartType partType;
}
