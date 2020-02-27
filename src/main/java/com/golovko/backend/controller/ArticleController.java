package com.golovko.backend.controller;

import com.golovko.backend.dto.article.*;
import com.golovko.backend.service.ArticleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    public List<ArticleReadDTO> getArticleList() {
        return articleService.getArticleList();
    }

    @GetMapping("/{id}/extended")
    public ArticleReadExtendedDTO getArticleExtended(@PathVariable UUID id) {
        return articleService.getArticleExtended(id);
    }

    @PostMapping
    public ArticleReadDTO createArticle(@RequestBody ArticleCreateDTO createDTO) {
        return articleService.createArticle(createDTO);
    }

    @PutMapping("/{id}")
    public ArticleReadDTO updateArticle(@PathVariable UUID id, @RequestBody ArticlePutDTO putDTO) {
        return articleService.updateArticle(id, putDTO);
    }

    @PatchMapping("/{id}")
    public ArticleReadDTO patchArticle(@PathVariable UUID id, @RequestBody ArticlePatchDTO patchDTO) {
        return articleService.patchArticle(id, patchDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteArticle(@PathVariable UUID id) {
        articleService.deleteArticle(id);
    }
}
