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
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Autowired
    private TransactionTemplate transactionTemplate;

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
}
