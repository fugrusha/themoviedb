package com.golovko.backend.repository;

import com.golovko.backend.domain.Genre;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GenreRepository extends CrudRepository<Genre, UUID> {

    List<Genre> findAllByOrderByGenreNameAsc();
}
