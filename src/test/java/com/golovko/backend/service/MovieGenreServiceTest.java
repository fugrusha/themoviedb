package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Genre;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.genre.GenreReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.LinkDuplicatedException;
import com.golovko.backend.repository.GenreRepository;
import com.golovko.backend.repository.MovieRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

public class MovieGenreServiceTest extends BaseTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieGenreService movieGenreService;

    @Test
    public void testGetGenresByMovieId() {
        Genre genre = testObjectFactory.createGenre("Comedy");
        Movie movie = testObjectFactory.createMovie();
        movie.setGenres(List.of(genre));
        movieRepository.save(movie);

        List<GenreReadDTO> actualResult = movieGenreService.getMovieGenres(movie.getId());

        Assertions.assertThat(actualResult).extracting("id")
                .contains(genre.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetNotFoundGenresByMovieId() {
        Movie movie = testObjectFactory.createMovie();
        movieGenreService.getMovieGenres(movie.getId());
    }

    @Test
    public void testAddGenreToMovie() {
        Genre genre = testObjectFactory.createGenre("Comedy");
        Movie movie = testObjectFactory.createMovie();

        List<GenreReadDTO> actualResult = movieGenreService.addGenreToMovie(movie.getId(), genre.getId());

        Assert.assertNotNull(actualResult);
        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(genre.getId());

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();
            Assertions.assertThat(savedMovie.getGenres()).extracting("id")
                    .containsExactlyInAnyOrder(genre.getId());

            Genre savedGenre = genreRepository.findById(genre.getId()).get();
            Assertions.assertThat(savedGenre.getMovies()).extracting("id")
                    .containsExactlyInAnyOrder(movie.getId());
        });
    }

    @Test
    public void testDuplicatedGenre() {
        Genre genre = testObjectFactory.createGenre("Comedy");
        Movie movie = testObjectFactory.createMovie();

        movieGenreService.addGenreToMovie(movie.getId(), genre.getId());

        Assertions.assertThatThrownBy(() -> movieGenreService.addGenreToMovie(movie.getId(), genre.getId()))
                .isInstanceOf(LinkDuplicatedException.class);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAddGenreToMovieWrongMovieId() {
        UUID wrongMovieId = UUID.randomUUID();
        Genre genre = testObjectFactory.createGenre("Comedy");

        movieGenreService.addGenreToMovie(wrongMovieId, genre.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAddGenreToMovieWrongGenreId() {
        Movie movie = testObjectFactory.createMovie();
        UUID wrongGenreId = UUID.randomUUID();

        movieGenreService.addGenreToMovie(movie.getId(), wrongGenreId);
    }

    @Test
    public void testRemoveGenreFromMovie() {
        Genre genre = testObjectFactory.createGenre("Comedy");
        Movie movie = testObjectFactory.createMovie();

        movieGenreService.addGenreToMovie(movie.getId(), genre.getId());  //linking

        List<GenreReadDTO> actualResult = movieGenreService.removeGenreFromMovie(movie.getId(), genre.getId());
        Assert.assertTrue(actualResult.isEmpty());

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();
            Assert.assertTrue(savedMovie.getGenres().isEmpty());
        });
    }

    @Test(expected = EntityNotFoundException.class)
    public void testRemoveNotFoundGenre() {
        Genre genre = testObjectFactory.createGenre("Comedy");
        Movie movie = testObjectFactory.createMovie();

        movieGenreService.removeGenreFromMovie(movie.getId(), genre.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testRemoveNotExistedGenre() {
        UUID genreId = UUID.randomUUID();
        Movie movie = testObjectFactory.createMovie();

        movieGenreService.removeGenreFromMovie(movie.getId(), genreId);
    }
}
