package com.golovko.backend.dto.comment;

import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class CommentPutDTO {

    @Size(min = 1, max = 500)
    private String message;

    @Size(min = 1, max = 500)
    private String spoiler;
}
