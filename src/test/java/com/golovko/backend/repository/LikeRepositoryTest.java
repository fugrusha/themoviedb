package com.golovko.backend.repository;


import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.Like;
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
@Sql(statements = {
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
}
