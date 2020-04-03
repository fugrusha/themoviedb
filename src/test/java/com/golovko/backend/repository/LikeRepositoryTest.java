package com.golovko.backend.repository;


import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static com.golovko.backend.domain.TargetObjectType.ARTICLE;
import static com.golovko.backend.domain.TargetObjectType.MOVIE;

public class LikeRepositoryTest extends BaseTest {

    @Autowired
    private LikeRepository likeRepository;

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Like like = testObjectFactory.createLike(true, user, article.getId(), ARTICLE);

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

        Like like = testObjectFactory.createLike(true, user, article.getId(), ARTICLE);

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

        Like like = testObjectFactory.createLike(true, user1, article.getId(), ARTICLE);
        testObjectFactory.createLike(true, user1, article.getId(), ARTICLE);
        testObjectFactory.createLike(true, user2, article.getId(), ARTICLE); // wrong user
        testObjectFactory.createLike(true, user2, article.getId(), ARTICLE);

        Like savedLike = likeRepository.findByIdAndUserId(like.getId(), user1.getId());

        Assert.assertEquals(like.getId(), savedLike.getId());
    }

    @Test
    public void testFindByUserIdAndLikedObjectId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();

        Like like = testObjectFactory.createLike(true, user1, movie1.getId(), MOVIE);
        testObjectFactory.createLike(true, user1, movie2.getId(), MOVIE);
        testObjectFactory.createLike(true, user2, movie1.getId(), MOVIE); // wrong user
        testObjectFactory.createLike(true, user2, movie2.getId(), MOVIE); // wrong user

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

        Like like1 = testObjectFactory.createLike(true, user1, a1.getId(), ARTICLE);
        Like like2 = testObjectFactory.createLike(true, user1, a2.getId(), ARTICLE);
        Like like3 = testObjectFactory.createLike(true, user2, movie.getId(), MOVIE);

        transactionTemplate.executeWithoutResult(status -> {
            likeRepository.deleteLikesByTargetObjectId(a1.getId(), ARTICLE);
        });

        Assert.assertFalse(likeRepository.existsById(like1.getId()));
        Assert.assertTrue(likeRepository.existsById(like2.getId()));
        Assert.assertTrue(likeRepository.existsById(like3.getId()));
    }
}
