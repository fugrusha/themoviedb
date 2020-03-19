package com.golovko.backend.dto.article;

import com.golovko.backend.domain.ArticleStatus;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class ArticleManagerFilter {

    private UUID authorId;

    private Set<ArticleStatus> statuses;
}
