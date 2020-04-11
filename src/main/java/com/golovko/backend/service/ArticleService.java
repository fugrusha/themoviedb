package com.golovko.backend.service;

import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.LinkDuplicatedException;
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

import java.util.List;
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

    @Transactional
    public List<PersonReadDTO> getArticlePeople(UUID articleId) {
        Article article = repoHelper.getEntityById(Article.class, articleId);

        if (article.getPeople() == null || article.getPeople().isEmpty()) {
            throw new EntityNotFoundException("Article " + articleId + " has not any mentioned person.");
        }

        return translationService.translateList(article.getPeople(), PersonReadDTO.class);
    }

    @Transactional
    public List<PersonReadDTO> addPersonToArticle(UUID articleId, UUID personId) {
        Article article = repoHelper.getEntityById(Article.class, articleId);
        Person person = repoHelper.getEntityById(Person.class, personId);

        if (article.getPeople().stream().anyMatch(p -> p.getId().equals(personId))) {
            throw new LinkDuplicatedException(String.format("Article %s already has person %s", articleId, personId));
        }

        article.getPeople().add(person);
        article = articleRepository.save(article);

        return translationService.translateList(article.getPeople(), PersonReadDTO.class);
    }

    @Transactional
    public List<PersonReadDTO> removePersonFromArticle(UUID articleId, UUID personId) {
        Article article = repoHelper.getEntityById(Article.class, articleId);

        boolean removed = article.getPeople().removeIf(p -> p.getId().equals(personId));

        if (!removed) {
            throw new EntityNotFoundException("Article " + articleId + " has no person " + personId);
        }

        article = articleRepository.save(article);

        return translationService.translateList(article.getPeople(), PersonReadDTO.class);
    }

    @Transactional
    public List<MovieReadDTO> getArticleMovies(UUID articleId) {
        Article article = repoHelper.getEntityById(Article.class, articleId);

        if (article.getMovies() == null || article.getMovies().isEmpty()) {
            throw new EntityNotFoundException("Article " + articleId + " has not any mentioned movie.");
        }

        return translationService.translateList(article.getMovies(), MovieReadDTO.class);
    }

    @Transactional
    public List<MovieReadDTO> addMovieToArticle(UUID articleId, UUID movieId) {
        Article article = repoHelper.getEntityById(Article.class, articleId);
        Movie movie = repoHelper.getEntityById(Movie.class, movieId);

        if (article.getMovies().stream().anyMatch(m -> m.getId().equals(movieId))) {
            throw new LinkDuplicatedException(String.format("Article %s already has movie %s", articleId, movieId));
        }

        article.getMovies().add(movie);
        article = articleRepository.save(article);

        return translationService.translateList(article.getMovies(), MovieReadDTO.class);
    }

    @Transactional
    public List<MovieReadDTO> removeMovieFromArticle(UUID articleId, UUID movieId) {
        Article article = repoHelper.getEntityById(Article.class, articleId);

        boolean removed = article.getMovies().removeIf(m -> m.getId().equals(movieId));

        if (!removed) {
            throw new EntityNotFoundException("Article " + articleId + " has no movie " + movieId);
        }

        article = articleRepository.save(article);

        return translationService.translateList(article.getMovies(), MovieReadDTO.class);
    }
}
