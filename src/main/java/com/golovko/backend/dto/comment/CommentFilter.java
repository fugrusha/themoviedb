package com.golovko.backend.dto.comment;

import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class CommentFilter {

    private UUID authorId;

    private Set<TargetObjectType> types;

    private Set<CommentStatus> statuses;
}
