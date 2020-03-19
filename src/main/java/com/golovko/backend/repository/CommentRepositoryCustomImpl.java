package com.golovko.backend.repository;

import com.golovko.backend.domain.Comment;
import com.golovko.backend.dto.comment.CommentFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Comment> findByFilter(CommentFilter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("select c from Comment c where 1=1");

        if (filter.getAuthorId() != null) {
            sb.append(" and c.author.id = :authorId");
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            sb.append(" and c.status in :statuses");
        }
        if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
            sb.append(" and c.targetObjectType in :types");
        }

        sb.append(" order by c.createdAt asc");

        TypedQuery<Comment> query = entityManager.createQuery(sb.toString(), Comment.class);

        if (filter.getAuthorId() != null) {
            query.setParameter("authorId", filter.getAuthorId());
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            query.setParameter("statuses", filter.getStatuses());
        }
        if (filter.getTypes() != null && !filter.getTypes().isEmpty()) {
            query.setParameter("types", filter.getTypes());
        }

        return query.getResultList();
    }
}
