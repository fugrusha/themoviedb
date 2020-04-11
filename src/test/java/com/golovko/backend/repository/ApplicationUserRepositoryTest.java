package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.user.UserInLeaderBoardDTO;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import static com.golovko.backend.domain.TargetObjectType.MOVIE;
import static com.golovko.backend.domain.TargetObjectType.MOVIE_CAST;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ApplicationUserRepositoryTest extends BaseTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    public void testSaveUser() {
        ApplicationUser user = testObjectFactory.createUser();

        assertNotNull(user.getId());
        assertTrue(applicationUserRepository.findById(user.getId()).isPresent());
    }

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();

        Instant createdAtBeforeReload = user.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        user = applicationUserRepository.findById(user.getId()).get();

        Instant createdAtAfterReload = user.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();

        Instant modifiedAtBeforeReload = user.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        user = applicationUserRepository.findById(user.getId()).get();
        user.setEmail("new.user@email.com");
        user = applicationUserRepository.save(user);
        Instant modifiedAtAfterReload = user.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testGetAllUsers() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        ApplicationUser user3 = testObjectFactory.createUser();

        Page<ApplicationUser> actualResult = applicationUserRepository.getAllUsers(Pageable.unpaged());

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(user1.getId(), user2.getId(), user3.getId());
    }

    @Test
    public void testFindByEmail() {
        String expectedEmail = "mynewtest@email.com";

        testObjectFactory.createUser();
        testObjectFactory.createUser();

        ApplicationUser user = testObjectFactory.createUser();
        user.setEmail(expectedEmail);
        applicationUserRepository.save(user);

        ApplicationUser actualUser = applicationUserRepository.findByEmail(expectedEmail);
        Assert.assertEquals(user.getId(), actualUser.getId());
        Assert.assertEquals(expectedEmail, actualUser.getEmail());
    }

    @Test
    public void testGetUsersLeaderBoardByMovieComments() {
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        Person p = testObjectFactory.createPerson();
        MovieCast mc = testObjectFactory.createMovieCast(p, m1);

        Set<UUID> userIds = new HashSet<>();
        for (int i = 0; i < 100; ++i) {
            ApplicationUser user = createUser();
            userIds.add(user.getId());

            createComment(user, m1.getId(), MOVIE);
            createComment(user, m2.getId(), MOVIE);
            createComment(user, mc.getId(), MOVIE_CAST);
            createRating(5, user, m1.getId(), MOVIE);
            createRating(9, user, m2.getId(), MOVIE);
            createRating(9, user, mc.getId(), MOVIE_CAST);
        }

        List<UserInLeaderBoardDTO> actualResult = applicationUserRepository.getUsersLeaderBoard();

        Assertions.assertThat(actualResult).isSortedAccordingTo(
                Comparator.comparing(UserInLeaderBoardDTO::getTrustLevel).reversed());

        Assert.assertEquals(userIds, actualResult.stream()
                .map(UserInLeaderBoardDTO::getId)
                .collect(Collectors.toSet()));

        for (UserInLeaderBoardDTO u : actualResult) {
            Assert.assertNotNull(u.getUsername());
            Assert.assertNotNull(u.getTrustLevel());
            Assert.assertEquals(2, u.getCommentsCount().longValue());
            Assert.assertEquals(2, u.getRatedMoviesCount().longValue());
        }
    }

    private ApplicationUser createUser() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("username");
        user.setPassword("123456789");
        user.setEmail("vetal@gmail.com");
        user.setIsBlocked(false);
        user.setTrustLevel(ThreadLocalRandom.current().nextDouble(1, 100));
        return applicationUserRepository.save(user);
    }

    private Comment createComment(
            ApplicationUser author,
            UUID targetObjectId,
            TargetObjectType targetObjectType) {
        Comment comment = new Comment();
        comment.setMessage("text");
        comment.setStatus(CommentStatus.APPROVED);
        comment.setAuthor(author);
        comment.setTargetObjectType(targetObjectType);
        comment.setTargetObjectId(targetObjectId);
        return commentRepository.save(comment);
    }

    private Rating createRating(
            Integer starRating,
            ApplicationUser author,
            UUID targetObjectId,
            TargetObjectType targetObjectType
    ) {
        Rating rating = new Rating();
        rating.setRating(starRating);
        rating.setRatedObjectId(targetObjectId);
        rating.setRatedObjectType(targetObjectType);
        rating.setAuthor(author);
        return ratingRepository.save(rating);
    }
}