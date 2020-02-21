package com.golovko.backend.repository;

import com.golovko.backend.domain.MovieCast;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MovieCastRepository extends CrudRepository<MovieCast, UUID> {

    List<MovieCast> findByMovieId(UUID movieId);

    MovieCast findByIdAndMovieId(UUID id, UUID movieId);
}
