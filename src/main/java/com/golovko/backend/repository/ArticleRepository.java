package com.golovko.backend.repository;

import com.golovko.backend.domain.Article;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArticleRepository extends CrudRepository<Article, UUID> {
}
