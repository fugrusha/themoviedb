package com.golovko.backend.repository;

import com.golovko.backend.domain.Misprint;
import com.golovko.backend.dto.misprint.MisprintFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MisprintRepositoryCustom {

    Page<Misprint> findByFilter(MisprintFilter filter, Pageable pageable);
}
