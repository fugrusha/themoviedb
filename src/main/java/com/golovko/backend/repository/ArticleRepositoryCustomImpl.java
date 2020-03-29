package com.golovko.backend.repository;

import com.golovko.backend.domain.Article;
import com.golovko.backend.dto.article.ArticleManagerFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class ArticleRepositoryCustomImpl implements ArticleRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RepositoryHelper repoHelper;

    @Override
    public Page<Article> findByManagerFilter(ArticleManagerFilter filter, Pageable pageable) {
        StringBuilder sb = new StringBuilder();
        sb.append("select a from Article a where 1=1");

        Query query = createQueryApplyingFilter(filter, pageable.getSort(), sb);
        repoHelper.applyPaging(query, pageable);

        List<Article> data = query.getResultList();

        long count = getCountOfArticles(filter);
        return new PageImpl<>(data, pageable, count);
    }

    private Query createQueryApplyingFilter(ArticleManagerFilter filter, Sort sort, StringBuilder sb) {
        if (filter.getAuthorId() != null) {
            sb.append(" and a.author.id = :authorId");
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            sb.append(" and a.status in :statuses");
        }

        if (sort != null && sort.isSorted()) {
            sb.append(" order by");
            for (Sort.Order order : sort.toList()) {
                sb.append(" a.").append(order.getProperty()).append(" ").append(order.getDirection());
            }
        }

        Query query = entityManager.createQuery(sb.toString());

        if (filter.getAuthorId() != null) {
            query.setParameter("authorId", filter.getAuthorId());
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            query.setParameter("statuses", filter.getStatuses());
        }

        return query;
    }

    private long getCountOfArticles(ArticleManagerFilter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(a) from Article a where 1=1");
        Query query = createQueryApplyingFilter(filter, null, sb);
        return ((Number) query.getResultList().get(0)).longValue();
    }
}
