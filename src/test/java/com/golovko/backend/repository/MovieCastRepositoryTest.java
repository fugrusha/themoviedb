package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.Person;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MovieCastRepositoryTest extends BaseTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Test
    public void testCreatedAtIsSet() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        Instant createdAtBeforeReload = movieCast.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        movieCast = movieCastRepository.findById(movieCast.getId()).get();

        Instant createdAtAfterReload = movieCast.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
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

    @Test
    public void testGetIdsOfMovieCasts() {
        Set<UUID> expectedResult = new HashSet<>();
        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie();
        expectedResult.add(testObjectFactory.createMovieCast(p1, m1).getId());
        expectedResult.add(testObjectFactory.createMovieCast(p2, m1).getId());

        transactionTemplate.executeWithoutResult(status -> {
            Set<UUID> actualResult = movieCastRepository.getIdsOfMovieCasts().collect(Collectors.toSet());
            Assert.assertEquals(expectedResult, actualResult);
        });
    }

    @Test
    public void testGetMovieCastsByMovieId() {
        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();
        Person p3 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        MovieCast mc1 = testObjectFactory.createMovieCast(p1, m1);
        MovieCast mc2 = testObjectFactory.createMovieCast(p2, m1);
        testObjectFactory.createMovieCast(p2, m2);
        testObjectFactory.createMovieCast(p3, m2);

        Page<MovieCast> movieCasts = movieCastRepository.findByMovieId(m1.getId(), Pageable.unpaged());

        Assertions.assertThat(movieCasts).extracting("id")
                .containsExactlyInAnyOrder(mc1.getId(), mc2.getId());
    }

    @Test
    public void testFindByIdAndMovieId() {
        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie();
        MovieCast mc1 = testObjectFactory.createMovieCast(p1, m1);
        testObjectFactory.createMovieCast(p2, m1);

        MovieCast actualResult = movieCastRepository.findByIdAndMovieId(mc1.getId(), m1.getId());

        Assert.assertEquals(mc1.getId(), actualResult.getId());
    }

    @Test
    public void testCalcAverageRatingOfPerson() {
        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        Movie m3 = testObjectFactory.createMovie();

        testObjectFactory.createMovieCast(p1, m1, 5.0);
        testObjectFactory.createMovieCast(p2, m1, 2.0); // another person
        testObjectFactory.createMovieCast(p1, m2, null); // without rating
        testObjectFactory.createMovieCast(p1, m3, 3.0);

        Double averageRating = movieCastRepository.calcAverageRatingOfPerson(p1.getId());

        Assert.assertEquals(4.0, averageRating, Double.MIN_NORMAL);
    }
}
