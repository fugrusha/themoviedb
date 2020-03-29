package com.golovko.backend.controller;

import com.golovko.backend.controller.validation.ControllerValidationUtil;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.ArticleManagerFilter;
import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.misprint.MisprintConfirmDTO;
import com.golovko.backend.dto.misprint.MisprintFilter;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.dto.misprint.MisprintRejectDTO;
import com.golovko.backend.service.ArticleService;
import com.golovko.backend.service.MisprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ContentManagerController {

    @Autowired
    private MisprintService misprintService;

    @Autowired
    private ArticleService articleService;

    @PostMapping("/misprints/{id}/confirm")
    public MisprintReadDTO confirmModeration(
            @PathVariable UUID id,
            @RequestBody @Valid MisprintConfirmDTO dto
    ) {
        ControllerValidationUtil.validateLessThan(dto.getStartIndex(), dto.getEndIndex(),
                "startIndex", "endIndex");
        return misprintService.confirmModeration(id, dto);
    }

    @PostMapping("/misprints/{id}/reject")
    public MisprintReadDTO rejectModeration(
            @PathVariable UUID id,
            @RequestBody @Valid MisprintRejectDTO dto
    ) {
        return misprintService.rejectModeration(id, dto);
    }

    @GetMapping("/misprints")
    public PageResult<MisprintReadDTO> getAllMisprints(MisprintFilter filter, Pageable pageable) {
        return misprintService.getMisprintsByFilter(filter, pageable);
    }

    @GetMapping("/articles/{articleId}/misprints/")
    public PageResult<MisprintReadDTO> getAllMisprintsByArticleId(
            @PathVariable UUID articleId,
            Pageable pageable
    ) {
        return misprintService.getMisprintsByTargetId(articleId, pageable);
    }

    @GetMapping("/articles/{articleId}/misprints/{id}")
    public MisprintReadDTO getMisprintByArticleId(
            @PathVariable UUID articleId,
            @PathVariable UUID id
    ) {
        return misprintService.getMisprintByTargetId(articleId, id);
    }

    @GetMapping("/movies/{movieId}/misprints/")
    public PageResult<MisprintReadDTO> getAllMisprintsByMovieId(
            @PathVariable UUID movieId,
            Pageable pageable
    ) {
        return misprintService.getMisprintsByTargetId(movieId, pageable);
    }

    @GetMapping("/movies/{movieId}/misprints/{id}")
    public MisprintReadDTO getMisprintByMovieId(
            @PathVariable UUID movieId,
            @PathVariable UUID id
    ) {
        return misprintService.getMisprintByTargetId(movieId, id);
    }

    @GetMapping("/persons/{personId}/misprints/")
    public PageResult<MisprintReadDTO> getAllMisprintsByPersonId(
            @PathVariable UUID personId,
            Pageable pageable
    ) {
        return misprintService.getMisprintsByTargetId(personId, pageable);
    }

    @GetMapping("/persons/{personId}/misprints/{id}")
    public MisprintReadDTO getMisprintByPersonId(
            @PathVariable UUID personId,
            @PathVariable UUID id
    ) {
        return misprintService.getMisprintByTargetId(personId, id);
    }

    @GetMapping("/movie-casts/{movieCastId}/misprints/")
    public PageResult<MisprintReadDTO> getAllMisprintsByMovieCastIdId(
            @PathVariable UUID movieCastId,
            Pageable pageable
    ) {
        return misprintService.getMisprintsByTargetId(movieCastId, pageable);
    }

    @GetMapping("/movie-casts/{movieCastId}/misprints/{id}")
    public MisprintReadDTO getMisprintByMovieCastId(
            @PathVariable UUID movieCastId,
            @PathVariable UUID id
    ) {
        return misprintService.getMisprintByTargetId(movieCastId, id);
    }

    @GetMapping("/movie-crews/{movieCrewId}/misprints/")
    public PageResult<MisprintReadDTO> getAllMisprintsByMovieCrewId(
            @PathVariable UUID movieCrewId,
            Pageable pageable
    ) {
        return misprintService.getMisprintsByTargetId(movieCrewId, pageable);
    }

    @GetMapping("/movie-crews/{movieCrewId}/misprints/{id}")
    public MisprintReadDTO getMisprintByMovieCrewId(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id
    ) {
        return misprintService.getMisprintByTargetId(movieCrewId, id);
    }

    @GetMapping("/articles/filter")
    public PageResult<ArticleReadDTO> getArticlesByFilter(ArticleManagerFilter filter, Pageable pageable) {
        return articleService.getArticlesByFilter(filter, pageable);
    }
}
