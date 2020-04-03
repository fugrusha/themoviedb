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

import static com.golovko.backend.domain.ComplaintType.VIOLENCE;
import static com.golovko.backend.domain.TargetObjectType.MOVIE;

public class ComplaintRepositoryTest extends BaseTest {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Test
    public void testGetComplaintsByAuthorId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1);
        Complaint c2 = testObjectFactory.createComplaint(user1);
        testObjectFactory.createComplaint(user2);
        testObjectFactory.createComplaint(user2);

        Page<Complaint> result = complaintRepository
                .findByAuthorId(user1.getId(), Pageable.unpaged());

        Assertions.assertThat(result).extracting(Complaint::getId)
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintByIdAndAuthorId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1);
        testObjectFactory.createComplaint(user2);

        Complaint complaint = complaintRepository.findByIdAndAuthorId(c1.getId(), user1.getId());

        Assert.assertEquals(complaint.getId(), c1.getId());
    }

    @Test
    public void testGetComplaintByTargetId() {
        ApplicationUser author = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();

        Complaint expectedComplaint = testObjectFactory.createComplaint(m1.getId(), MOVIE, VIOLENCE, author);
        testObjectFactory.createComplaint(m2.getId(), MOVIE, VIOLENCE, author);

        Complaint complaint = complaintRepository.findByIdAndTargetId(expectedComplaint.getId(), m1.getId(), MOVIE);

        Assert.assertEquals(complaint.getId(), expectedComplaint.getId());
    }

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser author = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(author);

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
        Complaint complaint = testObjectFactory.createComplaint(author);

        Instant modifiedAtBeforeReload = complaint.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        complaint = complaintRepository.findById(complaint.getId()).get();
        complaint.setComplaintText("new text");
        Complaint updatedComplaint = complaintRepository.save(complaint);

        Instant modifiedAtAfterReload = updatedComplaint.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }
}
