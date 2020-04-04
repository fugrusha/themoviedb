package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.complaint.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.WrongTargetObjectTypeException;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.CommentRepository;
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
    private CommentRepository commentRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

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

        validateTargetObject(createDTO);

        complaint.setComplaintStatus(ComplaintStatus.INITIATED);
        complaint.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, userId));
        complaint = complaintRepository.save(complaint);

        return translationService.translate(complaint, ComplaintReadDTO.class);
    }

    private void validateTargetObject(ComplaintCreateDTO createDTO) {
        switch (createDTO.getTargetObjectType()) {
          case MOVIE_CAST:
              repoHelper.getReferenceIfExist(MovieCast.class, createDTO.getTargetObjectId());
              break;
          case MOVIE_CREW:
              repoHelper.getReferenceIfExist(MovieCrew.class, createDTO.getTargetObjectId());
              break;
          case MOVIE:
              repoHelper.getReferenceIfExist(Movie.class, createDTO.getTargetObjectId());
              break;
          case ARTICLE:
              repoHelper.getReferenceIfExist(Article.class, createDTO.getTargetObjectId());
              break;
          case COMMENT:
              repoHelper.getReferenceIfExist(Comment.class, createDTO.getTargetObjectId());
              break;
          case PERSON:
              repoHelper.getReferenceIfExist(Person.class, createDTO.getTargetObjectId());
              break;
          default:
              throw new WrongTargetObjectTypeException(ActionType.CREATE_COMPLAINT, createDTO.getTargetObjectType());
        }
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

        if (complaint.getTargetObjectType().equals(TargetObjectType.COMMENT)) {
            moderateCommentComplaint(complaint.getTargetObjectId(), dto);
        }
        if (dto.getDecreaseComplaintAuthorTrustLevelByOne() != null &&
                dto.getDecreaseComplaintAuthorTrustLevelByOne()) {
            decreaseComplaintAuthorTrustLevelByOne(complaint.getAuthor());
        }

        complaint.setModerator(repoHelper.getReferenceIfExist(ApplicationUser.class, dto.getModeratorId()));
        complaint.setComplaintStatus(dto.getComplaintStatus());
        complaint = complaintRepository.save(complaint);

        return translationService.translate(complaint, ComplaintReadDTO.class);
    }

    private void decreaseComplaintAuthorTrustLevelByOne(ApplicationUser complaintAuthor) {
        Double trustLevel = complaintAuthor.getTrustLevel();
        complaintAuthor.setTrustLevel(trustLevel - 1);
        applicationUserRepository.save(complaintAuthor);
    }

    private void moderateCommentComplaint(UUID commentId, ComplaintModerateDTO dto) {
        Comment comment = repoHelper.getEntityById(Comment.class, commentId);

        if (dto.getNewCommentMessage() != null && !dto.getNewCommentMessage().trim().isEmpty()) {
            comment.setMessage(dto.getNewCommentMessage());
        }
        if (dto.getDeleteComment() != null && dto.getDeleteComment()) {
            commentRepository.delete(comment);
        }
        if (dto.getBlockCommentAuthor() != null && dto.getBlockCommentAuthor()) {
            ApplicationUser commentAuthor = comment.getAuthor();
            commentAuthor.setIsBlocked(true);
            applicationUserRepository.save(commentAuthor);
        }
    }

    private Complaint getComplaintByUserId(UUID id, UUID userId) {
        return Optional.ofNullable(complaintRepository.findByIdAndAuthorId(id, userId))
                .orElseThrow(() -> new EntityNotFoundException(Complaint.class, id, userId));
    }
}
