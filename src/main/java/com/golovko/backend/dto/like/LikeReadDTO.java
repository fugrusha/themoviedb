package com.golovko.backend.dto.like;

import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class LikeReadDTO {

    private UUID id;

    private Boolean meLiked;

    private Instant createdAt;

    private Instant updatedAt;

    private UUID authorId;

    private TargetObjectType likedObjectType;

    private UUID likedObjectId;
}
