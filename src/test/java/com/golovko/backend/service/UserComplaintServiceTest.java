package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = {"delete from complaint", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserComplaintServiceTest {

    @Autowired
    private UserComplaintService userComplaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void getComplaintTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user, ComplaintType.CHILD_ABUSE);
        ComplaintReadDTO readDTO = userComplaintService.getComplaint(user.getId(), complaint.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test
    public void getListOfUserComplaintsTest() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1, ComplaintType.CHILD_ABUSE);
        Complaint c2 = testObjectFactory.createComplaint(user1, ComplaintType.MISPRINT);
        testObjectFactory.createComplaint(user2, ComplaintType.MISPRINT);
        testObjectFactory.createComplaint(user2, ComplaintType.SPAM);

        List<ComplaintReadDTO> complaints = userComplaintService.getUserComplaints(user1.getId());

        Assertions.assertThat(complaints).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getComplaintWrongIdTest() {
        userComplaintService.getComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void patchComplaintTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user, ComplaintType.CHILD_ABUSE);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("another title");
        patchDTO.setComplaintText("another text");
        patchDTO.setComplaintType(ComplaintType.CHILD_ABUSE);
        patchDTO.setComplaintStatus(ComplaintStatus.DUPLICATE);

        ComplaintReadDTO readDTO = userComplaintService.patchComplaint(user.getId(), complaint.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");

        complaint = complaintRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(complaint).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test
    public void patchComplaintEmptyPatchTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user, ComplaintType.CHILD_ABUSE);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();

        ComplaintReadDTO readDTO = userComplaintService.patchComplaint(user.getId(), complaint.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        inTransaction(() -> {
            Complaint complaintAfterUpdate = complaintRepository.findById(readDTO.getId()).get();

            Assertions.assertThat(complaintAfterUpdate).hasNoNullFieldsOrProperties();
            Assertions.assertThat(complaintAfterUpdate).isEqualToIgnoringGivenFields(complaint,
                    "author");
            Assert.assertEquals(readDTO.getAuthorId(), complaintAfterUpdate.getAuthor().getId());
        });
    }

    @Test
    public void updateComplaintTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user, ComplaintType.CHILD_ABUSE);

        ComplaintPutDTO updateDTO = new ComplaintPutDTO();
        updateDTO.setComplaintText("new text");
        updateDTO.setComplaintTitle("new title");
        updateDTO.setComplaintType(ComplaintType.CHILD_ABUSE);
        updateDTO.setComplaintStatus(ComplaintStatus.DUPLICATE);

        ComplaintReadDTO readDTO = userComplaintService.updateComplaint(user.getId(), complaint.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");

        complaint = complaintRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(complaint).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test
    public void deleteComplaintTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user, ComplaintType.CHILD_ABUSE);
        userComplaintService.deleteComplaint(user.getId(), complaint.getId());

        Assert.assertFalse(complaintRepository.existsById(complaint.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteComplaintNotFound() {
        userComplaintService.deleteComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    private void inTransaction (Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}
