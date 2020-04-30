package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.ContentManager;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.service.ArticleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @ApiOperation(value = "Get article by id")
    @GetMapping("/{id}")
    public ArticleReadDTO getArticle(@PathVariable UUID id) {
        return articleService.getArticle(id);
    }

    @ApiPageable
    @ApiOperation(value = "Get all articles")
    @GetMapping
    public PageResult<ArticleReadDTO> getAllArticles(@ApiIgnore Pageable pageable) {
        return articleService.getAllPublishedArticles(pageable);
    }

    @ApiOperation(value = "Get extended article by id")
    @GetMapping("/{id}/extended")
    public ArticleReadExtendedDTO getArticleExtended(@PathVariable UUID id) {
        return articleService.getArticleExtended(id);
    }

    @ApiOperation(value = "Create article", notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @PostMapping
    public ArticleReadDTO createArticle(@RequestBody @Valid ArticleCreateDTO createDTO) {
        return articleService.createArticle(createDTO);
    }

    @ApiOperation(value = "Update article",
            notes = "Needs CONTENT_MANAGER authority. All fields will be updated")
    @ContentManager
    @PutMapping("/{id}")
    public ArticleReadDTO updateArticle(
            @PathVariable UUID id,
            @RequestBody @Valid ArticlePutDTO putDTO
    ) {
        return articleService.updateArticle(id, putDTO);
    }

    @ApiOperation(value = "Update article",
            notes = "Needs CONTENT_MANAGER authority. Empty fields will not be updated")
    @ContentManager
    @PatchMapping("/{id}")
    public ArticleReadDTO patchArticle(
            @PathVariable UUID id,
            @RequestBody @Valid ArticlePatchDTO patchDTO
    ) {
        return articleService.patchArticle(id, patchDTO);
    }

    @ApiOperation(value = "Delete article", notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @DeleteMapping("/{id}")
    public void deleteArticle(@PathVariable UUID id) {
        articleService.deleteArticle(id);
    }

    @GetMapping("/{articleId}/people")
    public List<PersonReadDTO> getArticlePeople(@PathVariable UUID articleId) {
        return articleService.getArticlePeople(articleId);
    }

    @ContentManager
    @PostMapping("/{articleId}/people/{personId}")
    public List<PersonReadDTO> addPersonToArticle(
            @PathVariable UUID articleId,
            @PathVariable UUID personId
    ) {
        return articleService.addPersonToArticle(articleId, personId);
    }

    @ContentManager
    @DeleteMapping("/{articleId}/people/{personId}")
    public List<PersonReadDTO> removePersonFromArticle(
            @PathVariable UUID articleId,
            @PathVariable UUID personId
    ) {
        return articleService.removePersonFromArticle(articleId, personId);
    }

    @GetMapping("/{articleId}/movies")
    public List<MovieReadDTO> getArticleMovies(@PathVariable UUID articleId) {
        return articleService.getArticleMovies(articleId);
    }

    @ContentManager
    @PostMapping("/{articleId}/movies/{movieId}")
    public List<MovieReadDTO> addMovieToArticle(
            @PathVariable UUID articleId,
            @PathVariable UUID movieId
    ) {
        return articleService.addMovieToArticle(articleId, movieId);
    }

    @ContentManager
    @DeleteMapping("/{articleId}/movies/{movieId}")
    public List<MovieReadDTO> removeMovieFromArticle(
            @PathVariable UUID articleId,
            @PathVariable UUID movieId
    ) {
        return articleService.removeMovieFromArticle(articleId, movieId);
    }
}
