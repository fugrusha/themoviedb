package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

import static com.golovko.backend.domain.CommentStatus.*;
import static com.golovko.backend.domain.TargetObjectType.ARTICLE;
import static com.golovko.backend.domain.TargetObjectType.MOVIE;

public class CommentRepositoryTest extends BaseTest {

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

    @Test
    public void testFindByIdAndTargetId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);

        Comment expectedComment = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        Comment actualComment = commentRepository.findByIdAndTargetId(expectedComment.getId(), article.getId());

        Assertions.assertThat(expectedComment).isEqualToIgnoringGivenFields(actualComment, "author");
        Assert.assertEquals(user2.getId(), actualComment.getAuthor().getId());
    }

    @Test
    public void testFindAllByStatusAndTargetId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);

        Comment c1 = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);
        testObjectFactory.createComment(user2, article.getId(), NEED_MODERATION, ARTICLE);
        testObjectFactory.createComment(user2, article.getId(), BLOCKED, ARTICLE);

        Page<Comment> comments = commentRepository
                .findAllByStatusAndTarget(article.getId(), APPROVED, Pageable.unpaged());

        Assertions.assertThat(comments).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testDeleteCommentsByTargetObjectId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article1 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Article article2 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user2, article1.getId(), APPROVED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user2, article2.getId(), APPROVED, ARTICLE); // another id
        Comment c3 = testObjectFactory.createComment(user2, movie.getId(), NEED_MODERATION, MOVIE); // another type

        transactionTemplate.executeWithoutResult(status -> {
            commentRepository.deleteCommentsByTargetObjectId(article1.getId(), ARTICLE);
        });

        Assert.assertTrue(commentRepository.existsById(c2.getId()));
        Assert.assertTrue(commentRepository.existsById(c3.getId()));
        Assert.assertFalse(commentRepository.existsById(c1.getId()));
    }

    @Test
    public void testIncrementLikesCountField() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, movie.getId(), APPROVED, MOVIE);
        c1.setLikesCount(5);
        commentRepository.save(c1);

        transactionTemplate.executeWithoutResult(status -> {
            commentRepository.incrementLikesCountField(c1.getId());
        });

        Comment updatedComment = commentRepository.findById(c1.getId()).get();
        Assert.assertEquals((Integer) 6, updatedComment.getLikesCount());
    }

    @Test
    public void testDecrementLikesCountField() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, movie.getId(), APPROVED, MOVIE);
        c1.setLikesCount(5);
        commentRepository.save(c1);

        transactionTemplate.executeWithoutResult(status -> {
            commentRepository.decrementLikesCountField(c1.getId());
        });

        Comment updatedComment = commentRepository.findById(c1.getId()).get();
        Assert.assertEquals((Integer) 4, updatedComment.getLikesCount());
    }

    @Test
    public void testIncrementDislikesCountField() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, movie.getId(), APPROVED, MOVIE);
        c1.setDislikesCount(5);
        commentRepository.save(c1);

        transactionTemplate.executeWithoutResult(status -> {
            commentRepository.incrementDislikesCountField(c1.getId());
        });

        Comment updatedComment = commentRepository.findById(c1.getId()).get();
        Assert.assertEquals((Integer) 6, updatedComment.getDislikesCount());
    }

    @Test
    public void testDecrementDislikesCountField() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, movie.getId(), APPROVED, MOVIE);
        c1.setDislikesCount(5);
        commentRepository.save(c1);

        transactionTemplate.executeWithoutResult(status -> {
            commentRepository.decrementDislikesCountField(c1.getId());
        });

        Comment updatedComment = commentRepository.findById(c1.getId()).get();
        Assert.assertEquals((Integer) 4, updatedComment.getDislikesCount());
    }
}
