package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieParticipation;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from person", "delete from movie", "delete from movie_participation"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieParticipationRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private MovieParticipationRepository movieParticipationRepository;

    @Test
    public void testCreateAtIsSet() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation moviePart = testObjectFactory.createMovieParticipation(person, movie);

        Instant createdAtBeforeReload = moviePart.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        moviePart = movieParticipationRepository.findById(moviePart.getId()).get();

        Instant createdAtAfterReload = moviePart.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testModifiedAtIsSet() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation moviePart = testObjectFactory.createMovieParticipation(person, movie);

        Instant modifiedAtBeforeReload = moviePart.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        moviePart = movieParticipationRepository.findById(moviePart.getId()).get();
        moviePart.setPartInfo("Another participation info");
        moviePart = movieParticipationRepository.save(moviePart);
        Instant modifiedAtAfterReload = moviePart.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

}
