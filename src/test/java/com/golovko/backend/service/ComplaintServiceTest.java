package com.golovko.backend.service;

import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.dto.ComplaintCreateDTO;
import com.golovko.backend.dto.ComplaintPatchDTO;
import com.golovko.backend.dto.ComplaintReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ComplaintRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = "delete from complaint", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ComplaintServiceTest {

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ComplaintRepository complaintRepository;

    private Complaint createReport() {
        Complaint complaint = new Complaint();
        complaint.setComplaintTitle("Some title");
        complaint.setComplaintText("Some report text");
        complaint.setComplaintType(ComplaintType.SPOILER);
        complaint = complaintRepository.save(complaint);
        return complaint;
    }

    @Test
    public void getReportTest() {
        Complaint complaint = createReport();
        ComplaintReadDTO readDTO = complaintService.getReport(complaint.getId());

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(complaint);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getReportWrongIdTest() {
        complaintService.getReport(UUID.randomUUID());
    }

    @Test
    public void createReportTest() {
        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("some title");
        createDTO.setComplaintText("some text");
        createDTO.setComplaintType(ComplaintType.SPOILER);

        ComplaintReadDTO readDTO = complaintService.createReport(createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Complaint complaint = complaintRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(complaint);
    }

    @Test
    public void patchReportTest() {
        Complaint complaint = createReport();

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("another title");
        patchDTO.setComplaintText("another text");
        patchDTO.setComplaintType(ComplaintType.CHILD_ABUSE);

        ComplaintReadDTO readDTO = complaintService.patchReport(complaint.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        complaint = complaintRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(complaint).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void patchReportEmptyPatchTest() {
        Complaint complaint = createReport();

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        ComplaintReadDTO readDTO = complaintService.patchReport(complaint.getId(), patchDTO);

        Assert.assertNotNull(readDTO.getComplaintTitle());
        Assert.assertNotNull(readDTO.getComplaintText());
        Assert.assertNotNull(readDTO.getComplaintType());

        Complaint complaintAfterUpdate = complaintRepository.findById(readDTO.getId()).get();

        Assert.assertNotNull(complaintAfterUpdate.getComplaintTitle());
        Assert.assertNotNull(complaintAfterUpdate.getComplaintText());
        Assert.assertNotNull(complaintAfterUpdate.getComplaintType());

        Assertions.assertThat(complaint).isEqualToComparingFieldByField(complaintAfterUpdate);
    }

    @Test
    public void deleteReportTest() {
        Complaint complaint = createReport();
        complaintService.deleteReport(complaint.getId());

        Assert.assertFalse(complaintRepository.existsById(complaint.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteReportNotFound() {
        complaintService.deleteReport(UUID.randomUUID());
    }
}
