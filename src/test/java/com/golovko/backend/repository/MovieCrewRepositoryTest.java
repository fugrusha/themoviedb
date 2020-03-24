package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCrew;
import com.golovko.backend.domain.Person;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MovieCrewRepositoryTest extends BaseTest {

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Test
    public void testCreatedAtIsSet() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        Instant createdAtBeforeReload = movieCrew.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        movieCrew = movieCrewRepository.findById(movieCrew.getId()).get();

        Instant createdAtAfterReload = movieCrew.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        Instant modifiedAtBeforeReload = movieCrew.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        movieCrew = movieCrewRepository.findById(movieCrew.getId()).get();
        movieCrew.setDescription("Another participation info");
        movieCrew = movieCrewRepository.save(movieCrew);
        Instant modifiedAtAfterReload = movieCrew.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testGetIdsOfMovieCrews() {
        Set<UUID> expectedResult = new HashSet<>();
        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie();
        expectedResult.add(testObjectFactory.createMovieCrew(p1, m1).getId());
        expectedResult.add(testObjectFactory.createMovieCrew(p2, m1).getId());

        transactionTemplate.executeWithoutResult(status -> {
            Set<UUID> actualResult = movieCrewRepository.getIdsOfMovieCrews().collect(Collectors.toSet());
            Assert.assertEquals(expectedResult, actualResult);
        });
    }

    @Test
    public void testGetMovieCrewsByMovieId() {
        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();
        Person p3 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        MovieCrew mc1 = testObjectFactory.createMovieCrew(p1, m1);
        MovieCrew mc2 = testObjectFactory.createMovieCrew(p2, m1);
        testObjectFactory.createMovieCrew(p2, m2);
        testObjectFactory.createMovieCrew(p3, m2);

        List<MovieCrew> movieCrews = movieCrewRepository.findByMovieId(m1.getId());

        Assertions.assertThat(movieCrews).extracting("id")
                .containsExactlyInAnyOrder(mc1.getId(), mc2.getId());
    }

    @Test
    public void testFindByIdAndMovieId() {
        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie();
        MovieCrew mc1 = testObjectFactory.createMovieCrew(p1, m1);
        testObjectFactory.createMovieCrew(p2, m1);

        MovieCrew actualResult = movieCrewRepository.findByIdAndMovieId(mc1.getId(), m1.getId());

        Assert.assertEquals(mc1.getId(), actualResult.getId());
    }
}
