package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Rating;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.RatingRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class UserMatchServiceTest extends BaseTest {

    @Autowired
    private UserMatchService userMatchService;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    public void testUpdateTopMatchersForUserWithZeroCorrelation() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        ApplicationUser u3 = testObjectFactory.createUser();

        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        Movie m3 = testObjectFactory.createMovie();
        Movie m4 = testObjectFactory.createMovie();

        testObjectFactory.createRating(6, u1, m1.getId(), TargetObjectType.MOVIE);  // same movie
        testObjectFactory.createRating(10, u1, m4.getId(), TargetObjectType.MOVIE);

        testObjectFactory.createRating(1, u2, m2.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(2, u2, m3.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(4, u2, m1.getId(), TargetObjectType.MOVIE); // same movie

        testObjectFactory.createRating(7, u3, m2.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(8, u3, m3.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(9, u3, m1.getId(), TargetObjectType.MOVIE); // same movie

        userMatchService.updateUserTopMatches(u1.getId());

        inTransaction(() -> {
            ApplicationUser updatedUser = applicationUserRepository.findById(u1.getId()).get();
            Assert.assertEquals(2, updatedUser.getTopMatches().size());

            Assertions.assertThat(updatedUser.getTopMatches()).extracting("id")
                    .containsExactlyInAnyOrder(u2.getId(), u3.getId());

            Assertions.assertThat(updatedUser.getTopMatches()).extracting("id")
                    .doesNotContain(u1.getId());
        });
    }

    @Test
    public void testUpdateTopMatchersForUser() {
        ApplicationUser u1 = testObjectFactory.createUser();

        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        Movie m3 = testObjectFactory.createMovie();
        Movie m4 = testObjectFactory.createMovie();

        testObjectFactory.createRating(6, u1, m1.getId(), TargetObjectType.MOVIE);  // same movie
        testObjectFactory.createRating(10, u1, m2.getId(), TargetObjectType.MOVIE); // same movie
        testObjectFactory.createRating(8, u1, m3.getId(), TargetObjectType.MOVIE); // same movie

        int usersCount = 15;
        for (int i = 0; i < usersCount; i++) {
            ApplicationUser u = testObjectFactory.createUser();

            createRating(u, m1.getId()); // same movie
            createRating(u, m2.getId()); // same movie
            createRating(u, m3.getId()); // same movie
            createRating(u, m4.getId());
        }

        userMatchService.updateUserTopMatches(u1.getId());

        inTransaction(() -> {
            ApplicationUser updatedUser = applicationUserRepository.findById(u1.getId()).get();
            Assert.assertNotNull(updatedUser.getTopMatches());
            Assert.assertEquals(10, updatedUser.getTopMatches().size());

            Assertions.assertThat(updatedUser.getTopMatches()).extracting("id")
                    .doesNotContain(u1.getId());
        });
    }

    @Test
    public void testDescOrderOfTopMathcers() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        ApplicationUser u3 = testObjectFactory.createUser();
        ApplicationUser u4 = testObjectFactory.createUser();

        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        Movie m3 = testObjectFactory.createMovie();

        testObjectFactory.createRating(6, u1, m1.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(10, u1, m3.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(7, u1, m2.getId(), TargetObjectType.MOVIE);

        testObjectFactory.createRating(4, u2, m1.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(4, u2, m3.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(8, u2, m2.getId(), TargetObjectType.MOVIE);
        // correlation will be -0.277

        testObjectFactory.createRating(9, u3, m1.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(5, u3, m3.getId(), TargetObjectType.MOVIE);
        // correlation will be -1.0

        testObjectFactory.createRating(6, u4, m2.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(8, u4, m1.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(8, u4, m3.getId(), TargetObjectType.MOVIE);
        // correlation will be 0.693

        userMatchService.updateUserTopMatches(u1.getId());

        inTransaction(() -> {
            ApplicationUser updatedUser = applicationUserRepository.findById(u1.getId()).get();
            Assert.assertEquals(3, updatedUser.getTopMatches().size());

            Assertions.assertThat(updatedUser.getTopMatches()).extracting("id")
                    .containsExactly(u4.getId(), u2.getId(), u3.getId());

            Assertions.assertThat(updatedUser.getTopMatches()).extracting("id")
                    .doesNotContain(u1.getId());
        });
    }

    private Rating createRating(ApplicationUser author, UUID targetObjectId) {
        Rating rating = new Rating();
        rating.setRating(ThreadLocalRandom.current().nextInt(1, 10));
        rating.setRatedObjectId(targetObjectId);
        rating.setRatedObjectType(TargetObjectType.MOVIE);
        rating.setAuthor(author);
        return ratingRepository.save(rating);
    }
}
