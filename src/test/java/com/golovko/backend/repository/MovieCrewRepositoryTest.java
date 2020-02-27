package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCrew;
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
@Sql(statements = {"delete from person", "delete from movie", "delete from movie_crew"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieCrewRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Test
    public void testCreateAtIsSet() {
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
    public void testModifiedAtIsSet() {
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

}
