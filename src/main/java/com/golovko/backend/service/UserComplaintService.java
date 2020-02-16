package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TranslationService translationService;

    public ComplaintReadDTO getComplaint(UUID userId, UUID id) {
        Complaint complaint = getComplaintByUserId(id, userId);
        return translationService.toRead(complaint);
    }

    public List<ComplaintReadDTO> getUserComplaints(UUID userId) {
        List<Complaint> complaints = complaintRepository.findByAuthorIdOrderByCreatedAtAsc(userId);
        return complaints.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public ComplaintReadDTO patchComplaint(UUID userId, UUID id, ComplaintPatchDTO patchDTO) {
        Complaint complaint = getComplaintByUserId(id, userId);

        translationService.patchEntity(patchDTO, complaint);
        complaint = complaintRepository.save(complaint);

        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO updateComplaint(UUID userId, UUID id, ComplaintPutDTO updateDTO) {
        Complaint complaint = getComplaintByUserId(id, userId);

        translationService.updateEntity(updateDTO, complaint);
        complaint = complaintRepository.save(complaint);

        return translationService.toRead(complaint);
    }

    public void deleteComplaint(UUID userId, UUID id) {
        complaintRepository.delete(getComplaintByUserId(id, userId));
    }

    private Complaint getComplaintByUserId(UUID id, UUID userId) {
        if (complaintRepository.findByIdAndAuthorId(id, userId) != null) {
            return complaintRepository.findByIdAndAuthorId(id, userId);
        } else {
            throw new EntityNotFoundException(Complaint.class, id, ApplicationUser.class, userId);
        }
    }
}
