package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

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

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void getMovieComplaintTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Complaint complaint = testObjectFactory.createMovieComplaint(movie.getId(), user, ComplaintType.CHILD_ABUSE);

        ComplaintReadDTO readDTO = movieComplaintService.getMovieComplaint(movie.getId(), complaint.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test
    public void getListOfMovieComplaintsTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();
        Complaint c1 = testObjectFactory.createMovieComplaint(movie1.getId(), user, ComplaintType.CHILD_ABUSE);
        Complaint c2 = testObjectFactory.createMovieComplaint(movie1.getId(), user, ComplaintType.SPAM);
        Complaint c3 = testObjectFactory.createMovieComplaint(movie1.getId(), user, ComplaintType.SPOILER);
        testObjectFactory.createMovieComplaint(movie2.getId(), user, ComplaintType.VIOLENCE);
        testObjectFactory.createMovieComplaint(movie2.getId(), user, ComplaintType.SPAM);
        testObjectFactory.createMovieComplaint(movie2.getId(), user, ComplaintType.OTHER);

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
        createDTO.setComplaintType(ComplaintType.SPAM);

        ComplaintReadDTO readDTO = movieComplaintService.createMovieComplaint(movie.getId(), createDTO, user);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository.findByIdAndParentId(readDTO.getId(), readDTO.getParentId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getMovieComplaintWrongIdTest() {
        movieComplaintService.getMovieComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    private void inTransaction (Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}
