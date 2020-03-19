package com.golovko.backend.repository;

import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleRepository extends CrudRepository<Article, UUID>, ArticleRepositoryCustom {

    List<Article> findByStatusOrderByCreatedAtDesc(ArticleStatus status);
}
