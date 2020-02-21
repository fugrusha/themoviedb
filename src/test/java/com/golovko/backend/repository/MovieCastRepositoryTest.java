package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.Person;
import com.golovko.backend.util.TestObjectFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;
import java.time.LocalDate;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from person", "delete from movie", "delete from movie_cast"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieCastRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Test
    public void testCreateAtIsSet() {
        Person person = testObjectFactory.createPerson();
        Movie movie = createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        Instant createdAtBeforeReload = movieCast.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        movieCast = movieCastRepository.findById(movieCast.getId()).get();

        Instant createdAtAfterReload = movieCast.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testModifiedAtIsSet() {
        Person person = testObjectFactory.createPerson();
        Movie movie = createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        Instant modifiedAtBeforeReload = movieCast.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        movieCast = movieCastRepository.findById(movieCast.getId()).get();
        movieCast.setCharacter("Another Character Role");
        movieCast = movieCastRepository.save(movieCast);
        Instant modifiedAtAfterReload = movieCast.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    private Movie createMovie() {
        Movie movie = new Movie();
        movie.setMovieTitle("Title of the Movie");
        movie.setDescription("movie description");
        movie.setIsReleased(true);
        movie.setReleaseDate(LocalDate.of(1992, 5, 4));
        movie.setAverageRating(5.0);
        movie = movieRepository.save(movie);
        return movie;
    }
}
