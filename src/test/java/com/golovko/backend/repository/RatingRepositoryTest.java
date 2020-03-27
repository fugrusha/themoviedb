package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Rating;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;

import static com.golovko.backend.domain.TargetObjectType.MOVIE;

public class RatingRepositoryTest extends BaseTest {

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
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();

        testObjectFactory.createRating(3, u1, m1.getId(), MOVIE);
        testObjectFactory.createRating(6, u2, m1.getId(), MOVIE);
        testObjectFactory.createRating(3, u1, m2.getId(), MOVIE);  // another movie

        Assert.assertEquals(4.5, ratingRepository.calcAverageRating(m1.getId()), Double.MIN_NORMAL);
    }

    @Test
    public void testFindByIdAndTargetId() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Rating rating = testObjectFactory.createRating(5, user, movie.getId(), MOVIE);
        testObjectFactory.createRating(4, user, movie.getId(), MOVIE);

        Rating actualRating = ratingRepository.findByIdAndTargetId(rating.getId(), movie.getId());

        Assert.assertEquals(actualRating.getId(), rating.getId());
    }

    @Test
    public void testFindAllByTargetId() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        ApplicationUser u3 = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();

        Rating r1 = testObjectFactory.createRating(3, u1, m1.getId(), MOVIE);
        Rating r2 = testObjectFactory.createRating(6, u2, m1.getId(), MOVIE);
        Rating r3 = testObjectFactory.createRating(4, u3, m1.getId(), MOVIE);
        testObjectFactory.createRating(3, u1, m2.getId(), MOVIE);
        testObjectFactory.createRating(3, u2, m2.getId(), MOVIE);

        List<Rating> ratings = ratingRepository.findAllByTargetId(m1.getId());

        Assertions.assertThat(ratings).extracting("id")
                .containsExactlyInAnyOrder(r1.getId(), r2.getId(), r3.getId());
    }

    @Test
    public void testDeleteRatingsByRatedObjectId() {
        ApplicationUser u1 = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();

        Rating r1 = testObjectFactory.createRating(3, u1, m1.getId(), MOVIE);
        Rating r2 = testObjectFactory.createRating(3, u1, m2.getId(), MOVIE);

        transactionTemplate.executeWithoutResult(status -> {
            ratingRepository.deleteRatingsByRatedObjectId(m1.getId(), MOVIE);
        });

        Assert.assertFalse(ratingRepository.existsById(r1.getId()));
        Assert.assertTrue(ratingRepository.existsById(r2.getId()));
    }
}
