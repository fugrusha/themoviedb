package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.MovieFilter;

import java.util.List;

public interface MovieRepositoryCustom {
    List<Movie> findByFilter(MovieFilter filter);
}
