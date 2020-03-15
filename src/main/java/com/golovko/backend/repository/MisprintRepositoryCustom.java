package com.golovko.backend.repository;

import com.golovko.backend.domain.Misprint;
import com.golovko.backend.dto.misprint.MisprintFilter;

import java.util.List;

public interface MisprintRepositoryCustom {

    List<Misprint> findByFilter(MisprintFilter filter);
}
