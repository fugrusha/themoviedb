package com.golovko.backend.repository;

import com.golovko.backend.domain.Complaint;
import com.golovko.backend.dto.complaint.ComplaintFilter;

import java.util.List;

public interface ComplaintRepositoryCustom {

    List<Complaint> findByFilter(ComplaintFilter filter);
}
