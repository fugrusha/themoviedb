package com.golovko.backend.repository;

import com.golovko.backend.domain.Article;
import com.golovko.backend.dto.article.ArticleManagerFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Article> findByManagerFilter(ArticleManagerFilter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("select a from Article a where 1=1");

        if (filter.getAuthorId() != null) {
            sb.append(" and a.author.id = :authorId");
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            sb.append(" and a.status in :statuses");
        }

        TypedQuery<Article> query = entityManager.createQuery(sb.toString(), Article.class);

        if (filter.getAuthorId() != null) {
            query.setParameter("authorId", filter.getAuthorId());
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            query.setParameter("statuses", filter.getStatuses());
        }

        return query.getResultList();
    }
}
