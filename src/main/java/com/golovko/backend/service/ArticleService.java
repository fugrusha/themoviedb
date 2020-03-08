package com.golovko.backend.service;

import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.repository.ArticleRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public ArticleReadDTO getArticle(UUID id) {
        Article article = repoHelper.getEntityById(Article.class, id);
        return translationService.toRead(article);
    }

    @Transactional(readOnly = true)
    public ArticleReadExtendedDTO getArticleExtended(UUID id) {
        Article article = repoHelper.getEntityById(Article.class, id);
        return translationService.toReadExtended(article);
    }

    public List<ArticleReadDTO> getAllArticles() {
        List<Article> articles = articleRepository.findByStatusOrderByCreatedAtDesc(ArticleStatus.PUBLISHED);
        return articles.stream().map(translationService::toRead).collect(Collectors.toList());
    }

    public ArticleReadDTO createArticle(ArticleCreateDTO createDTO) {
        Article article = translationService.toEntity(createDTO);

        article = articleRepository.save(article);

        return translationService.toRead(article);
    }

    public ArticleReadDTO updateArticle(UUID id, ArticlePutDTO putDTO) {
        Article article = repoHelper.getEntityById(Article.class, id);

        translationService.updateEntity(article, putDTO);

        article = articleRepository.save(article);
        return translationService.toRead(article);
    }

    public ArticleReadDTO patchArticle(UUID id, ArticlePatchDTO patchDTO) {
        Article article = repoHelper.getEntityById(Article.class, id);

        translationService.patchEntity(article, patchDTO);

        article = articleRepository.save(article);
        return translationService.toRead(article);
    }

    public void deleteArticle(UUID id) {
        articleRepository.delete(repoHelper.getEntityById(Article.class, id));
    }
}
