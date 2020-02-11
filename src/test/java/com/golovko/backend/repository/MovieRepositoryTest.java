package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
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

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
@Sql(statements = "delete from movie", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Test
    public void testCreateAtIsSet() {
        Movie movie = testObjectFactory.createMovie();

        Instant createdAtBeforeReload = movie.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        movie = movieRepository.findById(movie.getId()).get();

        Instant createdAtAfterReload = movie.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testModifiedAtIsSet() {
        Movie movie = testObjectFactory.createMovie();

        Instant modifiedAtBeforeReload = movie.getLastModifiedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        movie = movieRepository.findById(movie.getId()).get();
        movie.setMovieTitle("Another Movie Title");
        movie = movieRepository.save(movie);
        Instant modifiedAtAfterReload = movie.getLastModifiedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }
}
