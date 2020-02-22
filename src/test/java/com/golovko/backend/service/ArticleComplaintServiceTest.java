package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ComplaintRepository;
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

import java.util.UUID;

import static com.golovko.backend.domain.ComplaintType.CHILD_ABUSE;
import static com.golovko.backend.domain.ComplaintType.SPAM;
import static com.golovko.backend.domain.ParentType.ARTICLE;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = { "delete from complaint", "delete from article", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ArticleComplaintServiceTest {

    @Autowired
    private ArticleComplaintService articleComplaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Test
    public void getArticleComplaintTest() {
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(article.getId(), ARTICLE, user, moderator);

        ComplaintReadDTO readDTO = articleComplaintService.getComplaint(article.getId(), complaint.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), user.getId());
        Assert.assertEquals(readDTO.getModeratorId(), moderator.getId());
    }

    @Test
    public void createArticleComplaintTest() {
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);
        ApplicationUser user = testObjectFactory.createUser();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(SPAM);

        ComplaintReadDTO readDTO = articleComplaintService.createComplaint(article.getId(), createDTO, user);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository
                .findByIdAndParentId(readDTO.getId(), readDTO.getParentId(), ARTICLE);

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getArticleComplaintWrongIdTest() {
        articleComplaintService.getComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void patchArticleComplaintTest() {
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(article.getId(), ARTICLE, user, moderator);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("another title");
        patchDTO.setComplaintText("another text");
        patchDTO.setComplaintType(CHILD_ABUSE);

        ComplaintReadDTO readDTO = articleComplaintService
                .patchComplaint(article.getId(), complaint.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToIgnoringGivenFields(readDTO,
                "moderatorId", "authorId");

        complaint = complaintRepository.findByIdAndParentId(readDTO.getId(), readDTO.getParentId(), ARTICLE);
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), user.getId());
        Assert.assertEquals(readDTO.getModeratorId(), moderator.getId());
    }

    @Test
    public void patchArticleComplaintEmptyPatchTest() {
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(article.getId(), ARTICLE, user, moderator);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();

        ComplaintReadDTO readDTO = articleComplaintService
                .patchComplaint(article.getId(), complaint.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        Complaint complaintAfterUpdate = complaintRepository
                .findByIdAndParentId(readDTO.getId(), readDTO.getParentId(), ARTICLE);

        Assertions.assertThat(complaintAfterUpdate).hasNoNullFieldsOrProperties();
        Assertions.assertThat(complaintAfterUpdate).isEqualToIgnoringGivenFields(complaint,
                "author", "moderator");
        Assert.assertEquals(readDTO.getAuthorId(), complaintAfterUpdate.getAuthor().getId());
        Assert.assertEquals(readDTO.getModeratorId(), complaintAfterUpdate.getModerator().getId());
    }

    @Test
    public void updateArticleComplaintTest() {
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(article.getId(), ARTICLE, user, moderator);

        ComplaintPutDTO updateDTO = new ComplaintPutDTO();
        updateDTO.setComplaintText("new text");
        updateDTO.setComplaintTitle("new title");
        updateDTO.setComplaintType(CHILD_ABUSE);

        ComplaintReadDTO readDTO = articleComplaintService
                .updateComplaint(article.getId(), complaint.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToIgnoringGivenFields(readDTO,
                "moderatorId", "authorId");

        complaint = complaintRepository.findByIdAndParentId(readDTO.getId(), readDTO.getParentId(), ARTICLE);
        Assertions.assertThat(complaint).isEqualToIgnoringGivenFields(readDTO,
                "moderator", "author");
        Assert.assertEquals(readDTO.getAuthorId(), user.getId());
        Assert.assertEquals(readDTO.getModeratorId(), moderator.getId());
    }

    @Test
    public void deleteArticleComplaintTest() {
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(article.getId(), ARTICLE, user, moderator);

        articleComplaintService.deleteComplaint(article.getId(), complaint.getId());

        Assert.assertFalse(complaintRepository.existsById(complaint.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteArticleComplaintNotFound() {
        articleComplaintService.deleteComplaint(UUID.randomUUID(), UUID.randomUUID());
    }
}
