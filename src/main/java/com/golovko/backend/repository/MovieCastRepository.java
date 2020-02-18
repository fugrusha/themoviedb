package com.golovko.backend.repository;

import com.golovko.backend.domain.MovieCast;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface MovieCastRepository extends CrudRepository<MovieCast, UUID> {

    // TODO Why? For What?
    @Query("select mc from MovieCast mc where mc.person.id = :personId and" +
            " mc.movie.releaseDate >= :releasedFrom and mc.movie.releaseDate < :releasedTo" +
            " order by mc.movie.releaseDate asc")
    List<MovieCast> findMovieCastsWithActorInGivenInterval(UUID personId, LocalDate releasedFrom, LocalDate releasedTo);

    List<MovieCast> findByMovieId(UUID movieId);

    MovieCast findByIdAndMovieId(UUID id, UUID movieId);
}
