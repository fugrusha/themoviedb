package com.golovko.backend.dto.rating;

import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.util.UUID;

@Data
public class RatingCreateDTO {

    private Integer rating;

    private UUID authorId;

    private TargetObjectType ratedObjectType;
}
