package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.complaint.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.ComplaintRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionSystemException;

import java.util.*;

import static com.golovko.backend.domain.ComplaintType.*;
import static com.golovko.backend.domain.TargetObjectType.*;

public class ComplaintServiceTest extends BaseTest {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Test
    public void testGetComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user);

        ComplaintReadDTO readDTO = complaintService.getComplaint(user.getId(), complaint.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test
    public void testGetAllUserComplaints() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1);
        Complaint c2 = testObjectFactory.createComplaint(user1);
        testObjectFactory.createComplaint(user2);
        testObjectFactory.createComplaint(user2);

        PageResult<ComplaintReadDTO> complaints = complaintService
                .getUserComplaints(user1.getId(), Pageable.unpaged());

        Assertions.assertThat(complaints.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintsWithPagingAndSorting() {
        ApplicationUser user1 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1);
        Complaint c2 = testObjectFactory.createComplaint(user1);
        testObjectFactory.createComplaint(user1);
        testObjectFactory.createComplaint(user1);

        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "createdAt"));

        PageResult<ComplaintReadDTO> complaints = complaintService
                .getUserComplaints(user1.getId(), pageRequest);

        Assertions.assertThat(complaints.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetComplaintWrongId() {
        complaintService.getComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testCreateComplaintForMovie() {
        ApplicationUser author = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(MOVIE);
        createDTO.setTargetObjectId(movie.getId());

        ComplaintReadDTO readDTO = complaintService.createComplaint(author.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
        Assert.assertEquals(movie.getId(), complaint.getTargetObjectId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateComplaintForMovieWrongMovieId() {
        ApplicationUser author = testObjectFactory.createUser();
        UUID wrongMovieId = UUID.randomUUID();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(MOVIE);
        createDTO.setTargetObjectId(wrongMovieId);

        complaintService.createComplaint(author.getId(), createDTO);
    }

    @Test
    public void testCreateComplaintForMovieCast() {
        ApplicationUser author = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(MOVIE_CAST);
        createDTO.setTargetObjectId(movieCast.getId());

        ComplaintReadDTO readDTO = complaintService.createComplaint(author.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
        Assert.assertEquals(movieCast.getId(), complaint.getTargetObjectId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateComplaintForMovieCastWrongMovieCastId() {
        ApplicationUser author = testObjectFactory.createUser();
        UUID wrongMovieCastId = UUID.randomUUID();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(MOVIE_CAST);
        createDTO.setTargetObjectId(wrongMovieCastId);

        complaintService.createComplaint(author.getId(), createDTO);
    }

    @Test
    public void testCreateComplaintForMovieCrew() {
        ApplicationUser author = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(MOVIE_CREW);
        createDTO.setTargetObjectId(movieCrew.getId());

        ComplaintReadDTO readDTO = complaintService.createComplaint(author.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
        Assert.assertEquals(movieCrew.getId(), complaint.getTargetObjectId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateComplaintForMovieCrewWrongMovieCrewId() {
        ApplicationUser author = testObjectFactory.createUser();
        UUID wrongMovieCrewId = UUID.randomUUID();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(MOVIE_CREW);
        createDTO.setTargetObjectId(wrongMovieCrewId);

        complaintService.createComplaint(author.getId(), createDTO);
    }

    @Test
    public void testCreateComplaintForArticle() {
        ApplicationUser author = testObjectFactory.createUser();
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(ARTICLE);
        createDTO.setTargetObjectId(article.getId());

        ComplaintReadDTO readDTO = complaintService.createComplaint(author.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
        Assert.assertEquals(article.getId(), complaint.getTargetObjectId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateComplaintForArticleWrongArticleId() {
        ApplicationUser author = testObjectFactory.createUser();
        UUID wrongArticleId = UUID.randomUUID();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(ARTICLE);
        createDTO.setTargetObjectId(wrongArticleId);

        complaintService.createComplaint(author.getId(), createDTO);
    }

    @Test
    public void testCreateComplaintForComment() {
        ApplicationUser author = testObjectFactory.createUser();
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);
        Comment c1 = testObjectFactory.createComment(articleAuthor, article.getId(), CommentStatus.APPROVED, ARTICLE);

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(COMMENT);
        createDTO.setTargetObjectId(c1.getId());

        ComplaintReadDTO readDTO = complaintService.createComplaint(author.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
        Assert.assertEquals(c1.getId(), complaint.getTargetObjectId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateComplaintForCommentWrongCommentId() {
        ApplicationUser author = testObjectFactory.createUser();
        UUID wrongCommentId = UUID.randomUUID();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(COMMENT);
        createDTO.setTargetObjectId(wrongCommentId);

        complaintService.createComplaint(author.getId(), createDTO);
    }

    @Test
    public void testCreateComplaintForPerson() {
        ApplicationUser author = testObjectFactory.createUser();
        Person person = testObjectFactory.createPerson();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(PERSON);
        createDTO.setTargetObjectId(person.getId());

        ComplaintReadDTO readDTO = complaintService.createComplaint(author.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
        Assert.assertEquals(person.getId(), complaint.getTargetObjectId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateComplaintForPersonWrongPersonId() {
        ApplicationUser author = testObjectFactory.createUser();
        UUID wrongPersonId = UUID.randomUUID();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(PERSON);
        createDTO.setTargetObjectId(wrongPersonId);

        complaintService.createComplaint(author.getId(), createDTO);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateComplaintWrongAuthor() {
        Movie movie = testObjectFactory.createMovie();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(MOVIE);
        createDTO.setTargetObjectId(movie.getId());

        complaintService.createComplaint(UUID.randomUUID(), createDTO);
    }

    @Test
    public void testPatchComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("another title");
        patchDTO.setComplaintText("another text");
        patchDTO.setComplaintType(CHILD_ABUSE);

        ComplaintReadDTO readDTO = complaintService.patchComplaint(user.getId(), complaint.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToIgnoringGivenFields(readDTO,
                "moderatorId", "authorId");

        complaint = complaintRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(complaint).isEqualToIgnoringGivenFields(readDTO,
                "moderator", "author");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test
    public void testPatchComplaintEmptyPatch() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();

        ComplaintReadDTO readDTO = complaintService.patchComplaint(user.getId(), complaint.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrPropertiesExcept("moderatorId");

        inTransaction(() -> {
            Complaint complaintAfterUpdate = complaintRepository.findById(readDTO.getId()).get();

            Assertions.assertThat(complaintAfterUpdate).hasNoNullFieldsOrPropertiesExcept("moderator");
            Assertions.assertThat(complaintAfterUpdate).isEqualToIgnoringGivenFields(complaint,
                    "author", "moderator");
            Assert.assertEquals(readDTO.getAuthorId(), complaintAfterUpdate.getAuthor().getId());
        });
    }

    @Test
    public void testUpdateComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user);

        ComplaintPutDTO updateDTO = new ComplaintPutDTO();
        updateDTO.setComplaintText("new text");
        updateDTO.setComplaintTitle("new title");
        updateDTO.setComplaintType(CHILD_ABUSE);

        ComplaintReadDTO readDTO = complaintService.updateComplaint(user.getId(), complaint.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToIgnoringGivenFields(readDTO,
                "moderatorId", "authorId");

        complaint = complaintRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(complaint).isEqualToIgnoringGivenFields(readDTO,
                "moderator", "author");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test
    public void testDeleteComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user);
        complaintService.deleteComplaint(user.getId(), complaint.getId());

        Assert.assertFalse(complaintRepository.existsById(complaint.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteComplaintNotFound() {
        complaintService.deleteComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testGetComplaintsWithEmptyFilter() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user);
        Complaint c2 = testObjectFactory.createComplaint(user);
        Complaint c3 = testObjectFactory.createComplaint(user);

        ComplaintFilter filter = new ComplaintFilter();

        PageResult<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());
    }

    @Test
    public void testGetComplaintsWithEmptySetsOfFilter() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user);
        Complaint c2 = testObjectFactory.createComplaint(user);
        Complaint c3 = testObjectFactory.createComplaint(user);

        ComplaintFilter filter = new ComplaintFilter();
        filter.setComplaintTypes(new HashSet<ComplaintType>());
        filter.setStatuses(new HashSet<ComplaintStatus>());
        filter.setTargetObjectTypes(new HashSet<TargetObjectType>());

        PageResult<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());
    }

    @Test
    public void testGetComplaintsByStatus() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user);
        Complaint c2 = testObjectFactory.createComplaint(user);
        Complaint c3 = testObjectFactory.createComplaint(user);

        c1.setComplaintStatus(ComplaintStatus.DUPLICATE);
        c2.setComplaintStatus(ComplaintStatus.CLOSED);
        c3.setComplaintStatus(ComplaintStatus.DUPLICATE);
        complaintRepository.saveAll(List.of(c1, c2, c3));

        ComplaintFilter filter = new ComplaintFilter();
        filter.setStatuses(Set.of(ComplaintStatus.DUPLICATE));

        PageResult<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c3.getId());
    }

    @Test
    public void testGetComplaintsByAuthor() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        ApplicationUser user3 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1);
        Complaint c2 = testObjectFactory.createComplaint(user1);
        testObjectFactory.createComplaint(user2);
        testObjectFactory.createComplaint(user3);
        testObjectFactory.createComplaint(user3);

        ComplaintFilter filter = new ComplaintFilter();
        filter.setAuthorId(user1.getId());

        PageResult<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintsByModerator() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser moderator1 = testObjectFactory.createUser();
        ApplicationUser moderator2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1);
        Complaint c2 = testObjectFactory.createComplaint(user1);
        Complaint c3 = testObjectFactory.createComplaint(user1);
        testObjectFactory.createComplaint(user1); // without moderator

        c1.setModerator(moderator1);
        c2.setModerator(moderator1);
        c3.setModerator(moderator2);
        complaintRepository.saveAll(List.of(c1, c2, c3));

        ComplaintFilter filter = new ComplaintFilter();
        filter.setModeratorId(moderator1.getId());

        PageResult<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintsByComplaintType() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();

        Complaint c1 = testObjectFactory.createComplaint(m1.getId(), MOVIE, SPAM, user2);
        Complaint c2 = testObjectFactory.createComplaint(m1.getId(), MOVIE, VIOLENCE, user1);
        testObjectFactory.createComplaint(m1.getId(), MOVIE, SPOILER, user2);
        testObjectFactory.createComplaint(m1.getId(), MOVIE, CHILD_ABUSE, user1);

        ComplaintFilter filter = new ComplaintFilter();
        filter.setComplaintTypes(Set.of(SPAM, VIOLENCE));

        PageResult<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintsByTargetObjectType() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Article a1 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);

        Complaint c1 = testObjectFactory.createComplaint(m1.getId(), MOVIE, SPOILER, user2);
        Complaint c2 = testObjectFactory.createComplaint(m1.getId(), MOVIE, SPOILER, user1);
        testObjectFactory.createComplaint(a1.getId(), ARTICLE, SPOILER, user2);
        testObjectFactory.createComplaint(a1.getId(), ARTICLE, CHILD_ABUSE, user1);

        ComplaintFilter filter = new ComplaintFilter();
        filter.setTargetObjectTypes(Set.of(MOVIE));

        PageResult<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintsByAllFilters() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        ApplicationUser moder1 = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();

        Complaint c1 = testObjectFactory.createComplaint(m1.getId(), MOVIE, SPAM, user2);
        c1.setModerator(moder1);
        c1.setComplaintStatus(ComplaintStatus.CLOSED);

        Complaint c2 = testObjectFactory.createComplaint(m1.getId(), MOVIE, SPAM, user2);
        c2.setModerator(moder1);
        c2.setComplaintStatus(ComplaintStatus.DUPLICATE); // wrong status


        Complaint c3 = testObjectFactory.createComplaint(m1.getId(), MOVIE, SPAM, user1);  // wrong author
        c3.setModerator(moder1);
        c3.setComplaintStatus(ComplaintStatus.CLOSED);

        testObjectFactory.createComplaint(m1.getId(), ARTICLE, SPAM, user2); // wrong complaintType
        testObjectFactory.createComplaint(m1.getId(), MOVIE, SPAM, user2); // without moderator

        complaintRepository.saveAll(List.of(c1, c2, c3));

        ComplaintFilter filter = new ComplaintFilter();
        filter.setAuthorId(user2.getId());
        filter.setModeratorId(moder1.getId());
        filter.setStatuses(Set.of(ComplaintStatus.CLOSED));
        filter.setComplaintTypes(Set.of(SPAM));
        filter.setTargetObjectTypes(Set.of(MOVIE));

        PageResult<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId());
    }

    @Test
    public void testModerateNotCommentComplaint() {
        ApplicationUser author = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(moderator.getId());
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);

        Complaint c1 = testObjectFactory.createComplaint(author);
        c1.setTargetObjectType(ARTICLE);   // not COMMENT
        complaintRepository.save(c1);

        ComplaintReadDTO actualResult = complaintService.moderateComplaint(c1.getId(), moderDTO);

        Assertions.assertThat(actualResult).hasNoNullFieldsOrProperties();
        Assert.assertEquals(moderator.getId(), actualResult.getModeratorId());
        Assert.assertEquals(actualResult.getComplaintStatus(), ComplaintStatus.UNDER_INVESTIGATION);

        c1 = complaintRepository.findById(c1.getId()).get();
        Assert.assertEquals(moderator.getId(), c1.getModerator().getId());
        Assert.assertEquals(c1.getComplaintStatus(), ComplaintStatus.UNDER_INVESTIGATION);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testModerateComplaintWrongComplaintId() {
        ApplicationUser moderator = testObjectFactory.createUser();

        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(moderator.getId());
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);

        UUID wrongComplaintId = UUID.randomUUID();

        complaintService.moderateComplaint(wrongComplaintId, moderDTO);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testModerateComplaintWrongModeratorId() {
        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(UUID.randomUUID()); // random id
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);

        ApplicationUser author = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(author);

        complaintService.moderateComplaint(c1.getId(), moderDTO);
    }

    @Test
    public void testDecreaseComplaintAuthorTrustLevelByOne() {
        ApplicationUser moderator = testObjectFactory.createUser();
        ApplicationUser author = testObjectFactory.createUser();
        author.setTrustLevel(9.0);
        applicationUserRepository.save(author);

        Complaint c1 = testObjectFactory.createComplaint(author);

        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(moderator.getId());
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);
        moderDTO.setDecreaseComplaintAuthorTrustLevelByOne(true);

        complaintService.moderateComplaint(c1.getId(), moderDTO);

        ApplicationUser updatedAuthor = applicationUserRepository.findById(author.getId()).get();
        Assert.assertEquals(8.0, updatedAuthor.getTrustLevel(), Double.MIN_NORMAL);
    }

    @Test
    public void testDecreaseComplaintAuthorTrustLevelByOneFalseValue() {
        ApplicationUser moderator = testObjectFactory.createUser();
        ApplicationUser author = testObjectFactory.createUser();
        author.setTrustLevel(9.0);
        applicationUserRepository.save(author);

        Complaint c1 = testObjectFactory.createComplaint(author);

        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(moderator.getId());
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);
        moderDTO.setDecreaseComplaintAuthorTrustLevelByOne(false);

        complaintService.moderateComplaint(c1.getId(), moderDTO);

        ApplicationUser updatedAuthor = applicationUserRepository.findById(author.getId()).get();
        Assert.assertEquals(9.0, updatedAuthor.getTrustLevel(), Double.MIN_NORMAL);
    }

    @Test
    public void testModerateCommentComplaintWithNewMessage() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Comment comment = testObjectFactory.createComment(commentAuthor, m1.getId(), CommentStatus.APPROVED, MOVIE);

        ApplicationUser moderator = testObjectFactory.createUser();
        ApplicationUser author = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(comment.getId(), COMMENT, SPAM, author);

        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(moderator.getId());
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);
        moderDTO.setNewCommentMessage("new text message");

        complaintService.moderateComplaint(c1.getId(), moderDTO);

        Comment updatedComment = commentRepository.findById(comment.getId()).get();
        Assert.assertEquals(moderDTO.getNewCommentMessage(), updatedComment.getMessage());
    }

    @Test
    public void testModerateCommentComplaintWithNewMessageEmptyString() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Comment comment = testObjectFactory.createComment(commentAuthor, m1.getId(), CommentStatus.APPROVED, MOVIE);

        ApplicationUser moderator = testObjectFactory.createUser();
        ApplicationUser author = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(comment.getId(), COMMENT, SPAM, author);

        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(moderator.getId());
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);
        moderDTO.setNewCommentMessage("  "); // empty string

        complaintService.moderateComplaint(c1.getId(), moderDTO);

        Comment updatedComment = commentRepository.findById(comment.getId()).get();
        Assert.assertEquals(comment.getMessage(), updatedComment.getMessage());
    }

    @Test
    public void testModerateCommentComplaintDeleteComment() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Comment comment = testObjectFactory.createComment(commentAuthor, m1.getId(), CommentStatus.APPROVED, MOVIE);

        ApplicationUser moderator = testObjectFactory.createUser();
        ApplicationUser author = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(comment.getId(), COMMENT, SPAM, author);

        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(moderator.getId());
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);
        moderDTO.setDeleteComment(true);

        complaintService.moderateComplaint(c1.getId(), moderDTO);

        Assert.assertFalse(commentRepository.existsById(comment.getId()));
    }

    @Test
    public void testModerateCommentComplaintDeleteCommentFalseValue() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Comment comment = testObjectFactory.createComment(commentAuthor, m1.getId(), CommentStatus.APPROVED, MOVIE);

        ApplicationUser moderator = testObjectFactory.createUser();
        ApplicationUser author = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(comment.getId(), COMMENT, SPAM, author);

        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(moderator.getId());
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);
        moderDTO.setDeleteComment(false);

        complaintService.moderateComplaint(c1.getId(), moderDTO);

        Assert.assertTrue(commentRepository.existsById(comment.getId()));
    }

    @Test
    public void testModerateCommentComplaintBlockCommentAuthor() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Comment comment = testObjectFactory.createComment(commentAuthor, m1.getId(), CommentStatus.APPROVED, MOVIE);

        ApplicationUser moderator = testObjectFactory.createUser();
        ApplicationUser author = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(comment.getId(), COMMENT, SPAM, author);

        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(moderator.getId());
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);
        moderDTO.setBlockCommentAuthor(true);

        complaintService.moderateComplaint(c1.getId(), moderDTO);

        ApplicationUser updatedCommentAuthor = applicationUserRepository.findById(commentAuthor.getId()).get();
        Assert.assertTrue(updatedCommentAuthor.getIsBlocked());
    }

    @Test
    public void testModerateCommentComplaintBlockCommentAuthorFalseValue() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Comment comment = testObjectFactory.createComment(commentAuthor, m1.getId(), CommentStatus.APPROVED, MOVIE);

        ApplicationUser moderator = testObjectFactory.createUser();
        ApplicationUser author = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(comment.getId(), COMMENT, SPAM, author);

        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(moderator.getId());
        moderDTO.setComplaintStatus(ComplaintStatus.UNDER_INVESTIGATION);
        moderDTO.setBlockCommentAuthor(false);

        complaintService.moderateComplaint(c1.getId(), moderDTO);

        ApplicationUser updatedCommentAuthor = applicationUserRepository.findById(commentAuthor.getId()).get();
        Assert.assertFalse(updatedCommentAuthor.getIsBlocked());
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveComplaintNotNullValidation() {
        Complaint complaint = new Complaint();
        complaintRepository.save(complaint);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveComplaintMaxSizeValidation() {
        ApplicationUser user = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(user);
        complaint.setComplaintTitle("very long title".repeat(100));
        complaint.setComplaintText("long description of issue".repeat(1000));
        complaintRepository.save(complaint);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveComplaintMinSizeValidation() {
        ApplicationUser user = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(user);
        complaint.setComplaintTitle("");
        complaint.setComplaintText("");
        complaintRepository.save(complaint);
    }

    @Test
    public void testGetComplaintsWithFilterWithPagingAndSorting() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();

        Complaint c1 = testObjectFactory.createComplaint(user2);
        Complaint c2 = testObjectFactory.createComplaint(user1);
        testObjectFactory.createComplaint(user2);
        testObjectFactory.createComplaint(user1);

        ComplaintFilter filter = new ComplaintFilter();
        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "createdAt"));

        Assertions.assertThat(complaintService.getAllComplaints(filter, pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(c1.getId(), c2.getId()));
    }
}
