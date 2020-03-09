package com.golovko.backend.repository;

import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.TargetObjectType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends CrudRepository<Complaint, UUID>, ComplaintRepositoryCustom {

    Complaint findByIdAndAuthorId(UUID id, UUID authorId);

    List<Complaint> findByAuthorIdOrderByCreatedAtAsc(UUID authorId);

    @Query("select c from Complaint c where c.id = :id and"
            + " c.targetObjectId = :targetId and c.targetObjectType = :targetType")
    Complaint findByIdAndTargetId(UUID id, UUID targetId, TargetObjectType targetType);
}
