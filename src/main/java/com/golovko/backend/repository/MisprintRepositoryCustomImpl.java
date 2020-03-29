package com.golovko.backend.repository;

import com.golovko.backend.domain.Misprint;
import com.golovko.backend.dto.misprint.MisprintFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class MisprintRepositoryCustomImpl implements MisprintRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RepositoryHelper repoHelper;

    @Override
    public Page<Misprint> findByFilter(MisprintFilter filter, Pageable pageable) {
        StringBuilder sb = new StringBuilder();
        sb.append("select m from Misprint m where 1=1");

        Query query = createQueryApplyingFilter(filter, pageable.getSort(), sb);
        repoHelper.applyPaging(query, pageable);

        List<Misprint> data = query.getResultList();

        long count = getCountOfMovies(filter);
        return new PageImpl<>(data, pageable, count);
    }

    private Query createQueryApplyingFilter(MisprintFilter filter, Sort sort, StringBuilder sb) {

        if (filter.getModeratorId() != null) {
            sb.append(" and m.moderator.id = :moderatorId");
        }
        if (filter.getAuthorId() != null) {
            sb.append(" and m.author.id = :authorId");
        }
        if (filter.getTargetObjectTypes() != null && !filter.getTargetObjectTypes().isEmpty()) {
            sb.append(" and m.targetObjectType in :targetObjectTypes");
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            sb.append(" and m.status in :statuses");
        }

        if (sort != null && sort.isSorted()) {
            sb.append(" order by");
            for (Sort.Order order : sort.toList()) {
                sb.append(" m.").append(order.getProperty()).append(" ").append(order.getDirection());
            }
        }

        Query query = entityManager.createQuery(sb.toString());

        if (filter.getModeratorId() != null) {
            query.setParameter("moderatorId", filter.getModeratorId());
        }
        if (filter.getAuthorId() != null) {
            query.setParameter("authorId", filter.getAuthorId());
        }
        if (filter.getTargetObjectTypes() != null && !filter.getTargetObjectTypes().isEmpty()) {
            query.setParameter("targetObjectTypes", filter.getTargetObjectTypes());
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            query.setParameter("statuses", filter.getStatuses());
        }

        return query;
    }

    private long getCountOfMovies(MisprintFilter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(m) from Misprint m where 1=1");
        Query query = createQueryApplyingFilter(filter, null, sb);
        return ((Number) query.getResultList().get(0)).longValue();
    }
}
