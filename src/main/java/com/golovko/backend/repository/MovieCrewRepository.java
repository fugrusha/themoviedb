package com.golovko.backend.repository;

import com.golovko.backend.domain.MovieCrew;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface MovieCrewRepository extends CrudRepository<MovieCrew, UUID> {

    MovieCrew findByIdAndMovieId(UUID id, UUID movieId);

    Page<MovieCrew> findByMovieId(UUID movieId, Pageable pageable);

    @Query("select mc.id from MovieCrew mc")
    Stream<UUID> getIdsOfMovieCrews();
}
