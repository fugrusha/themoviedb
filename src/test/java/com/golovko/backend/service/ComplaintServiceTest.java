package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.complaint.*;
import com.golovko.backend.dto.moderator.ModeratorDTO;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.golovko.backend.domain.ComplaintType.*;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = {
        "delete from complaint",
        "delete from user_role",
        "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ComplaintServiceTest {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void testGetComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user, CHILD_ABUSE, TargetObjectType.PERSON);

        ComplaintReadDTO readDTO = complaintService.getComplaint(user.getId(), complaint.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test
    public void testGetAllUserComplaints() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1, CHILD_ABUSE, TargetObjectType.PERSON);
        Complaint c2 = testObjectFactory.createComplaint(user1, MISPRINT, TargetObjectType.PERSON);
        testObjectFactory.createComplaint(user2, MISPRINT, TargetObjectType.PERSON);
        testObjectFactory.createComplaint(user2, SPAM, TargetObjectType.PERSON);

        List<ComplaintReadDTO> complaints = complaintService.getUserComplaints(user1.getId());

        Assertions.assertThat(complaints).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetComplaintWrongId() {
        complaintService.getComplaint(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testCreateComplaint() {
        ApplicationUser author = testObjectFactory.createUser();
        Movie targetObject = testObjectFactory.createMovie();

        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);
        createDTO.setTargetObjectType(TargetObjectType.MOVIE);
        createDTO.setTargetObjectId(targetObject.getId());

        ComplaintReadDTO readDTO = complaintService.createComplaint(author.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository
                .findByIdAndTargetId(readDTO.getId(), readDTO.getTargetObjectId(), TargetObjectType.MOVIE);

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(complaint,
                "moderatorId", "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), complaint.getAuthor().getId());
    }

    @Test
    public void testPatchComplaint() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user, CHILD_ABUSE, TargetObjectType.PERSON);

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
        Complaint complaint = testObjectFactory.createComplaint(user, CHILD_ABUSE, TargetObjectType.PERSON);

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
        Complaint complaint = testObjectFactory.createComplaint(user, CHILD_ABUSE, TargetObjectType.PERSON);

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
        Complaint complaint = testObjectFactory.createComplaint(user, CHILD_ABUSE, TargetObjectType.PERSON);
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
        Complaint c1 = testObjectFactory.createComplaint(user, CHILD_ABUSE, TargetObjectType.PERSON);
        Complaint c2 = testObjectFactory.createComplaint(user, SPAM, TargetObjectType.COMMENT);
        Complaint c3 = testObjectFactory.createComplaint(user, VIOLENCE, TargetObjectType.MOVIE);

        ComplaintFilter filter = new ComplaintFilter();

        List<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter);

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());
    }

    @Test
    public void testGetComplaintsWithEmptySetsOfFilter() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user, CHILD_ABUSE, TargetObjectType.PERSON);
        Complaint c2 = testObjectFactory.createComplaint(user, SPAM, TargetObjectType.COMMENT);
        Complaint c3 = testObjectFactory.createComplaint(user, VIOLENCE, TargetObjectType.MOVIE);

        ComplaintFilter filter = new ComplaintFilter();
        filter.setComplaintTypes(new HashSet<ComplaintType>());
        filter.setStatuses(new HashSet<ComplaintStatus>());
        filter.setTargetObjectTypes(new HashSet<TargetObjectType>());

        List<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter);

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());
    }

    @Test
    public void testGetComplaintsByStatus() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user, CHILD_ABUSE, TargetObjectType.PERSON);
        Complaint c2 = testObjectFactory.createComplaint(user, SPAM, TargetObjectType.COMMENT);
        Complaint c3 = testObjectFactory.createComplaint(user, VIOLENCE, TargetObjectType.MOVIE);

        c1.setComplaintStatus(ComplaintStatus.DUPLICATE);
        c2.setComplaintStatus(ComplaintStatus.CLOSED);
        c3.setComplaintStatus(ComplaintStatus.DUPLICATE);
        complaintRepository.saveAll(List.of(c1, c2, c3));

        ComplaintFilter filter = new ComplaintFilter();
        filter.setStatuses(Set.of(ComplaintStatus.DUPLICATE));

        List<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter);

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c3.getId());
    }

    @Test
    public void testGetComplaintsByAuthor() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        ApplicationUser user3 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1, CHILD_ABUSE, TargetObjectType.PERSON);
        Complaint c2 = testObjectFactory.createComplaint(user1, SPAM, TargetObjectType.COMMENT);
        testObjectFactory.createComplaint(user2, VIOLENCE, TargetObjectType.MOVIE);
        testObjectFactory.createComplaint(user3, VIOLENCE, TargetObjectType.MOVIE);
        testObjectFactory.createComplaint(user3, VIOLENCE, TargetObjectType.MOVIE);

        ComplaintFilter filter = new ComplaintFilter();
        filter.setAuthorId(user1.getId());

        List<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter);

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintsByModerator() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser moderator1 = testObjectFactory.createUser();
        ApplicationUser moderator2 = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(user1, CHILD_ABUSE, TargetObjectType.PERSON);
        Complaint c2 = testObjectFactory.createComplaint(user1, SPAM, TargetObjectType.COMMENT);
        Complaint c3 = testObjectFactory.createComplaint(user1, VIOLENCE, TargetObjectType.MOVIE);
        testObjectFactory.createComplaint(user1, VIOLENCE, TargetObjectType.MOVIE); // without moderator

        c1.setModerator(moderator1);
        c2.setModerator(moderator1);
        c3.setModerator(moderator2);
        complaintRepository.saveAll(List.of(c1, c2, c3));

        ComplaintFilter filter = new ComplaintFilter();
        filter.setModeratorId(moderator1.getId());

        List<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter);

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintsByComplaintType() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();

        Complaint c1 = testObjectFactory.createComplaint(user2, SPAM, TargetObjectType.COMMENT);
        Complaint c2 = testObjectFactory.createComplaint(user1, VIOLENCE, TargetObjectType.MOVIE);
        testObjectFactory.createComplaint(user2, MISPRINT, TargetObjectType.MOVIE);
        testObjectFactory.createComplaint(user1, CHILD_ABUSE, TargetObjectType.PERSON);

        ComplaintFilter filter = new ComplaintFilter();
        filter.setComplaintTypes(Set.of(SPAM, VIOLENCE));

        List<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter);

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintsByTargetObjectType() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();

        Complaint c1 = testObjectFactory.createComplaint(user2, SPAM, TargetObjectType.COMMENT);
        Complaint c2 = testObjectFactory.createComplaint(user1, VIOLENCE, TargetObjectType.MOVIE);
        testObjectFactory.createComplaint(user2, MISPRINT, TargetObjectType.PERSON);
        testObjectFactory.createComplaint(user1, CHILD_ABUSE, TargetObjectType.MOVIE_CAST);

        ComplaintFilter filter = new ComplaintFilter();
        filter.setTargetObjectTypes(Set.of(TargetObjectType.COMMENT, TargetObjectType.MOVIE));

        List<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter);

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetComplaintsByAllFilters() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        ApplicationUser moder1 = testObjectFactory.createUser();

        Complaint c1 = testObjectFactory.createComplaint(user2, SPAM, TargetObjectType.MOVIE);
        c1.setModerator(moder1);
        c1.setComplaintStatus(ComplaintStatus.CLOSED);

        Complaint c2 = testObjectFactory.createComplaint(user2, SPAM, TargetObjectType.MOVIE);
        c2.setModerator(moder1);
        c2.setComplaintStatus(ComplaintStatus.DUPLICATE); // wrong status


        Complaint c3 = testObjectFactory.createComplaint(user1, SPAM, TargetObjectType.MOVIE);  // wrong author
        c3.setModerator(moder1);
        c3.setComplaintStatus(ComplaintStatus.CLOSED);

        testObjectFactory.createComplaint(user2, VIOLENCE, TargetObjectType.COMMENT); // wrong complaintType
        testObjectFactory.createComplaint(user2, SPAM, TargetObjectType.MOVIE); // without moderator

        complaintRepository.saveAll(List.of(c1, c2, c3));

        ComplaintFilter filter = new ComplaintFilter();
        filter.setAuthorId(user2.getId());
        filter.setModeratorId(moder1.getId());
        filter.setStatuses(Set.of(ComplaintStatus.CLOSED));
        filter.setComplaintTypes(Set.of(SPAM));
        filter.setTargetObjectTypes(Set.of(TargetObjectType.MOVIE));

        List<ComplaintReadDTO> actualResult = complaintService.getAllComplaints(filter);

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(c1.getId());
    }

    @Test
    public void testTakeComplaintForModeration() {
        ApplicationUser author = testObjectFactory.createUser();
        ApplicationUser moderator = testObjectFactory.createUser();

        ModeratorDTO moderDTO = new ModeratorDTO();
        moderDTO.setModeratorId(moderator.getId());

        Complaint c1 = testObjectFactory.createComplaint(author, SPAM, TargetObjectType.MOVIE);

        ComplaintReadDTO actualResult = complaintService.takeForModeration(c1.getId(), moderDTO);

        Assertions.assertThat(actualResult).hasNoNullFieldsOrProperties();
        Assert.assertEquals(moderator.getId(), actualResult.getModeratorId());
        Assert.assertEquals(actualResult.getComplaintStatus(), ComplaintStatus.UNDER_INVESTIGATION);

        c1 = complaintRepository.findById(c1.getId()).get();
        Assert.assertEquals(moderator.getId(), c1.getModerator().getId());
        Assert.assertEquals(c1.getComplaintStatus(), ComplaintStatus.UNDER_INVESTIGATION);
    }

    @Test
    public void testChangeComplaintStatus() {
        ModeratorDTO moderDTO = new ModeratorDTO();
        moderDTO.setComplaintStatus(ComplaintStatus.CLOSED);

        ApplicationUser author = testObjectFactory.createUser();
        Complaint c1 = testObjectFactory.createComplaint(author, SPAM, TargetObjectType.MOVIE);

        ComplaintReadDTO actualResult = complaintService.changeStatus(c1.getId(), moderDTO);

        Assertions.assertThat(actualResult).hasNoNullFieldsOrPropertiesExcept("moderatorId");
        Assert.assertEquals(actualResult.getComplaintStatus(), ComplaintStatus.CLOSED);


        Complaint updatedComplaint = complaintRepository.findById(c1.getId()).get();
        Assert.assertEquals(updatedComplaint.getComplaintStatus(), ComplaintStatus.CLOSED);
    }

    private void inTransaction (Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}
