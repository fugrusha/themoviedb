package com.golovko.backend.repository;


import com.golovko.backend.domain.*;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {
        "delete from movie",
        "delete from like",
        "delete from article",
        "delete from user_role",
        "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class LikeRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Like like = testObjectFactory.createLike(true, user, article.getId());

        Instant createdAtBeforeReload = like.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        like = likeRepository.findById(like.getId()).get();

        Instant createdAtAfterReload = like.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Like like = testObjectFactory.createLike(true, user, article.getId());

        Instant updatedAtBeforeReload = like.getUpdatedAt();
        Assert.assertNotNull(updatedAtBeforeReload);

        like = likeRepository.findById(like.getId()).get();
        like.setMeLiked(false);
        like = likeRepository.save(like);
        Instant updatedAtAfterReload = like.getUpdatedAt();

        Assert.assertNotNull(updatedAtAfterReload);
        Assert.assertTrue(updatedAtBeforeReload.isBefore(updatedAtAfterReload));
    }

    @Test
    public void testFindByIdAndUserId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);

        Like like = testObjectFactory.createLike(true, user1, article.getId());
        testObjectFactory.createLike(true, user1, article.getId());
        testObjectFactory.createLike(true, user2, article.getId()); // wrong user
        testObjectFactory.createLike(true, user2, article.getId());

        Like savedLike = likeRepository.findByIdAndUserId(like.getId(), user1.getId());

        Assert.assertEquals(like.getId(), savedLike.getId());
    }

    @Test
    public void testFindByUserIdAndLikedObjectId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();

        Like like = testObjectFactory.createLike(true, user1, movie1.getId());
        testObjectFactory.createLike(true, user1, movie2.getId());
        testObjectFactory.createLike(true, user2, movie1.getId()); // wrong user
        testObjectFactory.createLike(true, user2, movie2.getId()); // wrong user

        Like likeFromDb = likeRepository.findByUserIdAndLikedObjectId(user1.getId(), movie1.getId());
        Assert.assertEquals(like.getId(), likeFromDb.getId());
    }

    @Test
    public void testDeleteLikesByLikedObjectId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Article a2 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        Like like1 = testObjectFactory.createLike(true, user1, a1.getId(), TargetObjectType.ARTICLE);
        Like like2 = testObjectFactory.createLike(true, user1, a2.getId(), TargetObjectType.ARTICLE);
        Like like3 = testObjectFactory.createLike(true, user2, movie.getId(), TargetObjectType.MOVIE);

        transactionTemplate.executeWithoutResult(status -> {
            likeRepository.deleteLikesByTargetObjectId(a1.getId(), TargetObjectType.ARTICLE);
        });

        Assert.assertFalse(likeRepository.existsById(like1.getId()));
        Assert.assertTrue(likeRepository.existsById(like2.getId()));
        Assert.assertTrue(likeRepository.existsById(like3.getId()));
    }
}
