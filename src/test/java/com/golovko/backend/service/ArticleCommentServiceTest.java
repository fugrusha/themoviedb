package com.golovko.backend.service;

import com.golovko.backend.domain.*;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from comment", "delete from article", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ArticleCommentServiceTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private ArticleCommentService articleCommentService;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    public void getArticleCommentTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Comment comment = testObjectFactory.createComment(user, article.getId(), CommentStatus.APPROVED);

        CommentReadDTO readDTO = articleCommentService.getComment(article.getId(), comment.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(comment, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), comment.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getArticleCommentWrongIdTest() {
        articleCommentService.getComment(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void getAllArticleCommentsTest() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article1 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c1 = testObjectFactory.createComment(user1, article1.getId(), CommentStatus.APPROVED);
        Comment c2 = testObjectFactory.createComment(user2, article1.getId(), CommentStatus.BLOCKED);
        Comment c3 = testObjectFactory.createComment(user2, article1.getId(), CommentStatus.PENDING);

        List<CommentReadDTO> comments = articleCommentService.getAllComments(article1.getId());

        Assertions.assertThat(comments).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());
    }

    @Test
    public void getAllPublishedArticleCommentsTest() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article1 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment comment1 = testObjectFactory.createComment(user2, article1.getId(), CommentStatus.APPROVED);
        Comment comment2 = testObjectFactory.createComment(user2, article1.getId(), CommentStatus.APPROVED);
        testObjectFactory.createComment(user2, article1.getId(), CommentStatus.NEED_MODERATION);
        testObjectFactory.createComment(user1, article1.getId(), CommentStatus.PENDING);
        testObjectFactory.createComment(user1, article1.getId(), CommentStatus.BLOCKED);

        List<CommentReadDTO> comments = articleCommentService.getAllPublishedComments(article1.getId());

        Assertions.assertThat(comments).extracting("id")
                .containsExactlyInAnyOrder(comment1.getId(), comment2.getId());
    }

    @Test
    public void createArticleCommentTest() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");

        CommentReadDTO readDTO = articleCommentService.createComment(article.getId(), createDTO, user2);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(comment, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), comment.getAuthor().getId());
    }

    @Test
    public void patchArticleCommentTest() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), CommentStatus.APPROVED);

        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("New message");

        CommentReadDTO readDTO = articleCommentService.patchComment(article.getId(), c.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        Comment commentAfterUpdate = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(commentAfterUpdate).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(commentAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void patchArticleCommentEmptyPatchTest() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), CommentStatus.APPROVED);

        CommentPatchDTO patchDTO = new CommentPatchDTO();

        CommentReadDTO readDTO = articleCommentService.patchComment(article.getId(), c.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        Comment commentAfterUpdate = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(commentAfterUpdate).hasNoNullFieldsOrProperties();
        Assertions.assertThat(commentAfterUpdate).isEqualToIgnoringGivenFields(c, "author");
        Assert.assertEquals(commentAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void updateArticleCommentTest() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), CommentStatus.APPROVED);

        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("message text");

        CommentReadDTO readDTO = articleCommentService.updateComment(article.getId(), c.getId(), putDTO);

        Assertions.assertThat(putDTO).isEqualToComparingFieldByField(readDTO);

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(comment).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(comment.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void deleteArticleCommentTest() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), CommentStatus.APPROVED);

        articleCommentService.deleteComment(article.getId(), c.getId());

        Assert.assertFalse(commentRepository.existsById(c.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteArticleCommentNotFound() {
        articleCommentService.deleteComment(UUID.randomUUID(), UUID.randomUUID());
    }
}
