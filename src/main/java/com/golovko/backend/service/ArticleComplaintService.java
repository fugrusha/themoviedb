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
public class ArticleComplaintService {

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public ComplaintReadDTO getComplaint(UUID articleId, UUID id) {
        Complaint complaint = getComplaintByArticleId(id, articleId);
        return translationService.toRead(complaint);
    }

    public List<ComplaintReadDTO> getAllComplaints(UUID articleId) {
        List<Complaint> complaints = complaintRepository.findAllByParent(articleId, ParentType.ARTICLE);
        return complaints.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public ComplaintReadDTO createComplaint(UUID articleId, ComplaintCreateDTO createDTO, ApplicationUser author) {
        Complaint complaint = translationService.toEntity(createDTO);

        complaint.setComplaintStatus(ComplaintStatus.INITIATED);
        complaint.setParentType(ParentType.ARTICLE);
        complaint.setParentId(articleId);
        complaint.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, author.getId()));

        complaint = complaintRepository.save(complaint);
        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO patchComplaint(UUID articleId, UUID id, ComplaintPatchDTO patchDTO) {
        Complaint complaint = getComplaintByArticleId(id, articleId);

        translationService.patchEntity(patchDTO, complaint);
        complaint = complaintRepository.save(complaint);

        return translationService.toRead(complaint);
    }

    public ComplaintReadDTO updateComplaint(UUID articleId, UUID id, ComplaintPutDTO updateDTO) {
        Complaint complaint = getComplaintByArticleId(id, articleId);

        translationService.updateEntity(updateDTO, complaint);
        complaint = complaintRepository.save(complaint);

        return translationService.toRead(complaint);
    }

    public void deleteComplaint(UUID articleId, UUID id) {
        complaintRepository.delete(getComplaintByArticleId(id, articleId));
    }

    private Complaint getComplaintByArticleId(UUID id, UUID articleId) {
        Complaint complaint = complaintRepository.findByIdAndParentId(id, articleId, ParentType.ARTICLE);

        if (complaint != null) {
            return complaint;
        } else {
            throw new EntityNotFoundException(Complaint.class, id, Article.class, articleId);
        }
    }
}
