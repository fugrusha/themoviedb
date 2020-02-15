package com.golovko.backend.repository;

import com.golovko.backend.domain.Complaint;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends CrudRepository<Complaint, UUID> {

    List<Complaint> findByAuthorIdOrderByCreatedAtAsc(UUID authorId);

    Complaint findByIdAndAuthorId(UUID id, UUID authorId);

    Complaint findByIdAndParentId(UUID id, UUID parentId);

    List<Complaint> findByParentIdOrderByCreatedAtAsc(UUID parentId);
}
