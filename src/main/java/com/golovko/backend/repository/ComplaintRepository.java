package com.golovko.backend.repository;

import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ComplaintRepository extends CrudRepository<Complaint, UUID> {

    List<Complaint> findByAuthorIdAndComplaintType(UUID authorId, ComplaintType type);
}