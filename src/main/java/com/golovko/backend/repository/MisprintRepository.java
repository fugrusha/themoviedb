package com.golovko.backend.repository;

import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.Misprint;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MisprintRepository extends CrudRepository<Misprint, UUID>, MisprintRepositoryCustom {

    Misprint findByIdAndAuthorId(UUID id, UUID authorId);

    List<Misprint> findByAuthorIdOrderByCreatedAtAsc(UUID authorId);

    List<Misprint> findAllByTargetObjectId(UUID targetObjectId);

    Misprint findByIdAndTargetObjectId(UUID id, UUID targetObjectId);

    @Query("select m from Misprint m where m.targetObjectId = :targetObjectId"
            + " and m.misprintText = :misprintText"
            + " and m.status = :status")
    List<Misprint> findSimilarMisprints(UUID targetObjectId, String misprintText, ComplaintStatus status);
}
