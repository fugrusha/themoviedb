package com.golovko.backend.repository;

import com.golovko.backend.domain.Article;
import com.golovko.backend.dto.article.ArticleManagerFilter;
import org.bitbucket.brunneng.qb.JpaQueryBuilder;
import org.bitbucket.brunneng.qb.SpringQueryBuilderUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Article> findByManagerFilter(ArticleManagerFilter filter, Pageable pageable) {
        JpaQueryBuilder qb = new JpaQueryBuilder(entityManager);
        qb.append("select a from Article a where 1=1");
        qb.append("and a.author.id = :v", filter.getAuthorId());
        qb.appendIn("and a.status in :v", filter.getStatuses());

        return SpringQueryBuilderUtils.loadPage(qb, pageable, "id");
    }
}
