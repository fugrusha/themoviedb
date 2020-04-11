package com.golovko.backend.controller;

import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    @GetMapping("/{id}")
    public ArticleReadDTO getArticle(@PathVariable UUID id) {
        return articleService.getArticle(id);
    }

    @GetMapping
    public PageResult<ArticleReadDTO> getAllArticles(Pageable pageable) {
        return articleService.getAllPublishedArticles(pageable);
    }

    @GetMapping("/{id}/extended")
    public ArticleReadExtendedDTO getArticleExtended(@PathVariable UUID id) {
        return articleService.getArticleExtended(id);
    }

    @PostMapping
    public ArticleReadDTO createArticle(@RequestBody @Valid ArticleCreateDTO createDTO) {
        return articleService.createArticle(createDTO);
    }

    @PutMapping("/{id}")
    public ArticleReadDTO updateArticle(
            @PathVariable UUID id,
            @RequestBody @Valid ArticlePutDTO putDTO
    ) {
        return articleService.updateArticle(id, putDTO);
    }

    @PatchMapping("/{id}")
    public ArticleReadDTO patchArticle(
            @PathVariable UUID id,
            @RequestBody @Valid ArticlePatchDTO patchDTO
    ) {
        return articleService.patchArticle(id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteArticle(@PathVariable UUID id) {
        articleService.deleteArticle(id);
    }

    @GetMapping("/{articleId}/people")
    public List<PersonReadDTO> getArticlePeople(@PathVariable UUID articleId) {
        return articleService.getArticlePeople(articleId);
    }

    @PostMapping("/{articleId}/people/{personId}")
    public List<PersonReadDTO> addPersonToArticle(
            @PathVariable UUID articleId,
            @PathVariable UUID personId
    ) {
        return articleService.addPersonToArticle(articleId, personId);
    }

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

    @PostMapping("/{articleId}/movies/{movieId}")
    public List<MovieReadDTO> addMovieToArticle(
            @PathVariable UUID articleId,
            @PathVariable UUID movieId
    ) {
        return articleService.addMovieToArticle(articleId, movieId);
    }

    @DeleteMapping("/{articleId}/movies/{movieId}")
    public List<MovieReadDTO> removeMovieFromArticle(
            @PathVariable UUID articleId,
            @PathVariable UUID movieId
    ) {
        return articleService.removeMovieFromArticle(articleId, movieId);
    }
}
