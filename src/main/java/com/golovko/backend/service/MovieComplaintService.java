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

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MovieComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TranslationService translationService;

    public ComplaintReadDTO getMovieComplaint(UUID movieId, UUID id) {
        Complaint complaint = getComplaintByMovieId(id, movieId);
        return translationService.toRead(complaint);
    }

    public List<ComplaintReadDTO> getMovieComplaints(UUID movieId) {
        List<Complaint> complaints = complaintRepository.findByParentIdOrderByCreatedAtAsc(movieId);
        return complaints.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public ComplaintReadDTO createMovieComplaint(
            UUID movieId,
            ComplaintCreateDTO createDTO,
            ApplicationUser author
    ) {
        Complaint complaint = translationService.toEntity(createDTO, movieId, author);

        complaint = complaintRepository.save(complaint);
        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO patchMovieComplaint(UUID movieId, UUID id, ComplaintPatchDTO patchDTO) {
        return null; //TODO
    }

    public ComplaintReadDTO updateMovieComplaint(UUID movieId, UUID id, ComplaintPutDTO updateDTO) {
        return null; //TODO
    }

    public void deleteMovieComplaint(UUID movieId, UUID id) {
        //TODO
    }

    private Complaint getComplaintByMovieId(UUID id, UUID movieId) {
        if (complaintRepository.findByIdAndParentId(id, movieId) != null) {
            return complaintRepository.findByIdAndParentId(id, movieId);
        } else {
            throw new EntityNotFoundException(Complaint.class, id);
        }
    }
}
