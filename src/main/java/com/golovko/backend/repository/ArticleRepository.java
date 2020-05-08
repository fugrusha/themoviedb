package com.golovko.backend.repository;

import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ArticleRepository extends CrudRepository<Article, UUID>, ArticleRepositoryCustom {

    Page<Article> findByStatus(ArticleStatus status, Pageable pageable);

    @Modifying
    @Query("update Article a set a.likesCount=(coalesce(a.likesCount, 0) + 1)"
            + " where a.id = :articleId")
    void incrementLikesCountField(UUID articleId);

    @Modifying
    @Query("update Article a set a.likesCount=(coalesce(a.likesCount, 0) - 1)"
            + " where a.id = :articleId")
    void decrementLikesCountField(UUID articleId);

    @Modifying
    @Query("update Article a set a.dislikesCount=(coalesce(a.dislikesCount, 0) + 1)"
            + " where a.id = :articleId")
    void incrementDislikesCountField(UUID articleId);

    @Modifying
    @Query("update Article a set a.dislikesCount=(coalesce(a.dislikesCount, 0) - 1)"
            + " where a.id = :articleId")
    void decrementDislikesCountField(UUID articleId);
}
