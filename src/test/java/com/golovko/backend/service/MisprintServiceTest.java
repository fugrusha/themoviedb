package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.misprint.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.UnprocessableEntityException;
import com.golovko.backend.repository.ArticleRepository;
import com.golovko.backend.repository.MisprintRepository;
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

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = {
        "delete from movie",
        "delete from article",
        "delete from misprint",
        "delete from user_role",
        "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MisprintServiceTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private MisprintService misprintService;

    @Autowired
    private MisprintRepository misprintRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Test
    public void testGetMisprintComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), TargetObjectType.MOVIE, user);

        MisprintReadDTO readDTO = misprintService.getMisprintComplaint(user.getId(), m1.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(m1, "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), m1.getAuthor().getId());
    }

    @Test
    public void testGetAllUserMisprintComplaints() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), TargetObjectType.MOVIE, user1);
        Misprint m2 = testObjectFactory.createMisprint(movie.getId(), TargetObjectType.MOVIE, user1);
        testObjectFactory.createMisprint(movie.getId(), TargetObjectType.MOVIE, user2);
        testObjectFactory.createMisprint(movie.getId(), TargetObjectType.MOVIE, user2);

        List<MisprintReadDTO> misprints = misprintService.getAllMisprintComplaints(user1.getId());

        Assertions.assertThat(misprints).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetMisprintComplaintWrongId() {
        misprintService.getMisprintComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testCreateMisprintComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        MisprintCreateDTO createDTO = new MisprintCreateDTO();
        createDTO.setStartIndex(5);
        createDTO.setEndIndex(15);
        createDTO.setReplaceTo("Text text text");
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);
        createDTO.setTargetObjectId(movie.getId());

        MisprintReadDTO readDTO = misprintService.createMisprintComplaint(user.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Misprint misprint = misprintRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(misprint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), misprint.getAuthor().getId());
    }

    @Test
    public void testPatchMisprintComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), TargetObjectType.MOVIE, user);

        MisprintPatchDTO patchDTO = new MisprintPatchDTO();
        patchDTO.setReplaceTo("another text");
        patchDTO.setStartIndex(45);
        patchDTO.setEndIndex(65);

        MisprintReadDTO readDTO = misprintService.patchMisprintComplaint(user.getId(), m1.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToIgnoringGivenFields(readDTO,
                "moderatorId", "authorId");

        m1 = misprintRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(m1).isEqualToIgnoringGivenFields(readDTO, "moderator", "author");
        Assert.assertEquals(readDTO.getAuthorId(), m1.getAuthor().getId());
    }

    @Test
    public void testPatchMisprintComplaintEmptyPatch() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), TargetObjectType.MOVIE, user);

        MisprintPatchDTO patchDTO = new MisprintPatchDTO();

        MisprintReadDTO readDTO = misprintService.patchMisprintComplaint(user.getId(), m1.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrPropertiesExcept("moderatorId", "fixedAt",
                "replacedWith", "reason");

        Misprint misprintAfterUpdate = misprintRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(misprintAfterUpdate).hasNoNullFieldsOrPropertiesExcept("moderator", "fixedAt",
                "replacedWith", "reason");
        Assertions.assertThat(misprintAfterUpdate).isEqualToIgnoringGivenFields(m1,"author", "moderator");
        Assert.assertEquals(readDTO.getAuthorId(), misprintAfterUpdate.getAuthor().getId());
    }

    @Test
    public void testUpdateMisprintComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), TargetObjectType.MOVIE, user);

        MisprintPutDTO updateDTO = new MisprintPutDTO();
        updateDTO.setStartIndex(45);
        updateDTO.setEndIndex(65);
        updateDTO.setReplaceTo("new title");

        MisprintReadDTO readDTO = misprintService.updateMisprintComplaint(user.getId(), m1.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToIgnoringGivenFields(readDTO,
                "moderatorId", "authorId");

        m1 = misprintRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(m1).isEqualToIgnoringGivenFields(readDTO, "moderator", "author");
        Assert.assertEquals(readDTO.getAuthorId(), m1.getAuthor().getId());
    }

    @Test
    public void testDeleteMisprintComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), TargetObjectType.MOVIE, user);

        misprintService.deleteMisprintComplaint(user.getId(), m1.getId());

        Assert.assertFalse(misprintRepository.existsById(m1.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteMisprintComplaintNotFound() {
        misprintService.deleteMisprintComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testReplaceTextInArticle() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        String textBeforeUpdate = article.getText();

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(moderator.getId());
        confirmDTO.setStartIndex(2);
        confirmDTO.setEndIndex(8);
        confirmDTO.setReplaceTo("REPLACED_TEXT");

        misprintService.replaceMisprint(article.getId(), confirmDTO);

        Article updatedArticle = articleRepository.findById(article.getId()).get();
        String textAfterUpdate = updatedArticle.getText();

        Assert.assertNotEquals(textBeforeUpdate, textAfterUpdate);
    }

    @Test(expected = UnprocessableEntityException.class)
    public void testThrowUnprocessableEntityException() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), TargetObjectType.ARTICLE, user);
        m1.setStatus(ComplaintStatus.DUPLICATE);
        m1 = misprintRepository.save(m1);

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();

        misprintService.confirmModeration(article.getId(), m1.getId(), confirmDTO);
    }

    @Test
    public void testConfirmModeration() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), TargetObjectType.ARTICLE, user);

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(moderator.getId());
        confirmDTO.setStartIndex(5);
        confirmDTO.setEndIndex(20);
        confirmDTO.setReplaceTo("REPLACED_TEXT");

        MisprintReadDTO readDTO = misprintService.confirmModeration(article.getId(), m1.getId(), confirmDTO);

        Assert.assertNotNull(readDTO.getModeratorId());
        Assert.assertNotNull(readDTO.getReplacedWith());
        Assert.assertNotNull(readDTO.getFixedAt());

        Misprint updatedMisprint = misprintRepository.findById(m1.getId()).get();
        Assert.assertEquals(moderator.getId(), updatedMisprint.getModerator().getId());
        Assert.assertEquals(confirmDTO.getReplaceTo(), updatedMisprint.getReplacedWith());
    }


    @Test
    public void testRejectModeration() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), TargetObjectType.ARTICLE, user);

        MisprintRejectDTO rejectDTO = new MisprintRejectDTO();
        rejectDTO.setModeratorId(moderator.getId());
        rejectDTO.setStatus(ComplaintStatus.DUPLICATE);
        rejectDTO.setReason("some reason");

        MisprintReadDTO readDTO = misprintService.rejectModeration(article.getId(), m1.getId(), rejectDTO);

        Assert.assertNull(readDTO.getReplacedWith());

        Assert.assertNotNull(readDTO.getModeratorId());
        Assert.assertNotNull(readDTO.getFixedAt());
        Assert.assertNotNull(readDTO.getStatus());
        Assert.assertNotNull(readDTO.getReason());

        Misprint updatedMisprint = misprintRepository.findById(m1.getId()).get();
        Assert.assertEquals(moderator.getId(), updatedMisprint.getModerator().getId());
        Assert.assertEquals(updatedMisprint.getReason(), rejectDTO.getReason());
        Assert.assertEquals(updatedMisprint.getStatus(), rejectDTO.getStatus());
    }
}
