package com.golovko.backend.repository;

import com.golovko.backend.domain.Comment;
import com.golovko.backend.dto.comment.CommentFilter;
import org.bitbucket.brunneng.qb.JpaQueryBuilder;
import org.bitbucket.brunneng.qb.SpringQueryBuilderUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class CommentRepositoryCustomImpl implements CommentRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Comment> findByFilter(CommentFilter filter, Pageable pageable) {
        JpaQueryBuilder qb = new JpaQueryBuilder(entityManager);
        qb.append("select c from Comment c where 1=1");
        qb.append("and c.author.id = :v", filter.getAuthorId());
        qb.appendIn("and c.status in :v", filter.getStatuses());
        qb.appendIn("and c.targetObjectType in :v", filter.getTypes());

        return SpringQueryBuilderUtils.loadPage(qb, pageable, "id");
    }
}
