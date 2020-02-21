package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ComplaintRepository;
import com.golovko.backend.repository.RepositoryHelper;
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

    @Autowired
    private RepositoryHelper repoHelper;

    public ComplaintReadDTO getMovieComplaint(UUID movieId, UUID id) {
        Complaint complaint = getComplaintByMovieId(id, movieId);
        return translationService.toRead(complaint);
    }

    public List<ComplaintReadDTO> getMovieComplaints(UUID movieId) {
        List<Complaint> complaints = complaintRepository.findAllByParent(movieId, ParentType.MOVIE);
        return complaints.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public ComplaintReadDTO createMovieComplaint(
            UUID movieId,
            ComplaintCreateDTO createDTO,
            ApplicationUser author
    ) {
        Complaint complaint = translationService.toEntity(createDTO);

        complaint.setComplaintStatus(ComplaintStatus.INITIATED);
        complaint.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, author.getId()));
        complaint.setParentType(ParentType.MOVIE);
        complaint.setParentId(movieId);

        complaint = complaintRepository.save(complaint);
        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO patchMovieComplaint(UUID movieId, UUID id, ComplaintPatchDTO patchDTO) {
        Complaint complaint = getComplaintByMovieId(id, movieId);

        translationService.patchEntity(patchDTO, complaint);
        complaint = complaintRepository.save(complaint);

        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO updateMovieComplaint(UUID movieId, UUID id, ComplaintPutDTO updateDTO) {
        Complaint complaint = getComplaintByMovieId(id, movieId);

        translationService.updateEntity(updateDTO, complaint);
        complaint = complaintRepository.save(complaint);

        return translationService.toRead(complaint);
    }

    public void deleteMovieComplaint(UUID movieId, UUID id) {
        complaintRepository.delete(getComplaintByMovieId(id, movieId));
    }

    private Complaint getComplaintByMovieId(UUID id, UUID movieId) {
        if (complaintRepository.findByIdAndParentId(id, movieId, ParentType.MOVIE) != null) {
            return complaintRepository.findByIdAndParentId(id, movieId, ParentType.MOVIE);
        } else {
            throw new EntityNotFoundException(Complaint.class, id, Movie.class, movieId);
        }
    }
}
