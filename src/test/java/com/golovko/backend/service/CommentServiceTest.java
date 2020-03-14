package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.Comment;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.UUID;

import static com.golovko.backend.domain.CommentStatus.*;
import static com.golovko.backend.domain.TargetObjectType.ARTICLE;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {
        "delete from comment",
        "delete from article",
        "delete from user_role",
        "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class CommentServiceTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    public void testGetCommentById() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Comment comment = testObjectFactory.createComment(user, article.getId(), APPROVED, ARTICLE);

        CommentReadDTO readDTO = commentService.getComment(article.getId(), comment.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(comment, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), comment.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetCommentWrongId() {
        commentService.getComment(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testGetAllComments() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article1 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c1 = testObjectFactory.createComment(user1, article1.getId(), APPROVED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user2, article1.getId(), BLOCKED, ARTICLE);
        Comment c3 = testObjectFactory.createComment(user2, article1.getId(), PENDING, ARTICLE);

        List<CommentReadDTO> comments = commentService.getAllComments(article1.getId());

        Assertions.assertThat(comments).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());
    }

    @Test
    public void testGetAllPublishedComments() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article1 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment comment1 = testObjectFactory.createComment(user2, article1.getId(), APPROVED, ARTICLE);
        Comment comment2 = testObjectFactory.createComment(user2, article1.getId(), APPROVED, ARTICLE);
        testObjectFactory.createComment(user2, article1.getId(), NEED_MODERATION, ARTICLE);
        testObjectFactory.createComment(user1, article1.getId(), PENDING, ARTICLE);
        testObjectFactory.createComment(user1, article1.getId(), BLOCKED, ARTICLE);

        List<CommentReadDTO> comments = commentService.getAllPublishedComments(article1.getId());

        Assertions.assertThat(comments).extracting("id")
                .containsExactlyInAnyOrder(comment1.getId(), comment2.getId());
    }

    @Test
    public void testCreateComment() {
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(ARTICLE);

        CommentReadDTO readDTO = commentService.createComment(article.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(comment, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), comment.getAuthor().getId());
    }

    @Test
    public void testPatchComment() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("New message");

        CommentReadDTO readDTO = commentService.patchComment(article.getId(), c.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        Comment commentAfterUpdate = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(commentAfterUpdate).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(commentAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testPatchCommentEmptyPatch() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        CommentPatchDTO patchDTO = new CommentPatchDTO();

        CommentReadDTO readDTO = commentService.patchComment(article.getId(), c.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        Comment commentAfterUpdate = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(commentAfterUpdate).hasNoNullFieldsOrProperties();
        Assertions.assertThat(commentAfterUpdate).isEqualToIgnoringGivenFields(c, "author");
        Assert.assertEquals(commentAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testUpdateComment() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("message text");

        CommentReadDTO readDTO = commentService.updateComment(article.getId(), c.getId(), putDTO);

        Assertions.assertThat(putDTO).isEqualToComparingFieldByField(readDTO);

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(comment).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(comment.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testDeleteComment() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        commentService.deleteComment(article.getId(), c.getId());

        Assert.assertFalse(commentRepository.existsById(c.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteCommentNotFound() {
        commentService.deleteComment(UUID.randomUUID(), UUID.randomUUID());
    }
}
