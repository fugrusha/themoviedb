package com.golovko.backend.repository;

import com.golovko.backend.domain.Misprint;
import com.golovko.backend.dto.misprint.MisprintFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

public class MisprintRepositoryCustomImpl implements MisprintRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Misprint> findByFilter(MisprintFilter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("select m from Misprint m where 1=1");

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

        TypedQuery<Misprint> query = entityManager.createQuery(sb.toString(), Misprint.class);

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

        return query.getResultList();
    }
}
