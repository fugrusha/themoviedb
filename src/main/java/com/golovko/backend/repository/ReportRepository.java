package com.golovko.backend.repository;

import com.golovko.backend.domain.Report;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ReportRepository extends CrudRepository<Report, UUID> {
}
