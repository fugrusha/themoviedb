package com.golovko.backend.repository;

import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ArticleRepository extends CrudRepository<Article, UUID>, ArticleRepositoryCustom {

    List<Article> findByStatusOrderByCreatedAtDesc(ArticleStatus status);

    @Modifying
    @Query("update Article a set a.likesCount=(a.likesCount + 1)"
            + " where a.id = :articleId")
    void incrementLikesCountField(UUID articleId);

    @Modifying
    @Query("update Article a set a.likesCount=(a.likesCount - 1)"
            + " where a.id = :articleId")
    void decrementLikesCountField(UUID articleId);

    @Modifying
    @Query("update Article a set a.dislikesCount=(a.dislikesCount + 1)"
            + " where a.id = :articleId")
    void incrementDislikesCountField(UUID articleId);

    @Modifying
    @Query("update Article a set a.dislikesCount=(a.dislikesCount - 1)"
            + " where a.id = :articleId")
    void decrementDislikesCountField(UUID articleId);
}
