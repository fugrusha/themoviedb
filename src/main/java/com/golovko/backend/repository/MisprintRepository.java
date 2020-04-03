package com.golovko.backend.repository;

import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.Misprint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface MisprintRepository extends CrudRepository<Misprint, UUID>, MisprintRepositoryCustom {

    Misprint findByIdAndAuthorId(UUID id, UUID authorId);

    Page<Misprint> findByAuthorId(UUID authorId, Pageable pageable);

    Page<Misprint> findAllByTargetObjectId(UUID targetObjectId, Pageable pageable);

    Misprint findByIdAndTargetObjectId(UUID id, UUID targetObjectId);

    @Query("select m from Misprint m where m.targetObjectId = :targetObjectId"
            + " and m.misprintText = :misprintText"
            + " and m.status = :status")
    Stream<Misprint> findSimilarMisprints(UUID targetObjectId, String misprintText, ComplaintStatus status);
}
