package com.golovko.backend.repository;

import com.golovko.backend.domain.Comment;
import com.golovko.backend.dto.comment.CommentFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RepositoryHelper repoHelper;

    @Override
    public Page<Comment> findByFilter(CommentFilter filter, Pageable pageable) {
        StringBuilder sb = new StringBuilder();
        sb.append("select c from Comment c where 1=1");

        Query query = createQueryApplyingFilter(filter, pageable.getSort(), sb);
        repoHelper.applyPaging(query, pageable);

        List<Comment> data = query.getResultList();

        long count = getCountOfComments(filter);
        return new PageImpl<>(data, pageable, count);
    }

    private Query createQueryApplyingFilter(CommentFilter filter, Sort sort, StringBuilder sb) {
        if (filter.getAuthorId() != null) {
            sb.append(" and c.author.id = :authorId");
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            sb.append(" and c.status in :statuses");
        }
        if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
            sb.append(" and c.targetObjectType in :types");
        }

        if (sort != null && sort.isSorted()) {
            sb.append(" order by");
            for (Sort.Order order : sort.toList()) {
                sb.append(" c.").append(order.getProperty()).append(" ").append(order.getDirection());
            }
        }

        Query query = entityManager.createQuery(sb.toString());

        if (filter.getAuthorId() != null) {
            query.setParameter("authorId", filter.getAuthorId());
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            query.setParameter("statuses", filter.getStatuses());
        }
        if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
            query.setParameter("types", filter.getTypes());
        }

        return query;
    }

    private long getCountOfComments(CommentFilter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(c) from Comment c where 1=1");
        Query query = createQueryApplyingFilter(filter, null, sb);
        return ((Number) query.getResultList().get(0)).longValue();
    }
}
