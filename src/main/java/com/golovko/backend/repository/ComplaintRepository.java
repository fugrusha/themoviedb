package com.golovko.backend.repository;

import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.TargetObjectType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface ComplaintRepository extends CrudRepository<Complaint, UUID>, ComplaintRepositoryCustom {

    Complaint findByIdAndAuthorId(UUID id, UUID authorId);

    Page<Complaint> findByAuthorId(UUID authorId, Pageable pageable);

    @Query("select c from Complaint c where c.id = :id"
            + " and c.targetObjectId = :targetId"
            + " and c.targetObjectType = :targetType")
    Complaint findByIdAndTargetId(UUID id, UUID targetId, TargetObjectType targetType);

    @Query("select c from Complaint c where c.targetObjectId = :objectId"
            + " and c.complaintType = :complaintType"
            + " and c.complaintStatus = :status")
    Stream<Complaint> findSimilarComplaints(UUID objectId, ComplaintType complaintType, ComplaintStatus status);
}
