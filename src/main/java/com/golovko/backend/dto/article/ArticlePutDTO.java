package com.golovko.backend.dto.article;

import com.golovko.backend.domain.ArticleStatus;
import lombok.Data;

import javax.validation.constraints.Size;

@Data
public class ArticlePutDTO {

    @Size(min = 1, max = 300)
    private String title;

    @Size(min = 1, max = 10000)
    private String text;

    private ArticleStatus status;
}
