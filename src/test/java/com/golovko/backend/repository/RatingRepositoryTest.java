package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Rating;
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

import static com.golovko.backend.domain.TargetObjectType.MOVIE;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from rating", "delete from movie", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class RatingRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Rating rating = testObjectFactory.createRating(5, user, movie.getId(), MOVIE);

        Instant createdAtBeforeReload = rating.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        rating = ratingRepository.findById(rating.getId()).get();

        Instant createdAtAfterReload = rating.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Rating rating = testObjectFactory.createRating(5, user, movie.getId(), MOVIE);

        Instant modifiedAtBeforeReload = rating.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        rating = ratingRepository.findById(rating.getId()).get();
        rating.setRating(10);
        rating = ratingRepository.save(rating);
        Instant modifiedAtAfterReload = rating.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testCalcAverageRating() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        ApplicationUser u3 = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();

        testObjectFactory.createRating(3, u1, m1.getId(), MOVIE);
        testObjectFactory.createRating(6, u2, m1.getId(), MOVIE);
        testObjectFactory.createRating((Integer)null, u3, m1.getId(), MOVIE);  // no mark
        testObjectFactory.createRating(3, u1, m2.getId(), MOVIE);  // another movie

        Assert.assertEquals(4.5, ratingRepository.calcAverageRating(m1.getId()), Double.MIN_NORMAL);
    }
}
