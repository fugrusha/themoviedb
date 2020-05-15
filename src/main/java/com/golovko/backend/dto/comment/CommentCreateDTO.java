package com.golovko.backend.dto.comment;

import com.golovko.backend.domain.TargetObjectType;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class CommentCreateDTO {

    @NotNull
    @Size(min = 1, max = 500)
    private String message;

    @NotNull
    private UUID authorId;

    @NotNull
    private TargetObjectType targetObjectType;
}
