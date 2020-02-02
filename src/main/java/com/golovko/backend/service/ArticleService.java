package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

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

    public ArticleReadDTO createArticle(ArticleCreateDTO createDTO, ApplicationUser author) {
        Article article = translationService.toEntity(createDTO);
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        article.setPublishedDate(now);
        article.setAuthor(author);

        article = articleRepository.save(article);
        return translationService.toRead(article);
    }

    public ArticleReadDTO updateArticle(UUID id, ArticlePutDTO putDTO) {
        return null;
    }

    public ArticleReadDTO patchArticle(UUID id, ArticlePatchDTO patchDTO) {
        return null;
    }

    public void deleteArticle(UUID id) {

    }

    private Article getArticleRequired(UUID id) {
        return articleRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(Article.class, id));
    }
}
