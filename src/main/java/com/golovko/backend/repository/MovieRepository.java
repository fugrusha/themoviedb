package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface MovieRepository extends CrudRepository<Movie, UUID>, MovieRepositoryCustom {

    @Query("select m.id from Movie m")
    Stream<UUID> getIdsOfMovies();

    @Query("select avg(m.averageRating) from Movie m"
            + " join m.movieCasts mc"
            + " where mc.person.id = :personId")
    Double calcAverageRatingOfPersonMovies(UUID personId);
}
