package com.golovko.backend.service;

import com.golovko.backend.domain.Genre;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.repository.GenreRepository;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.util.TestObjectFactory;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;
import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = {"delete from genre_movie", "delete from genre", "delete from movie"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieGenreServiceTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void addGenreToMovieTest() {
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
    public void removeLinkGenreAndMovieTest() {
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

    private void inTransaction (Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}
