package com.golovko.backend.repository;

import com.golovko.backend.domain.Article;
import com.golovko.backend.dto.article.ArticleManagerFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ArticleRepositoryCustom {

    Page<Article> findByManagerFilter(ArticleManagerFilter filter, Pageable pageable);
}
