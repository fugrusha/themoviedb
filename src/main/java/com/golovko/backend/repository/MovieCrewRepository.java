package com.golovko.backend.repository;

import com.golovko.backend.domain.MovieCrew;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface MovieCrewRepository extends CrudRepository<MovieCrew, UUID> {

    MovieCrew findByIdAndMovieId(UUID id, UUID movieId);

    List<MovieCrew> findByMovieId(UUID movieId);

    @Query("select mc.id from MovieCrew mc")
    Stream<UUID> getIdsOfMovieCrews();
}
