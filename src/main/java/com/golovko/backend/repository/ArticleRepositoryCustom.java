package com.golovko.backend.repository;

import com.golovko.backend.domain.Article;
import com.golovko.backend.dto.article.ArticleManagerFilter;

import java.util.List;

public interface ArticleRepositoryCustom {

    List<Article> findByManagerFilter(ArticleManagerFilter filter);
}
