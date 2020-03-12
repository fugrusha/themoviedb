package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.Movie;
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

import java.time.Instant;
import java.util.List;

import static com.golovko.backend.domain.ComplaintType.CHILD_ABUSE;
import static com.golovko.backend.domain.ComplaintType.MISPRINT;
import static com.golovko.backend.domain.TargetObjectType.MOVIE;
import static com.golovko.backend.domain.TargetObjectType.PERSON;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {
        "delete from movie",
        "delete from complaint",
        "delete from user_role",
        "delete from application_user"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ComplaintRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Test
    public void testGetComplaintsByAuthorId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1, CHILD_ABUSE, PERSON);
        Complaint c2 = testObjectFactory.createComplaint(user1, CHILD_ABUSE, PERSON);
        testObjectFactory.createComplaint(user2, CHILD_ABUSE, PERSON);
        testObjectFactory.createComplaint(user2, MISPRINT, PERSON);

        List<Complaint> result = complaintRepository.findByAuthorIdOrderByCreatedAtAsc(user1.getId());

        Assertions.assertThat(result).extracting(Complaint::getId)
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintByIdAndAuthorId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1, CHILD_ABUSE, PERSON);
        testObjectFactory.createComplaint(user2, CHILD_ABUSE, PERSON);

        Complaint complaint = complaintRepository.findByIdAndAuthorId(c1.getId(), user1.getId());

        Assert.assertEquals(complaint.getId(), c1.getId());
    }

    @Test
    public void testGetComplaintByTargetId() {
        ApplicationUser author = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();

        Complaint expectedComplaint = testObjectFactory.createComplaint(m1.getId(), MOVIE, author);
        testObjectFactory.createComplaint(m2.getId(), MOVIE, author);

        Complaint complaint = complaintRepository.findByIdAndTargetId(expectedComplaint.getId(), m1.getId(), MOVIE);

        Assert.assertEquals(complaint.getId(), expectedComplaint.getId());
    }

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser author = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(author, MISPRINT, PERSON);

        Instant createdAtBeforeReload = complaint.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        complaint = complaintRepository.findById(complaint.getId()).get();

        Instant createdAtAfterReload = complaint.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        ApplicationUser author = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(author, MISPRINT, PERSON);

        Instant modifiedAtBeforeReload = complaint.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        complaint = complaintRepository.findById(complaint.getId()).get();
        complaint.setComplaintType(CHILD_ABUSE);
        complaint = complaintRepository.save(complaint);
        Instant modifiedAtAfterReload = complaint.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }
}
