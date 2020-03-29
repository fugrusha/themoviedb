package com.golovko.backend.service;

import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.repository.ArticleRepository;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.LikeRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.ARTICLE;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public ArticleReadDTO getArticle(UUID id) {
        Article article = repoHelper.getEntityById(Article.class, id);

        return translationService.translate(article, ArticleReadDTO.class);
    }

    @Transactional(readOnly = true)
    public ArticleReadExtendedDTO getArticleExtended(UUID id) {
        Article article = repoHelper.getEntityById(Article.class, id);

        return translationService.translate(article, ArticleReadExtendedDTO.class);
    }

    public PageResult<ArticleReadDTO> getAllPublishedArticles(Pageable pageable) {
        Page<Article> articles = articleRepository
                .findByStatus(ArticleStatus.PUBLISHED, pageable);

        return translationService.toPageResult(articles, ArticleReadDTO.class);
    }

    public ArticleReadDTO createArticle(ArticleCreateDTO createDTO) {
        Article article = translationService.translate(createDTO, Article.class);

        article = articleRepository.save(article);

        return translationService.translate(article, ArticleReadDTO.class);
    }

    public ArticleReadDTO updateArticle(UUID id, ArticlePutDTO putDTO) {
        Article article = repoHelper.getEntityById(Article.class, id);

        translationService.map(putDTO, article);
        article = articleRepository.save(article);

        return translationService.translate(article, ArticleReadDTO.class);
    }

    public ArticleReadDTO patchArticle(UUID id, ArticlePatchDTO patchDTO) {
        Article article = repoHelper.getEntityById(Article.class, id);

        translationService.map(patchDTO, article);
        article = articleRepository.save(article);

        return translationService.translate(article, ArticleReadDTO.class);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void deleteArticle(UUID id) {
        articleRepository.delete(repoHelper.getEntityById(Article.class, id));
        commentRepository.deleteCommentsByTargetObjectId(id, ARTICLE);
        likeRepository.deleteLikesByTargetObjectId(id, ARTICLE);
    }

    public PageResult<ArticleReadDTO> getArticlesByFilter(ArticleManagerFilter filter, Pageable pageable) {
        Page<Article> articles = articleRepository.findByManagerFilter(filter, pageable);

        return translationService.toPageResult(articles, ArticleReadDTO.class);
    }
}
