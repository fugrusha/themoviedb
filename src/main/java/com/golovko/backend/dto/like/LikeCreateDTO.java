package com.golovko.backend.dto.like;

import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.util.UUID;

@Data
public class LikeCreateDTO {

    private Boolean meLiked;

    private TargetObjectType likedObjectType;

    private UUID likedObjectId;
}
