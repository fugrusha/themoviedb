package com.golovko.backend.dto.comment;

import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentCreateDTO {

    private String message;

    private UUID authorId;

    private TargetObjectType targetObjectType;
}
