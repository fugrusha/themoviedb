package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.dto.complaint.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ComplaintRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public List<ComplaintReadDTO> getAllComplaints(ComplaintFilter filter) {
        List<Complaint> complaints = complaintRepository.findByFilter(filter);
        return complaints.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public ComplaintReadDTO getComplaint(UUID userId, UUID id) {
        Complaint complaint = getComplaintByUserId(id, userId);
        return translationService.toRead(complaint);
    }

    public List<ComplaintReadDTO> getUserComplaints(UUID userId) {
        List<Complaint> complaints = complaintRepository.findByAuthorIdOrderByCreatedAtAsc(userId);
        return complaints.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public ComplaintReadDTO createComplaint(UUID userId, ComplaintCreateDTO createDTO) {
        Complaint complaint = translationService.toEntity(createDTO);

        complaint.setComplaintStatus(ComplaintStatus.INITIATED);
        complaint.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, userId));

        complaint = complaintRepository.save(complaint);
        return translationService.toRead(complaint);
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
        Complaint complaint = complaintRepository.findByIdAndAuthorId(id, userId);

        if (complaint != null) {
            return complaint;
        } else {
            throw new EntityNotFoundException(Complaint.class, id, ApplicationUser.class, userId);
        }
    }
}
