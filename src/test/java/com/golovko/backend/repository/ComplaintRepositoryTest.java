package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.Movie;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

import static com.golovko.backend.domain.ComplaintType.CHILD_ABUSE;
import static com.golovko.backend.domain.ComplaintType.MISPRINT;
import static com.golovko.backend.domain.TargetObjectType.MOVIE;
import static com.golovko.backend.domain.TargetObjectType.PERSON;

public class ComplaintRepositoryTest extends BaseTest {

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

        Page<Complaint> result = complaintRepository
                .findByAuthorId(user1.getId(), Pageable.unpaged());

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
