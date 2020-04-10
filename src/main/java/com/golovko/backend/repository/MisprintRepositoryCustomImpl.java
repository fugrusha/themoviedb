package com.golovko.backend.repository;

import com.golovko.backend.domain.Misprint;
import com.golovko.backend.dto.misprint.MisprintFilter;
import org.bitbucket.brunneng.qb.JpaQueryBuilder;
import org.bitbucket.brunneng.qb.SpringQueryBuilderUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

public class MisprintRepositoryCustomImpl implements MisprintRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Page<Misprint> findByFilter(MisprintFilter filter, Pageable pageable) {
        JpaQueryBuilder qb = new JpaQueryBuilder(entityManager);
        qb.append("select m from Misprint m where 1=1");
        qb.append("and m.moderator.id = :v", filter.getModeratorId());
        qb.append("and m.author.id = :v", filter.getAuthorId());
        qb.appendIn("and m.targetObjectType in :v", filter.getTargetObjectTypes());
        qb.appendIn("and m.status in :v", filter.getStatuses());

        return SpringQueryBuilderUtils.loadPage(qb, pageable, "id");
    }
}
