package com.golovko.backend.dto.article;

import com.golovko.backend.domain.ArticleStatus;
import lombok.Data;

@Data
public class ArticlePutDTO {

    private String title;

    private String text;

    private ArticleStatus status;
}
