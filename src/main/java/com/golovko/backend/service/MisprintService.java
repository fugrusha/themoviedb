package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.misprint.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.EntityWrongStatusException;
import com.golovko.backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public List<MisprintReadDTO> getAllUserMisprintComplaints(UUID userId) {
        List<Misprint> misprints = misprintRepository.findByAuthorIdOrderByCreatedAtAsc(userId);

        return misprints.stream()
                .map(m -> translationService.translate(m, MisprintReadDTO.class))
                .collect(Collectors.toList());
    }

    public MisprintReadDTO createMisprintComplaint(UUID userId, MisprintCreateDTO createDTO) {
        Misprint misprint = translationService.translate(createDTO, Misprint.class);

        misprint.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, userId));
        misprint.setStatus(ComplaintStatus.INITIATED);
        misprint = misprintRepository.save(misprint);

        return translationService.translate(misprint, MisprintReadDTO.class);
    }

    public void deleteMisprintComplaint(UUID userId, UUID id) {
        misprintRepository.delete(getMisprintByUserId(id, userId));
    }

    public List<MisprintReadDTO> getAllMisprintsByTargetId(UUID targetObjectId) {
        List<Misprint> misprints = misprintRepository.findAllByTargetObjectId(targetObjectId);

        return misprints.stream()
                .map(m -> translationService.translate(m, MisprintReadDTO.class))
                .collect(Collectors.toList());
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
            String text = getTextWithMisprintFromEntity(misprint);
            String misprintText = misprint.getMisprintText();

            String newText = replaceMisprint(text, misprintText, dto);

            saveNewTextToEntity(misprint, newText);

            setStatusClosedAndSave(dto, misprint);

            closeSimilarMisprints(dto, misprintText);

            return translationService.translate(misprint, MisprintReadDTO.class);
        }
    }

    private void closeSimilarMisprints(MisprintConfirmDTO dto, String misprintText) {
        misprintRepository.findSimilarMisprints(dto.getTargetObjectId(), misprintText, ComplaintStatus.INITIATED)
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

    public String getTextWithMisprintFromEntity(Misprint misprint) {
        switch (misprint.getTargetObjectType()) {
          case MOVIE:
              Movie movie = repoHelper.getEntityById(Movie.class, misprint.getTargetObjectId());
              return movie.getDescription();
          case ARTICLE:
              Article article = repoHelper.getEntityById(Article.class, misprint.getTargetObjectId());
              return article.getText();
          case PERSON:
              Person person = repoHelper.getEntityById(Person.class, misprint.getTargetObjectId());
              return person.getBio();
          case MOVIE_CAST:
              MovieCast movieCast = repoHelper.getEntityById(MovieCast.class, misprint.getTargetObjectId());
              return movieCast.getDescription();
          case MOVIE_CREW:
              MovieCrew movieCrew = repoHelper.getEntityById(MovieCrew.class, misprint.getTargetObjectId());
              return movieCrew.getDescription();
          default:
              throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                      String.format("It is not allowed to fix misprint in %s entity",
                              misprint.getTargetObjectType()));
        }
    }

    public void saveNewTextToEntity(Misprint misprint, String newText) {
        switch (misprint.getTargetObjectType()) {
          case MOVIE:
              Movie movie = repoHelper.getEntityById(Movie.class, misprint.getTargetObjectId());
              movie.setDescription(newText);
              movieRepository.save(movie);
              break;
          case ARTICLE:
              Article article = repoHelper.getEntityById(Article.class, misprint.getTargetObjectId());
              article.setText(newText);
              articleRepository.save(article);
              break;
          case PERSON:
              Person person = repoHelper.getEntityById(Person.class, misprint.getTargetObjectId());
              person.setBio(newText);
              personRepository.save(person);
              break;
          case MOVIE_CAST:
              MovieCast movieCast = repoHelper.getEntityById(MovieCast.class, misprint.getTargetObjectId());
              movieCast.setDescription(newText);
              movieCastRepository.save(movieCast);
              break;
          case MOVIE_CREW:
              MovieCrew movieCrew = repoHelper.getEntityById(MovieCrew.class, misprint.getTargetObjectId());
              movieCrew.setDescription(newText);
              movieCrewRepository.save(movieCrew);
              break;
          default:
        }
    }

    public String replaceMisprint(String text, String misprintText, MisprintConfirmDTO dto) {

        if (misprintText.equals(text.substring(dto.getStartIndex(), dto.getEndIndex()))) {
            return text.substring(0, dto.getStartIndex())
                    + dto.getReplaceTo()
                    + text.substring(dto.getEndIndex());
        } else {
            throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,
                    "Text could not be replaced."
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
