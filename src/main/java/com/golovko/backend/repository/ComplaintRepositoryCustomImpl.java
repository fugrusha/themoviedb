package com.golovko.backend.repository;

import com.golovko.backend.domain.Complaint;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.util.List;

public class ComplaintRepositoryCustomImpl implements ComplaintRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private RepositoryHelper repoHelper;

    @Override
    public Page<Complaint> findByFilter(ComplaintFilter filter, Pageable pageable) {
        StringBuilder sb = new StringBuilder();
        sb.append("select c from Complaint c where 1=1");

        Query query = createQueryApplyingFilter(filter, pageable.getSort(), sb);
        repoHelper.applyPaging(query, pageable);

        List<Complaint> data = query.getResultList();

        long count = getCountOfComplaints(filter);
        return new PageImpl<>(data, pageable, count);
    }

    private Query createQueryApplyingFilter(ComplaintFilter filter, Sort sort, StringBuilder sb) {

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

        if (sort != null && sort.isSorted()) {
            sb.append(" order by");
            for (Sort.Order order : sort.toList()) {
                sb.append(" c.").append(order.getProperty()).append(" ").append(order.getDirection());
            }
        }

        Query query = entityManager.createQuery(sb.toString());

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

        return query;
    }

    private long getCountOfComplaints(ComplaintFilter filter) {
        StringBuilder sb = new StringBuilder();
        sb.append("select count(c) from Complaint c where 1=1");
        Query query = createQueryApplyingFilter(filter, null, sb);
        return ((Number) query.getResultList().get(0)).longValue();
    }
}
