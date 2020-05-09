package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.misprint.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.EntityWrongStatusException;
import com.golovko.backend.exception.TextBetweenIndexesNotFoundException;
import com.golovko.backend.exception.WrongTargetObjectTypeException;
import com.golovko.backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Slf4j
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

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    public PageResult<MisprintReadDTO> getMisprintsByFilter(MisprintFilter filter, Pageable pageable) {
        Page<Misprint> misprints = misprintRepository.findByFilter(filter, pageable);

        return translationService.toPageResult(misprints, MisprintReadDTO.class);
    }

    public MisprintReadDTO getMisprintComplaint(UUID userId, UUID id) {
        Misprint misprint = getMisprintByUserId(id, userId);

        return translationService.translate(misprint, MisprintReadDTO.class);
    }

    public PageResult<MisprintReadDTO> getAllUserMisprintComplaints(UUID userId, Pageable pageable) {
        Page<Misprint> misprints = misprintRepository.findByAuthorId(userId, pageable);

        return translationService.toPageResult(misprints, MisprintReadDTO.class);
    }

    public MisprintReadDTO createMisprintComplaint(UUID userId, MisprintCreateDTO createDTO) {
        Misprint misprint = translationService.translate(createDTO, Misprint.class);

        validateTargetObject(createDTO);

        misprint.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, userId));
        misprint.setStatus(ComplaintStatus.INITIATED);
        misprint = misprintRepository.save(misprint);

        return translationService.translate(misprint, MisprintReadDTO.class);
    }

    private void validateTargetObject(MisprintCreateDTO createDTO) {
        switch (createDTO.getTargetObjectType()) {
          case MOVIE:
              repoHelper.getReferenceIfExist(Movie.class, createDTO.getTargetObjectId());
              break;
          case ARTICLE:
              repoHelper.getReferenceIfExist(Article.class, createDTO.getTargetObjectId());
              break;
          case MOVIE_CAST:
              repoHelper.getReferenceIfExist(MovieCast.class, createDTO.getTargetObjectId());
              break;
          case MOVIE_CREW:
              repoHelper.getReferenceIfExist(MovieCrew.class, createDTO.getTargetObjectId());
              break;
          case PERSON:
              repoHelper.getReferenceIfExist(Person.class, createDTO.getTargetObjectId());
              break;
          default:
              throw new WrongTargetObjectTypeException(ActionType.CREATE_MISPRINT, createDTO.getTargetObjectType());
        }
    }

    public void deleteMisprintComplaint(UUID userId, UUID id) {
        misprintRepository.delete(getMisprintByUserId(id, userId));
    }

    public PageResult<MisprintReadDTO> getMisprintsByTargetId(UUID targetObjectId, Pageable pageable) {
        Page<Misprint> misprints = misprintRepository.findAllByTargetObjectId(targetObjectId, pageable);

        return translationService.toPageResult(misprints, MisprintReadDTO.class);
    }

    public MisprintReadDTO getMisprintByTargetId(UUID targetObjectId, UUID id) {
        Misprint misprint = getMisprintByTargetIdRequired(id, targetObjectId);

        return translationService.translate(misprint, MisprintReadDTO.class);
    }

    public MisprintReadDTO rejectModeration(UUID id, MisprintRejectDTO dto) {
        Misprint misprint = repoHelper.getEntityById(Misprint.class, id);

        if (!misprint.getStatus().equals(ComplaintStatus.INITIATED)) {
            throw new EntityWrongStatusException(Misprint.class, id);
        } else {
            misprint.setStatus(dto.getStatus());
            misprint.setReason(dto.getReason());
            misprint.setFixedAt(Instant.now());
            misprint.setModerator(repoHelper.getReferenceIfExist(ApplicationUser.class, dto.getModeratorId()));
            misprintRepository.save(misprint);

            return translationService.translate(misprint, MisprintReadDTO.class);
        }
    }

    @Transactional
    public MisprintReadDTO confirmModeration(UUID id, MisprintConfirmDTO dto) {
        Misprint misprint = repoHelper.getEntityById(Misprint.class, id);

        if (!misprint.getStatus().equals(ComplaintStatus.INITIATED)) {
            throw new EntityWrongStatusException(Misprint.class, id);
        } else {
            fixIssue(misprint, dto);

            setStatusClosedAndSave(dto, misprint);

            closeSimilarMisprints(dto, misprint.getMisprintText(), misprint.getTargetObjectId());

            return translationService.translate(misprint, MisprintReadDTO.class);
        }
    }

    private void closeSimilarMisprints(MisprintConfirmDTO dto, String misprintText, UUID targetObjectId) {
        misprintRepository.findSimilarMisprints(targetObjectId, misprintText, ComplaintStatus.INITIATED)
                .forEach(m -> {
                    setStatusClosedAndSave(dto, m);

                    log.info("And misprint with id={} saved successfully", m.getId());
                });
    }

    private void setStatusClosedAndSave(MisprintConfirmDTO dto, Misprint misprint) {
        misprint.setReplacedWith(dto.getReplaceTo());
        misprint.setFixedAt(Instant.now());
        misprint.setModerator(repoHelper.getReferenceIfExist(ApplicationUser.class, dto.getModeratorId()));
        misprint.setStatus(ComplaintStatus.CLOSED);
        misprintRepository.save(misprint);

        log.info("Misprint with id={} saved successfully", misprint.getId());
    }

    public void fixIssue(Misprint misprint, MisprintConfirmDTO dto) {
        switch (misprint.getTargetObjectType()) {
          case MOVIE:
              Movie movie = repoHelper.getEntityById(Movie.class, misprint.getTargetObjectId());
              replaceMisprint(movie::getDescription, movie::setDescription, misprint.getMisprintText(), dto);
              movieRepository.save(movie);
              break;
          case ARTICLE:
              Article article = repoHelper.getEntityById(Article.class, misprint.getTargetObjectId());
              replaceMisprint(article::getText, article::setText, misprint.getMisprintText(), dto);
              articleRepository.save(article);
              break;
          case PERSON:
              Person person = repoHelper.getEntityById(Person.class, misprint.getTargetObjectId());
              replaceMisprint(person::getBio, person::setBio, misprint.getMisprintText(), dto);
              personRepository.save(person);
              break;
          case MOVIE_CAST:
              MovieCast movieCast = repoHelper.getEntityById(MovieCast.class, misprint.getTargetObjectId());
              replaceMisprint(movieCast::getDescription, movieCast::setDescription, misprint.getMisprintText(), dto);
              movieCastRepository.save(movieCast);
              break;
          case MOVIE_CREW:
              MovieCrew movieCrew = repoHelper.getEntityById(MovieCrew.class, misprint.getTargetObjectId());
              replaceMisprint(movieCrew::getDescription, movieCrew::setDescription, misprint.getMisprintText(), dto);
              movieCrewRepository.save(movieCrew);
              break;
          default:
              throw new WrongTargetObjectTypeException(ActionType.MODERATE_MISPRINT, misprint.getTargetObjectType());
        }
    }

    public void replaceMisprint(Supplier<String> getterText, Consumer<String> setter,
                                String misprintText, MisprintConfirmDTO dto) {
        String text = getterText.get();

        if (misprintText.equals(text.substring(dto.getStartIndex(), dto.getEndIndex()))) {
            String newText = text.substring(0, dto.getStartIndex())
                    + dto.getReplaceTo()
                    + text.substring(dto.getEndIndex());

            setter.accept(newText);
        } else {
            throw new TextBetweenIndexesNotFoundException("Text could not be replaced."
                    + " The text of mistake does not match with the text passed between indexes");
        }
    }

    private Misprint getMisprintByUserId(UUID id, UUID userId) {
        return Optional.ofNullable(misprintRepository.findByIdAndAuthorId(id, userId))
                .orElseThrow(() -> new EntityNotFoundException(Misprint.class, id, userId));
    }

    private Misprint getMisprintByTargetIdRequired(UUID id, UUID targetObjectId) {
        return Optional.ofNullable(misprintRepository.findByIdAndTargetObjectId(id, targetObjectId))
                .orElseThrow(() -> new EntityNotFoundException(Misprint.class, id, targetObjectId));
    }
}
