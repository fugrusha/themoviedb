package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintType;
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
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from complaint", "delete from application_user"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ComplaintRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Test
    public void getComplaintsByUser() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1, ComplaintType.CHILD_ABUSE);
        Complaint c2 = testObjectFactory.createComplaint(user1, ComplaintType.CHILD_ABUSE);
        testObjectFactory.createComplaint(user2, ComplaintType.CHILD_ABUSE);
        testObjectFactory.createComplaint(user2, ComplaintType.MISPRINT);

        List<Complaint> result = complaintRepository
                .findByAuthorIdOrderByCreatedAtAsc(user1.getId());

        Assertions.assertThat(result).extracting(Complaint::getId).isEqualTo(Arrays.asList(c1.getId(), c2.getId()));
    }

    @Test
    public void testCreateAtIsSet() {
        ApplicationUser author = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(author, ComplaintType.MISPRINT);

        Instant createdAtBeforeReload = complaint.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        complaint = complaintRepository.findById(complaint.getId()).get();

        Instant createdAtAfterReload = complaint.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testModifiedAtIsSet() {
        ApplicationUser author = testObjectFactory.createUser();

        Complaint complaint = testObjectFactory.createComplaint(author, ComplaintType.MISPRINT);

        Instant modifiedAtBeforeReload = complaint.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        complaint = complaintRepository.findById(complaint.getId()).get();
        complaint.setComplaintType(ComplaintType.CHILD_ABUSE);
        complaint = complaintRepository.save(complaint);
        Instant modifiedAtAfterReload = complaint.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }
}
