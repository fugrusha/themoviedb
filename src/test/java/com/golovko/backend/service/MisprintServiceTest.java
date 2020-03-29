package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.misprint.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.EntityWrongStatusException;
import com.golovko.backend.repository.*;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static com.golovko.backend.domain.TargetObjectType.*;

public class MisprintServiceTest extends BaseTest {

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

        PageResult<MisprintReadDTO> misprints = misprintService
                .getAllUserMisprintComplaints(user1.getId(), Pageable.unpaged());

        Assertions.assertThat(misprints.getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetUserMisprintsWithPagingAndSorting() {
        ApplicationUser user1 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user1, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user1, "misprint");
        testObjectFactory.createMisprint(movie.getId(), MOVIE, user1, "misprint");
        testObjectFactory.createMisprint(movie.getId(), MOVIE, user1, "misprint");

        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "createdAt"));

        Assertions.assertThat(misprintService.getAllUserMisprintComplaints(user1.getId(), pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(m1.getId(), m2.getId()));
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

    @Test(expected = EntityNotFoundException.class)
    public void testCreateMisprintWrongUser() {
        Movie movie = testObjectFactory.createMovie();

        MisprintCreateDTO createDTO = new MisprintCreateDTO();
        createDTO.setMisprintText("misprint");
        createDTO.setReplaceTo("Text text text");
        createDTO.setTargetObjectType(MOVIE);
        createDTO.setTargetObjectId(movie.getId());

        misprintService.createMisprintComplaint(UUID.randomUUID(), createDTO);
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

        PageResult<MisprintReadDTO> expectedResult = misprintService
                .getMisprintsByTargetId(movie1.getId(), Pageable.unpaged());

        Assertions.assertThat(expectedResult.getData()).extracting("id")
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

    @Test(expected = EntityNotFoundException.class)
    public void testGetMisprintByTargetIdNotFound() {
        misprintService.getMisprintByTargetId(UUID.randomUUID(), UUID.randomUUID());
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

    @Test
    public void testCloseSimilarMisprints() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        String textBeforeUpdate = "simply dummy text";
        String expectedText = "simply REPLACED_TEXT text";

        Person person = testObjectFactory.createPerson();
        person.setBio(textBeforeUpdate);
        personRepository.save(person);

        Misprint m1 = testObjectFactory.createMisprint(person.getId(), PERSON, user1, "dummy");
        Misprint m2 = testObjectFactory.createMisprint(person.getId(), PERSON, user2, "dummy");

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(moderator.getId());
        confirmDTO.setStartIndex(7);
        confirmDTO.setEndIndex(12);
        confirmDTO.setReplaceTo("REPLACED_TEXT");
        confirmDTO.setTargetObjectId(person.getId());

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
    }

    @Test
    public void testGetMisprintsWithEmptyFilter() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");
        Misprint m3 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");

        MisprintFilter filter = new MisprintFilter();

        PageResult<MisprintReadDTO> actualResult = misprintService.getMisprintsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId(), m3.getId());
    }

    @Test
    public void testGetMisprintsWithEmptySetsOfFilter() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");
        Misprint m3 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");

        MisprintFilter filter = new MisprintFilter();
        filter.setStatuses(new HashSet<ComplaintStatus>());
        filter.setTargetObjectTypes(new HashSet<TargetObjectType>());

        PageResult<MisprintReadDTO> actualResult = misprintService.getMisprintsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId(), m3.getId());
    }

    @Test
    public void testGetMisprintsByStatus() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");
        Misprint m3 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user, "misprint");

        m1.setStatus(ComplaintStatus.DUPLICATE);
        m2.setStatus(ComplaintStatus.CLOSED);
        m3.setStatus(ComplaintStatus.DUPLICATE);
        misprintRepository.saveAll(List.of(m1, m2, m3));

        MisprintFilter filter = new MisprintFilter();
        filter.setStatuses(Set.of(ComplaintStatus.DUPLICATE));

        PageResult<MisprintReadDTO> actualResult = misprintService.getMisprintsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m3.getId());
    }

    @Test
    public void testGetMisprintsByAuthor() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        ApplicationUser user3 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user2, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user2, "misprint");
        testObjectFactory.createMisprint(article.getId(), ARTICLE, user3, "misprint");
        testObjectFactory.createMisprint(article.getId(), ARTICLE, user3, "misprint");

        MisprintFilter filter = new MisprintFilter();
        filter.setAuthorId(user2.getId());

        PageResult<MisprintReadDTO> actualResult = misprintService.getMisprintsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetMisprintsByModerator() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser moderator1 = testObjectFactory.createUser();
        ApplicationUser moderator2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint");
        Misprint m3 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint");
        testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint"); // without moderator

        m1.setModerator(moderator1);
        m2.setModerator(moderator1);
        m3.setModerator(moderator2);
        misprintRepository.saveAll(List.of(m1, m2, m3));

        MisprintFilter filter = new MisprintFilter();
        filter.setModeratorId(moderator1.getId());

        PageResult<MisprintReadDTO> actualResult = misprintService.getMisprintsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetMisprintsByTargetObjectType() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(person.getId(), PERSON, user2, "misprint");
        testObjectFactory.createMisprint(movie.getId(), MOVIE, user1, "misprint");

        MisprintFilter filter = new MisprintFilter();
        filter.setTargetObjectTypes(Set.of(PERSON, ARTICLE));

        PageResult<MisprintReadDTO> actualResult = misprintService.getMisprintsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetMisprintsByAllFilters() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        ApplicationUser moder1 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Person person = testObjectFactory.createPerson();

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint");
        m1.setModerator(moder1);

        Misprint m2 = testObjectFactory.createMisprint(person.getId(), PERSON, user1, "misprint");
        m2.setModerator(moder1);// wrong type

        Misprint m3 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user2, "misprint");
        m3.setModerator(moder1);  // wrong author

        Misprint m4 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint");
        m4.setStatus(ComplaintStatus.CLOSED); // wrong status

        testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint"); // without moderator

        misprintRepository.saveAll(List.of(m1, m2, m3, m4));

        MisprintFilter filter = new MisprintFilter();
        filter.setAuthorId(user1.getId());
        filter.setModeratorId(moder1.getId());
        filter.setStatuses(Set.of(ComplaintStatus.INITIATED));
        filter.setTargetObjectTypes(Set.of(ARTICLE));

        PageResult<MisprintReadDTO> actualResult = misprintService.getMisprintsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId());
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMisprintNotNullValidation() {
        Misprint misprint = new Misprint();
        misprintRepository.save(misprint);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMisprintMinSizeValidation() {
        ApplicationUser user1 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Misprint misprint = new Misprint();
        misprint.setMisprintText("");
        misprint.setReplaceTo("");
        misprint.setReplacedWith("");
        misprint.setReason("");
        misprint.setAuthor(user1);
        misprint.setStatus(ComplaintStatus.CLOSED);
        misprint.setTargetObjectId(movie.getId());
        misprint.setTargetObjectType(MOVIE);

        misprintRepository.save(misprint);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMisprintMaxSizeValidation() {
        ApplicationUser user1 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Misprint misprint = new Misprint();
        misprint.setMisprintText("long text".repeat(100));
        misprint.setReplaceTo("long text".repeat(100));
        misprint.setReplacedWith("long text".repeat(100));
        misprint.setReason("long text".repeat(100));
        misprint.setAuthor(user1);
        misprint.setStatus(ComplaintStatus.CLOSED);
        misprint.setTargetObjectId(movie.getId());
        misprint.setTargetObjectType(MOVIE);

        misprintRepository.save(misprint);
    }

    @Test
    public void testGetMisprintsWithFilterWithPagingAndSorting() {
        ApplicationUser user1 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);

        Misprint m1 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint");
        testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint");
        testObjectFactory.createMisprint(article.getId(), ARTICLE, user1, "misprint");

        MisprintFilter filter = new MisprintFilter();
        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "createdAt"));

        Assertions.assertThat(misprintService.getMisprintsByFilter(filter, pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(m1.getId(), m2.getId()));

    }
}
