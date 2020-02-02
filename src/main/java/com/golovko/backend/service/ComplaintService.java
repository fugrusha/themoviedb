package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TranslationService translationService;

    public ComplaintReadDTO getComplaint(UUID id) {
        Complaint complaint = getComplaintRequired(id);
        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO createComplaint(ComplaintCreateDTO createDTO, ApplicationUser author) {
        Complaint complaint = translationService.toEntity(createDTO);
        complaint.setAuthor(author);

        complaint = complaintRepository.save(complaint);
        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO patchComplaint(UUID id, ComplaintPatchDTO patchDTO) {
        Complaint complaint = getComplaintRequired(id);

        translationService.patchEntity(patchDTO, complaint);

        complaint = complaintRepository.save(complaint);

        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO updateComplaint(UUID id, ComplaintPutDTO updateDTO) {
        Complaint complaint = getComplaintRequired(id);

        translationService.updateEntity(updateDTO, complaint);

        complaint = complaintRepository.save(complaint);

        return translationService.toRead(complaint);
    }

    public void deleteComplaint(UUID id) {
        complaintRepository.delete(getComplaintRequired(id));
    }

    private Complaint getComplaintRequired(UUID id) {
        return complaintRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(Complaint.class, id)
        );
    }
}
