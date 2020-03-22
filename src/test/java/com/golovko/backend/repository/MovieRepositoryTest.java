package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
@Sql(statements = {
        "delete from movie_cast",
        "delete from person",
        "delete from movie"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void testCreatedAtIsSet() {
        Movie movie = testObjectFactory.createMovie();

        Instant createdAtBeforeReload = movie.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        movie = movieRepository.findById(movie.getId()).get();

        Instant createdAtAfterReload = movie.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        Movie movie = testObjectFactory.createMovie();

        Instant modifiedAtBeforeReload = movie.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        movie = movieRepository.findById(movie.getId()).get();
        movie.setMovieTitle("Another Movie Title");
        movie = movieRepository.save(movie);
        Instant modifiedAtAfterReload = movie.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testGetIdsOfMovies() {
        Set<UUID> expectedResult = new HashSet<>();
        expectedResult.add(testObjectFactory.createMovie().getId());
        expectedResult.add(testObjectFactory.createMovie().getId());
        expectedResult.add(testObjectFactory.createMovie().getId());

        transactionTemplate.executeWithoutResult(status -> {
            Set<UUID> actualResult = movieRepository.getIdsOfMovies().collect(Collectors.toSet());
            Assert.assertEquals(expectedResult, actualResult);
        });
    }

    @Test
    public void testCalcAverageRatingOfPersonMovies() {
        Movie m1 = testObjectFactory.createMovie(5.0);
        Movie m2 = testObjectFactory.createMovie(4.0);
        Movie m3 = testObjectFactory.createMovie(null);
        Movie m4 = testObjectFactory.createMovie(9.0);

        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();

        testObjectFactory.createMovieCast(p1, m1);
        testObjectFactory.createMovieCast(p1, m2);
        testObjectFactory.createMovieCast(p1, m3);
        testObjectFactory.createMovieCast(p2, m4); // wrong person
        testObjectFactory.createMovieCast(p2, m3); // wrong person

        Double result = movieRepository.calcAverageRatingOfPersonMovies(p1.getId());
        Assert.assertEquals(4.5, result, Double.MIN_NORMAL);
    }

    @Test
    public void testIncrementLikesCountField() {
        Movie m1 = testObjectFactory.createMovie();
        m1.setLikesCount(5);
        movieRepository.save(m1);

        transactionTemplate.executeWithoutResult(status -> {
            movieRepository.incrementLikesCountField(m1.getId());
        });

        Movie updatedMovie = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals((Integer) 6, updatedMovie.getLikesCount());
    }

    @Test
    public void testDecrementLikesCountField() {
        Movie m1 = testObjectFactory.createMovie();
        m1.setLikesCount(5);
        movieRepository.save(m1);

        transactionTemplate.executeWithoutResult(status ->  {
            movieRepository.decrementLikesCountField(m1.getId());
        });

        Movie updatedMovie = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals((Integer) 4, updatedMovie.getLikesCount());
    }

    @Test
    public void testIncrementDislikesCountField() {
        Movie m1 = testObjectFactory.createMovie();
        m1.setDislikesCount(5);
        movieRepository.save(m1);

        transactionTemplate.executeWithoutResult(status -> {
            movieRepository.incrementDislikesCountField(m1.getId());
        });

        Movie updatedMovie = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals((Integer) 6, updatedMovie.getDislikesCount());
    }

    @Test
    public void testDecrementDislikesCountField() {
        Movie m1 = testObjectFactory.createMovie();
        m1.setDislikesCount(5);
        movieRepository.save(m1);

        transactionTemplate.executeWithoutResult(status ->  {
            movieRepository.decrementDislikesCountField(m1.getId());
        });

        Movie updatedMovie = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals((Integer) 4, updatedMovie.getDislikesCount());
    }
}
