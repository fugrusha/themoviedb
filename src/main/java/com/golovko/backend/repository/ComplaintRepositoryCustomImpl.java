package com.golovko.backend.repository;

import com.golovko.backend.domain.Complaint;
import com.golovko.backend.dto.complaint.ComplaintFilter;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

public class ComplaintRepositoryCustomImpl implements ComplaintRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<Complaint> findByFilter(ComplaintFilter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("select c from Complaint c where 1=1");

        if (filter.getModeratorId() != null) {
            sb.append(" and c.moderator.id = :moderatorId");
        }
        if (filter.getAuthorId() != null) {
            sb.append(" and c.author.id = :authorId");
        }
        if (filter.getComplaintTypes() != null && !filter.getComplaintTypes().isEmpty()) {
            sb.append(" and c.complaintType in :complaintTypes");
        }
        if (filter.getTargetObjectTypes() != null && !filter.getTargetObjectTypes().isEmpty()) {
            sb.append(" and c.targetObjectType in :targetObjectTypes");
        }
        if (filter.getStatuses() != null && !filter.getStatuses().isEmpty()) {
            sb.append(" and c.complaintStatus in :statuses");
        }

        TypedQuery<Complaint> query = entityManager.createQuery(sb.toString(), Complaint.class);

        if (filter.getModeratorId() != null) {
            query.setParameter("moderatorId", filter.getModeratorId());
        }
        if (filter.getAuthorId() != null) {
            query.setParameter("authorId", filter.getAuthorId());
        }
        if (filter.getComplaintTypes() != null && !filter.getComplaintTypes().isEmpty()) {
            query.setParameter("complaintTypes", filter.getComplaintTypes());
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
