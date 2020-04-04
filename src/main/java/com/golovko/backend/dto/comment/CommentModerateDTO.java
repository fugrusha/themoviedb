package com.golovko.backend.dto.comment;

import com.golovko.backend.domain.CommentStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
public class CommentModerateDTO {

    @NotNull
    private CommentStatus newStatus;

    @Size(min = 1, max = 500)
    private String newMessage;
}
