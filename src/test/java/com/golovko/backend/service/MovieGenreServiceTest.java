package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Genre;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.repository.GenreRepository;
import com.golovko.backend.repository.MovieRepository;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Set;
import java.util.UUID;

public class MovieGenreServiceTest extends BaseTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Test
    public void testAddGenreToMovie() {
        Genre genre = testObjectFactory.createGenre("Comedy");
        Movie movie = testObjectFactory.createMovie();

        movie.setGenres(Set.of(genre));
        movieRepository.save(movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();
            Assert.assertEquals(1, savedMovie.getGenres().size());

            Genre savedGenre = genreRepository.findById(genre.getId()).get();
            Assert.assertEquals(1, savedGenre.getMovies().size());
        });
    }

    @Ignore // TODO add unlink option
    @Test
    public void testRemoveLinkGenreAndMovie() {
        Genre genre = testObjectFactory.createGenre("Comedy");
        Movie movie = testObjectFactory.createMovie();
        UUID movieId = movie.getId();

        movie.setGenres(Set.of(genre));
        movie = movieRepository.save(movie);

        movie.getGenres().remove(genre);
        movieRepository.save(movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movieId).get();
            Assert.assertEquals(0, savedMovie.getGenres().size());
        });
    }
}
