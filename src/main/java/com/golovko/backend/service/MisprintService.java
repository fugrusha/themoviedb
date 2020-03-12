package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.Misprint;
import com.golovko.backend.dto.misprint.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.UnprocessableEntityException;
import com.golovko.backend.repository.ArticleRepository;
import com.golovko.backend.repository.MisprintRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MisprintService {

    @Autowired
    private MisprintRepository misprintRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    @Autowired
    private ArticleRepository articleRepository;

    public MisprintReadDTO getMisprintComplaint(UUID userId, UUID id) {
        Misprint misprint = getMisprintByUserId(id, userId);

        return translationService.toRead(misprint);
    }

    public List<MisprintReadDTO> getAllMisprintComplaints(UUID userId) {
        List<Misprint> misprints = misprintRepository.findByAuthorIdOrderByCreatedAtAsc(userId);

        return misprints.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public MisprintReadDTO createMisprintComplaint(UUID userId, MisprintCreateDTO createDTO) {
        Misprint misprint = translationService.toEntity(createDTO);

        misprint.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, userId));
        misprint.setStatus(ComplaintStatus.INITIATED);
        misprint = misprintRepository.save(misprint);

        return translationService.toRead(misprint);
    }

    public MisprintReadDTO patchMisprintComplaint(
            UUID userId,
            UUID id,
            MisprintPatchDTO patchDTO
    ) {
        Misprint misprint = getMisprintByUserId(id, userId);

        translationService.patchEntity(patchDTO, misprint);
        misprint = misprintRepository.save(misprint);

        return translationService.toRead(misprint);
    }

    public MisprintReadDTO updateMisprintComplaint(
            UUID userId,
            UUID id,
            MisprintPutDTO updateDTO
    ) {
        Misprint misprint = getMisprintByUserId(id, userId);

        translationService.updateEntity(updateDTO, misprint);
        misprint = misprintRepository.save(misprint);

        return translationService.toRead(misprint);
    }

    public void deleteMisprintComplaint(UUID userId, UUID id) {
        misprintRepository.delete(getMisprintByUserId(id, userId));
    }

    public List<MisprintReadDTO> getMisprintsByTargetId(UUID targetObjectId) {
        List<Misprint> misprints = misprintRepository.findAllByTargetObjectId(targetObjectId);

        return misprints.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public MisprintReadDTO getMisprintById(UUID targetObjectId, UUID id) {
        Misprint misprint = misprintRepository.findByIdAndTargetObjectId(id, targetObjectId);

        return translationService.toRead(misprint);
    }

    public MisprintReadDTO confirmModeration(UUID targetObjectId, UUID id, MisprintConfirmDTO dto) {
        Misprint misprint = misprintRepository.findByIdAndTargetObjectId(id, targetObjectId);

        if (!misprint.getStatus().equals(ComplaintStatus.INITIATED)) {
            throw new UnprocessableEntityException(Misprint.class, id);
        } else {
            replaceMisprint(targetObjectId, dto);

            misprint.setReplacedWith(dto.getReplaceTo());
            misprint.setFixedAt(Instant.now());
            misprint.setModerator(repoHelper.getReferenceIfExist(ApplicationUser.class, dto.getModeratorId()));
            misprintRepository.save(misprint);

            return translationService.toRead(misprint);
        }
    }

    public MisprintReadDTO rejectModeration(UUID targetObjectId, UUID id, MisprintRejectDTO dto) {
        Misprint misprint = misprintRepository.findByIdAndTargetObjectId(id, targetObjectId);

        if (!misprint.getStatus().equals(ComplaintStatus.INITIATED)) {
            throw new UnprocessableEntityException(Misprint.class, id);
        } else {
            misprint.setStatus(dto.getStatus());
            misprint.setReason(dto.getReason());
            misprint.setFixedAt(Instant.now());
            misprint.setModerator(repoHelper.getReferenceIfExist(ApplicationUser.class, dto.getModeratorId()));
            misprintRepository.save(misprint);

            return translationService.toRead(misprint);
        }
    }

    public void replaceMisprint(UUID targetObjectId, MisprintConfirmDTO dto) {
        Article article = repoHelper.getEntityById(Article.class, targetObjectId);

        String articleText = article.getText();

        String newArticleText = articleText.substring(0, dto.getStartIndex())
                + dto.getReplaceTo()
                + articleText.substring(dto.getEndIndex() + 1);

        article.setText(newArticleText);
        articleRepository.save(article);
    }

    private Misprint getMisprintByUserId(UUID id, UUID userId) {
        Misprint misprint = misprintRepository.findByIdAndAuthorId(id, userId);

        if (misprint != null) {
            return misprint;
        } else {
            throw new EntityNotFoundException(Misprint.class, id, userId);
        }
    }

}
