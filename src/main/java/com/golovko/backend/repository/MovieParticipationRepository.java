package com.golovko.backend.repository;

import com.golovko.backend.domain.MovieParticipation;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieParticipationRepository extends CrudRepository<MovieParticipation, UUID> {
    MovieParticipation findByIdAndMovieId(UUID id, UUID movieId);

    List<MovieParticipation> findByMovieId(UUID movieId);
}
