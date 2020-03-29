package com.golovko.backend.dto.like;

import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
public class LikeCreateDTO {

    @NotNull
    private Boolean meLiked;

    @NotNull
    private TargetObjectType likedObjectType;

    @NotNull
    private UUID likedObjectId;
}
