package com.golovko.backend.repository;

import com.golovko.backend.domain.Complaint;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import org.bitbucket.brunneng.qb.JpaQueryBuilder;
import org.bitbucket.brunneng.qb.SpringQueryBuilderUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class ComplaintRepositoryCustomImpl implements ComplaintRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Complaint> findByFilter(ComplaintFilter filter, Pageable pageable) {
        JpaQueryBuilder qb = new JpaQueryBuilder(entityManager);
        qb.append("select c from Complaint c where 1=1");
        qb.append("and c.moderator.id = :v", filter.getModeratorId());
        qb.append("and c.author.id = :v", filter.getAuthorId());
        qb.appendIn("and c.complaintType in :v", filter.getComplaintTypes());
        qb.appendIn("and c.targetObjectType in :v", filter.getTargetObjectTypes());
        qb.appendIn("and c.complaintStatus in :v", filter.getStatuses());

        return SpringQueryBuilderUtils.loadPage(qb, pageable, "id");
    }
}
