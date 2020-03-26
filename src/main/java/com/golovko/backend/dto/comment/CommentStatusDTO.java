package com.golovko.backend.dto.comment;

import com.golovko.backend.domain.CommentStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class CommentStatusDTO {

    @NotNull
    private CommentStatus status;
}
