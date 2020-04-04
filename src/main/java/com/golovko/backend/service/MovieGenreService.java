package com.golovko.backend.service;

import com.golovko.backend.domain.Genre;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.genre.GenreReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.LinkDuplicatedException;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class MovieGenreService {

    @Autowired
    private RepositoryHelper repoHelper;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TranslationService translationService;

    @Transactional
    public List<GenreReadDTO> getMovieGenres(UUID movieId) {
        Movie movie = repoHelper.getEntityById(Movie.class, movieId);

        if (movie.getGenres() == null || movie.getGenres().isEmpty()) {
            throw new EntityNotFoundException("Movie " + movieId + " has not any genre.");
        }

        return translationService.translateList(movie.getGenres(), GenreReadDTO.class);
    }

    @Transactional
    public List<GenreReadDTO> addGenreToMovie(UUID movieId, UUID genreId) {
        Movie movie = repoHelper.getEntityById(Movie.class, movieId);
        Genre genre = repoHelper.getEntityById(Genre.class, genreId);

        if (movie.getGenres().stream().anyMatch(g -> g.getId().equals(genreId))) {
            throw new LinkDuplicatedException(String.format("Movie %s already has genre %s", movieId, genreId));
        }

        movie.getGenres().add(genre);
        movieRepository.save(movie);

        return translationService.translateList(movie.getGenres(), GenreReadDTO.class);
    }

    @Transactional
    public List<GenreReadDTO> removeGenreFromMovie(UUID movieId, UUID genreId) {
        Movie movie = repoHelper.getEntityById(Movie.class, movieId);

        boolean removed = movie.getGenres().removeIf(g -> g.getId().equals(genreId));

        if (!removed) {
            throw new EntityNotFoundException("Movie " + movieId + " has no genre " + genreId);
        }

        movie = movieRepository.save(movie);

        return translationService.translateList(movie.getGenres(), GenreReadDTO.class);
    }
}
