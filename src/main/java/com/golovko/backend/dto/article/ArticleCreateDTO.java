package com.golovko.backend.dto.article;

import com.golovko.backend.domain.ArticleStatus;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.UUID;

@Data
public class ArticleCreateDTO {

    @NotNull
    @Size(min = 1, max = 300)
    private String title;

    @NotNull
    @Size(min = 1, max = 10000)
    private String text;

    @NotNull
    private ArticleStatus status;

    @NotNull
    private UUID authorId;

}
