package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.Comment;
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

import static com.golovko.backend.domain.CommentStatus.APPROVED;
import static com.golovko.backend.domain.CommentStatus.NEED_MODERATION;
import static com.golovko.backend.domain.TargetObjectType.ARTICLE;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from comment", "delete from article", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CommentRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment comment = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        Instant createdAtBeforeReload = comment.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        comment = commentRepository.findById(comment.getId()).get();

        Instant createdAtAfterReload = comment.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment comment = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        Instant modifiedAtBeforeReload = comment.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        comment = commentRepository.findById(comment.getId()).get();
        comment.setStatus(NEED_MODERATION);
        comment = commentRepository.save(comment);
        Instant modifiedAtAfterReload = comment.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }
}
