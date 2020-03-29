package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.complaint.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ComplaintRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public PageResult<ComplaintReadDTO> getAllComplaints(ComplaintFilter filter, Pageable pageable) {
        Page<Complaint> complaints = complaintRepository.findByFilter(filter, pageable);

        return translationService.toPageResult(complaints, ComplaintReadDTO.class);
    }

    public ComplaintReadDTO getComplaint(UUID userId, UUID id) {
        Complaint complaint = getComplaintByUserId(id, userId);
        return translationService.translate(complaint, ComplaintReadDTO.class);
    }

    public PageResult<ComplaintReadDTO> getUserComplaints(UUID userId, Pageable pageable) {
        Page<Complaint> complaints = complaintRepository.findByAuthorId(userId, pageable);

        return translationService.toPageResult(complaints, ComplaintReadDTO.class);
    }

    public ComplaintReadDTO createComplaint(UUID userId, ComplaintCreateDTO createDTO) {
        Complaint complaint = translationService.translate(createDTO, Complaint.class);

        complaint.setComplaintStatus(ComplaintStatus.INITIATED);
        complaint.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, userId));
        complaint = complaintRepository.save(complaint);

        return translationService.translate(complaint, ComplaintReadDTO.class);
    }

    public ComplaintReadDTO patchComplaint(UUID userId, UUID id, ComplaintPatchDTO patchDTO) {
        Complaint complaint = getComplaintByUserId(id, userId);

        translationService.map(patchDTO, complaint);
        complaint = complaintRepository.save(complaint);

        return translationService.translate(complaint, ComplaintReadDTO.class);
    }

    public ComplaintReadDTO updateComplaint(UUID userId, UUID id, ComplaintPutDTO updateDTO) {
        Complaint complaint = getComplaintByUserId(id, userId);

        translationService.map(updateDTO, complaint);
        complaint = complaintRepository.save(complaint);

        return translationService.translate(complaint, ComplaintReadDTO.class);
    }

    public void deleteComplaint(UUID userId, UUID id) {
        complaintRepository.delete(getComplaintByUserId(id, userId));
    }

    @Transactional
    public ComplaintReadDTO moderateComplaint(UUID complaintId, ComplaintModerateDTO dto) {
        Complaint complaint = repoHelper.getReferenceIfExist(Complaint.class, complaintId);

        complaint.setModerator(repoHelper.getReferenceIfExist(ApplicationUser.class, dto.getModeratorId()));
        complaint.setComplaintStatus(dto.getComplaintStatus());
        complaint = complaintRepository.save(complaint);

        return translationService.translate(complaint, ComplaintReadDTO.class);
    }

    private Complaint getComplaintByUserId(UUID id, UUID userId) {
        return Optional.ofNullable(complaintRepository.findByIdAndAuthorId(id, userId))
                .orElseThrow(() -> new EntityNotFoundException(Complaint.class, id, userId));
    }
}
