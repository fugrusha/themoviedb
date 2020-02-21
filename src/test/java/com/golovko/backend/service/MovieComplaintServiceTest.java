package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.ParentType;
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

import java.util.List;
import java.util.UUID;

import static com.golovko.backend.domain.ComplaintType.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = { "delete from complaint", "delete from movie", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieComplaintServiceTest {

    @Autowired
    private MovieComplaintService movieComplaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Test
    public void getMovieComplaintTest() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Complaint complaint = testObjectFactory
                .createMovieComplaint(movie.getId(), user, CHILD_ABUSE, moderator);

        ComplaintReadDTO readDTO = movieComplaintService.getMovieComplaint(movie.getId(), complaint.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), user.getId());
        Assert.assertEquals(readDTO.getModeratorId(), moderator.getId());
    }

    @Test
    public void getListOfMovieComplaintsTest() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();

        Complaint c1 = testObjectFactory.createMovieComplaint(movie1.getId(), user, CHILD_ABUSE, moderator);
        Complaint c2 = testObjectFactory.createMovieComplaint(movie1.getId(), user, SPAM, moderator);
        Complaint c3 = testObjectFactory.createMovieComplaint(movie1.getId(), user, SPOILER, moderator);

        testObjectFactory.createMovieComplaint(movie2.getId(), user, VIOLENCE, moderator);
        testObjectFactory.createMovieComplaint(movie2.getId(), user, SPAM, moderator);
        testObjectFactory.createMovieComplaint(movie2.getId(), user, OTHER, moderator);

        List<ComplaintReadDTO> complaints = movieComplaintService.getMovieComplaints(movie1.getId());

        Assertions.assertThat(complaints).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());
    }

    @Test
    public void createMovieComplaintTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(SPAM);

        ComplaintReadDTO readDTO = movieComplaintService.createMovieComplaint(movie.getId(), createDTO, user);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository
                .findByIdAndParentId(readDTO.getId(), readDTO.getParentId(), ParentType.MOVIE);

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getMovieComplaintWrongIdTest() {
        movieComplaintService.getMovieComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void patchMovieComplaintTest() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Complaint complaint = testObjectFactory
                .createMovieComplaint(movie.getId(), user, CHILD_ABUSE, moderator);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("another title");
        patchDTO.setComplaintText("another text");
        patchDTO.setComplaintType(CHILD_ABUSE);

        ComplaintReadDTO readDTO = movieComplaintService
                .patchMovieComplaint(movie.getId(), complaint.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToIgnoringGivenFields(readDTO,
                "moderatorId", "authorId");

        complaint = complaintRepository.findByIdAndParentId(readDTO.getId(), readDTO.getParentId(), ParentType.MOVIE);
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), user.getId());
        Assert.assertEquals(readDTO.getModeratorId(), moderator.getId());
    }

    @Test
    public void patchMovieComplaintEmptyPatchTest() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Complaint complaint = testObjectFactory
                .createMovieComplaint(movie.getId(), user, CHILD_ABUSE, moderator);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();

        ComplaintReadDTO readDTO = movieComplaintService
                .patchMovieComplaint(movie.getId(), complaint.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        Complaint complaintAfterUpdate = complaintRepository
                .findByIdAndParentId(readDTO.getId(), readDTO.getParentId(), ParentType.MOVIE);

        Assertions.assertThat(complaintAfterUpdate).hasNoNullFieldsOrProperties();
        Assertions.assertThat(complaintAfterUpdate).isEqualToIgnoringGivenFields(complaint,
                "author", "moderator");
        Assert.assertEquals(readDTO.getAuthorId(), complaintAfterUpdate.getAuthor().getId());
        Assert.assertEquals(readDTO.getModeratorId(), complaintAfterUpdate.getModerator().getId());
    }

    @Test
    public void updateMovieComplaintTest() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Complaint complaint = testObjectFactory
                .createMovieComplaint(movie.getId(), user, CHILD_ABUSE, moderator);

        ComplaintPutDTO updateDTO = new ComplaintPutDTO();
        updateDTO.setComplaintText("new text");
        updateDTO.setComplaintTitle("new title");
        updateDTO.setComplaintType(CHILD_ABUSE);

        ComplaintReadDTO readDTO = movieComplaintService
                .updateMovieComplaint(movie.getId(), complaint.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToIgnoringGivenFields(readDTO,
                "moderatorId", "authorId");

        complaint = complaintRepository.findByIdAndParentId(readDTO.getId(), readDTO.getParentId(), ParentType.MOVIE);
        Assertions.assertThat(complaint).isEqualToIgnoringGivenFields(readDTO,
                "moderator", "author");
        Assert.assertEquals(readDTO.getAuthorId(), user.getId());
        Assert.assertEquals(readDTO.getModeratorId(), moderator.getId());
    }

    @Test
    public void deleteMovieComplaintTest() {
        ApplicationUser user = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Complaint complaint = testObjectFactory
                .createMovieComplaint(movie.getId(), user, CHILD_ABUSE, moderator);

        movieComplaintService.deleteMovieComplaint(movie.getId(), complaint.getId());

        Assert.assertFalse(complaintRepository.existsById(complaint.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteMovieComplaintNotFound() {
        movieComplaintService.deleteMovieComplaint(UUID.randomUUID(), UUID.randomUUID());
    }
}
