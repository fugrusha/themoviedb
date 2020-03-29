package com.golovko.backend.repository;

import com.golovko.backend.domain.Complaint;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ComplaintRepositoryCustom {

    Page<Complaint> findByFilter(ComplaintFilter filter, Pageable pageable);
}
