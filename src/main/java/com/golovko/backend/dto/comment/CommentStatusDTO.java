package com.golovko.backend.dto.comment;

import com.golovko.backend.domain.CommentStatus;
import lombok.Data;

@Data
public class CommentStatusDTO {

    private CommentStatus status;
}
