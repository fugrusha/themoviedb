package com.golovko.backend.dto.rating;

import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class RatingReadDTO {

    private UUID id;

    private Integer rating;

    private Instant createdAt;

    private Instant updatedAt;

    private UUID authorId;

    private TargetObjectType targetObjectType;

    private UUID targetObjectId;
}
