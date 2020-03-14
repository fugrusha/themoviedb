package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.misprint.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.EntityWrongStatusException;
import com.golovko.backend.repository.*;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = {
        "delete from movie_cast",
        "delete from movie_crew",
        "delete from person",
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

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Test
    public void testGetMisprintComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user, "misprint");

        MisprintReadDTO readDTO = misprintService.getMisprintComplaint(user.getId(), m1.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(m1, "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), m1.getAuthor().getId());
    }

    @Test
    public void testGetAllUserMisprintComplaints() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user1, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user1, "misprint");
        testObjectFactory.createMisprint(movie.getId(), MOVIE, user2, "misprint");
        testObjectFactory.createMisprint(movie.getId(), MOVIE, user2, "misprint");

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
        createDTO.setMisprintText("misprint");
        createDTO.setReplaceTo("Text text text");
        createDTO.setTargetObjectType(MOVIE);
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

        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user, "misprint");

        MisprintPatchDTO patchDTO = new MisprintPatchDTO();
        patchDTO.setReplaceTo("another text");
        patchDTO.setMisprintText("misprint");

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
        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user, "misprint");

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
        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user, "misprint");

        MisprintPutDTO updateDTO = new MisprintPutDTO();
        updateDTO.setMisprintText("misprint");
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
        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user, "misprint");

        misprintService.deleteMisprintComplaint(user.getId(), m1.getId());

        Assert.assertFalse(misprintRepository.existsById(m1.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteMisprintComplaintNotFound() {
        misprintService.deleteMisprintComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testGetAllMisprintsByTargetId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie1.getId(), MOVIE, user1, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(movie1.getId(), MOVIE, user1, "misprint");
        testObjectFactory.createMisprint(movie2.getId(), MOVIE, user1, "misprint");
        testObjectFactory.createMisprint(movie2.getId(), MOVIE, user1, "misprint");

        List<MisprintReadDTO> expectedResult = misprintService.getAllMisprintsByTargetId(movie1.getId());

        Assertions.assertThat(expectedResult).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetMisprintByIdAndTargetId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie1.getId(), MOVIE, user1, "misprint");
        m1.setTargetObjectId(movie1.getId());
        misprintRepository.save(m1);

        testObjectFactory.createMisprint(movie1.getId(), MOVIE, user1, "misprint");

        MisprintReadDTO readDTO = misprintService.getMisprintByTargetId(movie1.getId(), m1.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(m1, "authorId", "moderatorId");
        Assert.assertEquals(readDTO.getAuthorId(), m1.getAuthor().getId());
    }

    @Test
    public void testReplaceMisprintInText() {
        String textBeforeUpdate = "simply dummy text of the printing and typesetting industry.";
        String misprintText = "dummy";

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setStartIndex(7);
        confirmDTO.setEndIndex(12);
        confirmDTO.setReplaceTo("DUMMY");

        String expectedResult = "simply DUMMY text of the printing and typesetting industry.";

        String actualResult = misprintService.replaceMisprint(textBeforeUpdate, misprintText, confirmDTO);

        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test(expected = ResponseStatusException.class)
    public void testReplaceMisprintInTextWrongIndex() {
        String textBeforeUpdate = "simply dummy text of the printing and typesetting industry.";
        String misprintText = "dummy";

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setStartIndex(5); // wrong index
        confirmDTO.setEndIndex(12);
        confirmDTO.setReplaceTo("DUMMY");

        misprintService.replaceMisprint(textBeforeUpdate, misprintText, confirmDTO);
    }

    @Test(expected = ResponseStatusException.class)
    public void testReplaceMisprintAtAlreadyUpdatedText() {
        String alreadyUpdatedText = "simply DUMMY text of the printing and typesetting industry.";
        String misprintText = "dummy";

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setStartIndex(7);
        confirmDTO.setEndIndex(12);
        confirmDTO.setReplaceTo("DUMMY");

        misprintService.replaceMisprint(alreadyUpdatedText, misprintText, confirmDTO);
    }

    @Test(expected = EntityWrongStatusException.class)
    public void testConfirmModerationAndThrowEntityWrongStatusException() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");
        m1.setStatus(ComplaintStatus.DUPLICATE);
        m1 = misprintRepository.save(m1);

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();

        misprintService.confirmModeration(m1.getId(), confirmDTO);
    }

    @Test(expected = EntityWrongStatusException.class)
    public void testRejectModerationAndThrowEntityWrongStatusException() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");
        m1.setStatus(ComplaintStatus.DUPLICATE);
        m1 = misprintRepository.save(m1);

        MisprintRejectDTO rejectDTO = new MisprintRejectDTO();

        misprintService.rejectModeration(m1.getId(), rejectDTO);
    }

    @Test
    public void testConfirmModerationArticleMisprint() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        String textBeforeUpdate = "simply dummy text of the printing and typesetting industry.";
        String expectedText = "simply REPLACED_TEXT text of the printing and typesetting industry.";

        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        article.setText(textBeforeUpdate);
        articleRepository.save(article);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "dummy");

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(moderator.getId());
        confirmDTO.setStartIndex(7);
        confirmDTO.setEndIndex(12);
        confirmDTO.setReplaceTo("REPLACED_TEXT");

        MisprintReadDTO readDTO = misprintService.confirmModeration(m1.getId(), confirmDTO);

        Assert.assertNotNull(readDTO.getModeratorId());
        Assert.assertNotNull(readDTO.getReplacedWith());
        Assert.assertNotNull(readDTO.getFixedAt());

        Misprint updatedMisprint = misprintRepository.findById(m1.getId()).get();
        Assert.assertEquals(moderator.getId(), updatedMisprint.getModerator().getId());
        Assert.assertEquals(confirmDTO.getReplaceTo(), updatedMisprint.getReplacedWith());

        Article updatedArticle = articleRepository.findById(article.getId()).get();
        Assert.assertEquals(expectedText, updatedArticle.getText());
    }

    @Test
    public void testConfirmModerationMovieMisprint() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        String textBeforeUpdate = "simply dummy text of the printing and typesetting industry.";
        String expectedText = "simply REPLACED_TEXT text of the printing and typesetting industry.";

        Movie movie = testObjectFactory.createMovie();
        movie.setDescription(textBeforeUpdate);
        movieRepository.save(movie);

        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user, "dummy");

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(moderator.getId());
        confirmDTO.setStartIndex(7);
        confirmDTO.setEndIndex(12);
        confirmDTO.setReplaceTo("REPLACED_TEXT");

        MisprintReadDTO readDTO = misprintService.confirmModeration(m1.getId(), confirmDTO);

        Assert.assertNotNull(readDTO.getModeratorId());
        Assert.assertNotNull(readDTO.getReplacedWith());
        Assert.assertNotNull(readDTO.getFixedAt());

        Misprint updatedMisprint = misprintRepository.findById(m1.getId()).get();
        Assert.assertEquals(moderator.getId(), updatedMisprint.getModerator().getId());
        Assert.assertEquals(confirmDTO.getReplaceTo(), updatedMisprint.getReplacedWith());

        Movie updatedMovie = movieRepository.findById(movie.getId()).get();
        Assert.assertEquals(expectedText, updatedMovie.getDescription());
    }

    @Test
    public void testConfirmModerationPersonMisprint() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        String textBeforeUpdate = "simply dummy text";
        String expectedText = "simply REPLACED_TEXT text";

        Person person = testObjectFactory.createPerson();
        person.setBio(textBeforeUpdate);
        personRepository.save(person);

        Misprint m1 = testObjectFactory.createMisprint(person.getId(), PERSON, user, "dummy");

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(moderator.getId());
        confirmDTO.setStartIndex(7);
        confirmDTO.setEndIndex(12);
        confirmDTO.setReplaceTo("REPLACED_TEXT");

        MisprintReadDTO readDTO = misprintService.confirmModeration(m1.getId(), confirmDTO);

        Assert.assertNotNull(readDTO.getModeratorId());
        Assert.assertNotNull(readDTO.getReplacedWith());
        Assert.assertNotNull(readDTO.getFixedAt());

        Misprint updatedMisprint = misprintRepository.findById(m1.getId()).get();
        Assert.assertEquals(moderator.getId(), updatedMisprint.getModerator().getId());
        Assert.assertEquals(confirmDTO.getReplaceTo(), updatedMisprint.getReplacedWith());

        Person updatedPerson = personRepository.findById(person.getId()).get();
        Assert.assertEquals(expectedText, updatedPerson.getBio());
    }

    @Test
    public void testConfirmModerationMovieCastMisprint() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        String textBeforeUpdate = "simply dummy text";
        String expectedText = "simply REPLACED_TEXT text";

        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);
        movieCast.setDescription(textBeforeUpdate);
        movieCastRepository.save(movieCast);

        Misprint m1 = testObjectFactory.createMisprint(movieCast.getId(), MOVIE_CAST, user, "dummy");

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(moderator.getId());
        confirmDTO.setStartIndex(7);
        confirmDTO.setEndIndex(12);
        confirmDTO.setReplaceTo("REPLACED_TEXT");

        MisprintReadDTO readDTO = misprintService.confirmModeration(m1.getId(), confirmDTO);

        Assert.assertNotNull(readDTO.getModeratorId());
        Assert.assertNotNull(readDTO.getReplacedWith());
        Assert.assertNotNull(readDTO.getFixedAt());

        Misprint updatedMisprint = misprintRepository.findById(m1.getId()).get();
        Assert.assertEquals(moderator.getId(), updatedMisprint.getModerator().getId());
        Assert.assertEquals(confirmDTO.getReplaceTo(), updatedMisprint.getReplacedWith());

        MovieCast updatedMovieCast = movieCastRepository.findById(movieCast.getId()).get();
        Assert.assertEquals(expectedText, updatedMovieCast.getDescription());
    }

    @Test
    public void testConfirmModerationMovieCrewMisprint() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        String textBeforeUpdate = "simply dummy text";
        String expectedText = "simply REPLACED_TEXT text";

        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);
        movieCrew.setDescription(textBeforeUpdate);
        movieCrewRepository.save(movieCrew);

        Misprint m1 = testObjectFactory.createMisprint(movieCrew.getId(), MOVIE_CREW, user, "dummy");

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(moderator.getId());
        confirmDTO.setStartIndex(7);
        confirmDTO.setEndIndex(12);
        confirmDTO.setReplaceTo("REPLACED_TEXT");

        MisprintReadDTO readDTO = misprintService.confirmModeration(m1.getId(), confirmDTO);

        Assert.assertNotNull(readDTO.getModeratorId());
        Assert.assertNotNull(readDTO.getReplacedWith());
        Assert.assertNotNull(readDTO.getFixedAt());

        Misprint updatedMisprint = misprintRepository.findById(m1.getId()).get();
        Assert.assertEquals(moderator.getId(), updatedMisprint.getModerator().getId());
        Assert.assertEquals(confirmDTO.getReplaceTo(), updatedMisprint.getReplacedWith());

        MovieCrew updatedMovieCrew = movieCrewRepository.findById(movieCrew.getId()).get();
        Assert.assertEquals(expectedText, updatedMovieCrew.getDescription());
    }

    @Test(expected = ResponseStatusException.class)
    public void testConfirmModerationWrongTargetObjectType() {
        ApplicationUser user = testObjectFactory.createUser();
        Person person = testObjectFactory.createPerson();

        Misprint m1 = testObjectFactory.createMisprint(person.getId(), COMMENT, user, "dummy");

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();

        misprintService.confirmModeration(m1.getId(), confirmDTO);
    }

    @Test
    public void testRejectModeration() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");

        MisprintRejectDTO rejectDTO = new MisprintRejectDTO();
        rejectDTO.setModeratorId(moderator.getId());
        rejectDTO.setStatus(ComplaintStatus.DUPLICATE);
        rejectDTO.setReason("some reason");

        MisprintReadDTO readDTO = misprintService.rejectModeration(m1.getId(), rejectDTO);

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

    @Ignore
    @Test
    public void testCloseSimilarMisprints() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        String textBeforeUpdate = "simply dummy text";
        String expectedText = "simply REPLACED_TEXT text";

        Person person = testObjectFactory.createPerson();
        person.setBio(textBeforeUpdate);
        personRepository.save(person);

        Misprint m1 = testObjectFactory.createMisprint(person.getId(), PERSON, user, "dummy");
        Misprint m2 = testObjectFactory.createMisprint(person.getId(), PERSON, user, "dummy");

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(moderator.getId());
        confirmDTO.setStartIndex(7);
        confirmDTO.setEndIndex(12);
        confirmDTO.setReplaceTo("REPLACED_TEXT");

        MisprintReadDTO readDTO = misprintService.confirmModeration(m1.getId(), confirmDTO);

        Assert.assertNotNull(readDTO.getModeratorId());
        Assert.assertNotNull(readDTO.getReplacedWith());
        Assert.assertNotNull(readDTO.getFixedAt());

        Misprint updatedMisprint1 = misprintRepository.findById(m1.getId()).get();
        Assert.assertEquals(moderator.getId(), updatedMisprint1.getModerator().getId());
        Assert.assertEquals(confirmDTO.getReplaceTo(), updatedMisprint1.getReplacedWith());

        Person updatedPerson = personRepository.findById(person.getId()).get();
        Assert.assertEquals(expectedText, updatedPerson.getBio());

        // check if m2 was closed
        Misprint updatedMisprint2 = misprintRepository.findById(m2.getId()).get();
        Assert.assertEquals(moderator.getId(), updatedMisprint2.getModerator().getId());
        Assert.assertEquals(confirmDTO.getReplaceTo(), updatedMisprint2.getReplacedWith());

        Assert.assertEquals(readDTO.getModeratorId(), updatedMisprint2.getModerator().getId());
        Assert.assertEquals(readDTO.getReplacedWith(), updatedMisprint2.getReplacedWith());
        Assert.assertEquals(readDTO.getFixedAt(), updatedMisprint2.getFixedAt());
    }
}
