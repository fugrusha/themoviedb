package com.golovko.backend.repository;

import com.golovko.backend.domain.MovieParticipation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MovieParticipationRepository extends CrudRepository<MovieParticipation, UUID> {
}
