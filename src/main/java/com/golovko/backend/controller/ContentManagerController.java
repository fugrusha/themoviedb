package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.ContentManager;
import com.golovko.backend.controller.validation.ControllerValidationUtil;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.ArticleManagerFilter;
import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.misprint.MisprintConfirmDTO;
import com.golovko.backend.dto.misprint.MisprintFilter;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.dto.misprint.MisprintRejectDTO;
import com.golovko.backend.dto.movie.MovieReadExtendedDTO;
import com.golovko.backend.service.ArticleService;
import com.golovko.backend.service.ContentManagerService;
import com.golovko.backend.service.MisprintService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@ContentManager
public class ContentManagerController {

    @Autowired
    private MisprintService misprintService;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ContentManagerService contentManagerService;

    @ApiOperation(value = "Import movie, cast and crew team from TheMovieDB",
            notes = "Needs CONTENT_MANAGER authority")
    @PostMapping("/movies/import-movie/{externalMovieId}")
    public MovieReadExtendedDTO importMovie(@PathVariable String externalMovieId) {
        return contentManagerService.importMovie(externalMovieId);
    }

    @ApiOperation(value = "Confirm misprint moderation", notes = "Needs CONTENT_MANAGER authority")
    @PostMapping("/misprints/{id}/confirm")
    public MisprintReadDTO confirmModeration(
            @PathVariable UUID id,
            @RequestBody @Valid MisprintConfirmDTO dto
    ) {
        ControllerValidationUtil.validateLessThan(dto.getStartIndex(), dto.getEndIndex(),
                "startIndex", "endIndex");
        return misprintService.confirmModeration(id, dto);
    }

    @ApiOperation(value = "Reject misprint moderation", notes = "Needs CONTENT_MANAGER authority")
    @PostMapping("/misprints/{id}/reject")
    public MisprintReadDTO rejectModeration(
            @PathVariable UUID id,
            @RequestBody @Valid MisprintRejectDTO dto
    ) {
        return misprintService.rejectModeration(id, dto);
    }

    @ApiPageable
    @ApiOperation(value = "Get all misprints by filter", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/misprints")
    public PageResult<MisprintReadDTO> getAllMisprints(
            MisprintFilter filter,
            @ApiIgnore Pageable pageable
    ) {
        return misprintService.getMisprintsByFilter(filter, pageable);
    }

    @ApiPageable
    @ApiOperation(value = "Get all misprints for single article", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/articles/{articleId}/misprints/")
    public PageResult<MisprintReadDTO> getAllMisprintsByArticleId(
            @PathVariable UUID articleId,
            @ApiIgnore Pageable pageable
    ) {
        return misprintService.getMisprintsByTargetId(articleId, pageable);
    }

    @ApiOperation(value = "Get single misprint for article", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/articles/{articleId}/misprints/{id}")
    public MisprintReadDTO getMisprintByArticleId(
            @PathVariable UUID articleId,
            @PathVariable UUID id
    ) {
        return misprintService.getMisprintByTargetId(articleId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all misprints for single movie", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/movies/{movieId}/misprints/")
    public PageResult<MisprintReadDTO> getAllMisprintsByMovieId(
            @PathVariable UUID movieId,
            @ApiIgnore Pageable pageable
    ) {
        return misprintService.getMisprintsByTargetId(movieId, pageable);
    }

    @ApiOperation(value = "Get single misprint for movie", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/movies/{movieId}/misprints/{id}")
    public MisprintReadDTO getMisprintByMovieId(
            @PathVariable UUID movieId,
            @PathVariable UUID id
    ) {
        return misprintService.getMisprintByTargetId(movieId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all misprints for single person", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/people/{personId}/misprints/")
    public PageResult<MisprintReadDTO> getAllMisprintsByPersonId(
            @PathVariable UUID personId,
            @ApiIgnore Pageable pageable
    ) {
        return misprintService.getMisprintsByTargetId(personId, pageable);
    }

    @ApiOperation(value = "Get single misprint for person", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/people/{personId}/misprints/{id}")
    public MisprintReadDTO getMisprintByPersonId(
            @PathVariable UUID personId,
            @PathVariable UUID id
    ) {
        return misprintService.getMisprintByTargetId(personId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all misprints for movie cast", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/movie-casts/{movieCastId}/misprints/")
    public PageResult<MisprintReadDTO> getAllMisprintsByMovieCastIdId(
            @PathVariable UUID movieCastId,
            @ApiIgnore Pageable pageable
    ) {
        return misprintService.getMisprintsByTargetId(movieCastId, pageable);
    }

    @ApiOperation(value = "Get misprint for movie cast", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/movie-casts/{movieCastId}/misprints/{id}")
    public MisprintReadDTO getMisprintByMovieCastId(
            @PathVariable UUID movieCastId,
            @PathVariable UUID id
    ) {
        return misprintService.getMisprintByTargetId(movieCastId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all misprints for movie crew", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/movie-crews/{movieCrewId}/misprints/")
    public PageResult<MisprintReadDTO> getAllMisprintsByMovieCrewId(
            @PathVariable UUID movieCrewId,
            @ApiIgnore Pageable pageable
    ) {
        return misprintService.getMisprintsByTargetId(movieCrewId, pageable);
    }

    @ApiOperation(value = "Get misprint for movie crew", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/movie-crews/{movieCrewId}/misprints/{id}")
    public MisprintReadDTO getMisprintByMovieCrewId(
            @PathVariable UUID movieCrewId,
            @PathVariable UUID id
    ) {
        return misprintService.getMisprintByTargetId(movieCrewId, id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all articles by filter", notes = "Needs CONTENT_MANAGER authority")
    @GetMapping("/articles/filter")
    public PageResult<ArticleReadDTO> getArticlesByFilter(
            ArticleManagerFilter filter,
            @ApiIgnore Pageable pageable
    ) {
        return articleService.getArticlesByFilter(filter, pageable);
    }
}
