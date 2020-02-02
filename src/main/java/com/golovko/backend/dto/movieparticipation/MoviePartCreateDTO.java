package com.golovko.backend.dto.movieparticipation;

import com.golovko.backend.domain.PartType;
import lombok.Data;

@Data
public class MoviePartCreateDTO {

    private String partInfo;

    private PartType partType;
}
