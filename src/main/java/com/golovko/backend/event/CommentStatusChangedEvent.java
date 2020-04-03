package com.golovko.backend.event;

import com.golovko.backend.domain.CommentStatus;
import lombok.Data;

import java.util.UUID;

@Data
public class CommentStatusChangedEvent {

    private UUID commentId;

    private CommentStatus newStatus;
}
