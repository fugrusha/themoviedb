package com.golovko.backend.repository;

import com.golovko.backend.domain.MovieCast;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface MovieCastRepository extends CrudRepository<MovieCast, UUID> {

    List<MovieCast> findByMovieId(UUID movieId);

    MovieCast findByIdAndMovieId(UUID id, UUID movieId);

    @Query("select mc.id from MovieCast mc")
    Stream<UUID> getIdsOfMovieCasts();

    @Query("select avg(mc.averageRating) from MovieCast mc"
            + " where mc.person.id = :personId")
    Double calcAverageRatingOfPerson(UUID personId);
}
