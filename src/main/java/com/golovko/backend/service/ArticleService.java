package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TranslationService translationService;

    public ArticleReadDTO getArticle(UUID id) {
        Article article = getArticleRequired(id);
        return translationService.toRead(article);
    }

    public ArticleReadExtendedDTO getArticleExtended(UUID id) {
        Article article = getArticleRequired(id);
        return translationService.toReadExtended(article);
    }

    public List<ArticleReadDTO> getArticleList() {
        List<Article> articles = articleRepository.findByStatusOrderByCreatedAtDesc(ArticleStatus.PUBLISHED);
        return articles.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public ArticleReadDTO createArticle(ArticleCreateDTO createDTO, ApplicationUser author) {
        Article article = translationService.toEntity(createDTO, author);

        article = articleRepository.save(article);
        return translationService.toRead(article);
    }

    public ArticleReadDTO updateArticle(UUID id, ArticlePutDTO putDTO) {
        Article article = getArticleRequired(id);

        translationService.updateEntity(article, putDTO);

        article = articleRepository.save(article);
        return translationService.toRead(article);
    }

    public ArticleReadDTO patchArticle(UUID id, ArticlePatchDTO patchDTO) {
        Article article = getArticleRequired(id);

        translationService.patchEntity(article, patchDTO);

        article = articleRepository.save(article);
        return translationService.toRead(article);
    }

    public void deleteArticle(UUID id) {
        articleRepository.delete(getArticleRequired(id));
    }

    private Article getArticleRequired(UUID id) {
        return articleRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(Article.class, id));
    }
}
