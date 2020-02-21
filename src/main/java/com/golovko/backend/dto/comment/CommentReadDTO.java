package com.golovko.backend.dto.comment;

import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.ParentType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class CommentReadDTO {

    private UUID id;

    private String message;

    private CommentStatus status;

    private Integer likesCount;

    private Integer dislikesCount;

    private Instant createdAt;

    private Instant updatedAt;

    private UUID authorId;

    private ParentType parentType;

    private UUID parentId;
}
