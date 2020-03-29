package com.golovko.backend.repository;

import com.golovko.backend.domain.MovieCast;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface MovieCastRepository extends CrudRepository<MovieCast, UUID> {

    Page<MovieCast> findByMovieId(UUID movieId, Pageable pageable);

    MovieCast findByIdAndMovieId(UUID id, UUID movieId);

    @Query("select mc.id from MovieCast mc")
    Stream<UUID> getIdsOfMovieCasts();

    @Query("select avg(mc.averageRating) from MovieCast mc"
            + " where mc.person.id = :personId")
    Double calcAverageRatingOfPerson(UUID personId);
}
