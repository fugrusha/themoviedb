package com.golovko.backend.service;

import com.golovko.backend.domain.Complaint;
import com.golovko.backend.dto.ComplaintCreateDTO;
import com.golovko.backend.dto.ComplaintPatchDTO;
import com.golovko.backend.dto.ComplaintReadDTO;
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

    public ComplaintReadDTO getReport(UUID id) {
        Complaint complaint = getReportRequired(id);
        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO createReport(ComplaintCreateDTO createDTO) {
        Complaint complaint = translationService.toEntity(createDTO);

        complaint = complaintRepository.save(complaint);
        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO patchReport(UUID id, ComplaintPatchDTO patchDTO) {
        Complaint complaint = getReportRequired(id);

        translationService.patchEntity(patchDTO, complaint);

        complaint = complaintRepository.save(complaint);

        return translationService.toRead(complaint);
    }

    public void deleteReport(UUID id) {
        complaintRepository.delete(getReportRequired(id));
    }

    private Complaint getReportRequired(UUID id) {
        return complaintRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(Complaint.class, id)
        );
    }
}
