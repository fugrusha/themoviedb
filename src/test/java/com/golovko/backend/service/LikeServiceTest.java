package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.like.LikeCreateDTO;
import com.golovko.backend.dto.like.LikePatchDTO;
import com.golovko.backend.dto.like.LikePutDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.WrongTypeOfTargetObjectException;
import com.golovko.backend.repository.ArticleRepository;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.LikeRepository;
import com.golovko.backend.repository.MovieRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

import static com.golovko.backend.domain.CommentStatus.APPROVED;
import static com.golovko.backend.domain.TargetObjectType.*;

public class LikeServiceTest extends BaseTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LikeService likeService;

    @Test
    public void testGetRatingById() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Like like = testObjectFactory.createLike(true, user, article.getId(), ARTICLE);

        LikeReadDTO readDTO = likeService.getLike(user.getId(), like.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(like, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), like.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetLikeWrongId() {
        likeService.getLike(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testCreateLike() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(true);
        createDTO.setLikedObjectType(ARTICLE);
        createDTO.setLikedObjectId(article.getId());

        LikeReadDTO readDTO = likeService.createLike(user.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Like like = likeRepository.findByIdAndUserId(readDTO.getId(), user.getId());
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(like, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), like.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateLikeWrongUser() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(true);
        createDTO.setLikedObjectType(ARTICLE);
        createDTO.setLikedObjectId(article.getId());

        likeService.createLike(UUID.randomUUID(), createDTO);
    }

    @Test
    public void testPatchLike() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Like like = testObjectFactory.createLike(true, user, article.getId(), ARTICLE);

        LikePatchDTO patchDTO = new LikePatchDTO();
        patchDTO.setMeLiked(false);

        LikeReadDTO readDTO = likeService.patchLike(user.getId(), like.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");

        like = likeRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(like).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(readDTO.getAuthorId(), like.getAuthor().getId());
    }

    @Test
    public void testPatchLikeEmptyPatch() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Like like = testObjectFactory.createLike(true, user, article.getId(), ARTICLE);

        LikePatchDTO patchDTO = new LikePatchDTO();

        LikeReadDTO readDTO = likeService.patchLike(user.getId(), like.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        Like updatedLike = likeRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(updatedLike).isEqualToIgnoringGivenFields(like, "author");
        Assert.assertEquals(readDTO.getAuthorId(), updatedLike.getAuthor().getId());
    }

    @Test
    public void testUpdateLike() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Like like = testObjectFactory.createLike(true, user, article.getId(), ARTICLE);

        LikePutDTO putDTO = new LikePutDTO();
        putDTO.setMeLiked(false);

        LikeReadDTO readDTO = likeService.updateLike(user.getId(), like.getId(), putDTO);

        like = likeRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(like).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(readDTO.getAuthorId(), like.getAuthor().getId());
    }

    @Test
    public void testDeleteLike() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Like like = testObjectFactory.createLike(true, user, article.getId(), ARTICLE);

        likeService.deleteLike(user.getId(), like.getId());

        Assert.assertFalse(likeRepository.existsById(like.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteLikeNotFound() {
        likeService.deleteLike(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testCreateSecondLikeForMovie() {
        ApplicationUser user = testObjectFactory.createUser();

        Movie movie = testObjectFactory.createMovie();
        movie.setLikesCount(5);
        movieRepository.save(movie);

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(true);
        createDTO.setLikedObjectType(MOVIE);
        createDTO.setLikedObjectId(movie.getId());

        likeService.createLike(user.getId(), createDTO); // first like was created

        Assertions.assertThatThrownBy(() -> {
            likeService.createLike(user.getId(), createDTO); // second like was created
        }).isInstanceOf(ResponseStatusException.class);
    }

    @Test(expected = WrongTypeOfTargetObjectException.class)
    public void testCreateLikeForWrongObjectType() {
        ApplicationUser user = testObjectFactory.createUser();

        Movie movie = testObjectFactory.createMovie();

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(true);
        createDTO.setLikedObjectType(MOVIE_CAST);    // wrong type
        createDTO.setLikedObjectId(movie.getId());

        likeService.createLike(user.getId(), createDTO);
    }

    @Test(expected = WrongTypeOfTargetObjectException.class)
    public void testCreateDislikeForWrongObjectType() {
        ApplicationUser user = testObjectFactory.createUser();

        Movie movie = testObjectFactory.createMovie();

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(false);
        createDTO.setLikedObjectType(MOVIE_CAST);    // wrong type
        createDTO.setLikedObjectId(movie.getId());

        likeService.createLike(user.getId(), createDTO);
    }

    @Test
    public void testCreateLikeForMovie() {
        ApplicationUser user = testObjectFactory.createUser();

        Movie movie = testObjectFactory.createMovie();
        movie.setLikesCount(5);
        movieRepository.save(movie);

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(true);
        createDTO.setLikedObjectType(MOVIE);
        createDTO.setLikedObjectId(movie.getId());

        LikeReadDTO readDTO = likeService.createLike(user.getId(), createDTO);

        Assert.assertEquals(readDTO.getLikedObjectId(), movie.getId());

        Movie updatedMovie = movieRepository.findById(readDTO.getLikedObjectId()).get();
        Assert.assertEquals(updatedMovie.getLikesCount(), (Integer) 6);
    }

    @Test
    public void testCreateLikeForComment() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, movie.getId(), APPROVED, MOVIE);
        c1.setLikesCount(5);
        commentRepository.save(c1);

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(true);
        createDTO.setLikedObjectType(COMMENT);
        createDTO.setLikedObjectId(c1.getId());

        LikeReadDTO readDTO = likeService.createLike(user.getId(), createDTO);

        Assert.assertEquals(readDTO.getLikedObjectId(), c1.getId());

        Comment updatedComment = commentRepository.findById(readDTO.getLikedObjectId()).get();
        Assert.assertEquals(updatedComment.getLikesCount(), (Integer) 6);
    }

    @Test
    public void testCreateLikeForArticle() {
        ApplicationUser author = testObjectFactory.createUser();

        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a1.setLikesCount(5);
        articleRepository.save(a1);

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(true);
        createDTO.setLikedObjectType(ARTICLE);
        createDTO.setLikedObjectId(a1.getId());

        LikeReadDTO readDTO = likeService.createLike(author.getId(), createDTO);

        Assert.assertEquals(readDTO.getLikedObjectId(), a1.getId());

        Article updatedArticle = articleRepository.findById(readDTO.getLikedObjectId()).get();
        Assert.assertEquals(updatedArticle.getLikesCount(), (Integer) 6);
    }

    @Test
    public void testDeleteLikeForMovie() {
        ApplicationUser user = testObjectFactory.createUser();

        Movie movie = testObjectFactory.createMovie();
        movie.setLikesCount(5);
        movieRepository.save(movie);

        Like like = testObjectFactory.createLike(true, user, movie.getId(), MOVIE);

        likeService.deleteLike(user.getId(), like.getId());

        Movie updatedMovie = movieRepository.findById(movie.getId()).get();
        Assert.assertEquals(updatedMovie.getLikesCount(), (Integer) 4);
    }

    @Test
    public void testDeleteLikeForComment() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, movie.getId(), APPROVED, MOVIE);
        c1.setLikesCount(5);
        commentRepository.save(c1);

        Like like = testObjectFactory.createLike(true, user, c1.getId(), COMMENT);

        likeService.deleteLike(user.getId(), like.getId());

        Comment updatedComment = commentRepository.findById(c1.getId()).get();
        Assert.assertEquals(updatedComment.getLikesCount(), (Integer) 4);
    }

    @Test
    public void testDeleteLikeForArticle() {
        ApplicationUser author = testObjectFactory.createUser();

        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a1.setLikesCount(5);
        articleRepository.save(a1);

        Like like = testObjectFactory.createLike(true, author, a1.getId(), ARTICLE);

        likeService.deleteLike(author.getId(), like.getId());

        Article updatedArticle = articleRepository.findById(a1.getId()).get();
        Assert.assertEquals(updatedArticle.getLikesCount(), (Integer) 4);
    }

    @Test
    public void testCreateDislikeForMovie() {
        ApplicationUser user = testObjectFactory.createUser();

        Movie movie = testObjectFactory.createMovie();
        movie.setDislikesCount(5);
        movieRepository.save(movie);

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(false);
        createDTO.setLikedObjectType(MOVIE);
        createDTO.setLikedObjectId(movie.getId());

        LikeReadDTO readDTO = likeService.createLike(user.getId(), createDTO);

        Assert.assertEquals(readDTO.getLikedObjectId(), movie.getId());

        Movie updatedMovie = movieRepository.findById(readDTO.getLikedObjectId()).get();
        Assert.assertEquals(updatedMovie.getDislikesCount(), (Integer) 6);
    }

    @Test
    public void testCreateDislikeForComment() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, movie.getId(), APPROVED, MOVIE);
        c1.setDislikesCount(5);
        commentRepository.save(c1);

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(false);
        createDTO.setLikedObjectType(COMMENT);
        createDTO.setLikedObjectId(c1.getId());

        LikeReadDTO readDTO = likeService.createLike(user.getId(), createDTO);

        Assert.assertEquals(readDTO.getLikedObjectId(), c1.getId());

        Comment updatedComment = commentRepository.findById(readDTO.getLikedObjectId()).get();
        Assert.assertEquals(updatedComment.getDislikesCount(), (Integer) 6);
    }

    @Test
    public void testCreateDislikeForArticle() {
        ApplicationUser author = testObjectFactory.createUser();

        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a1.setDislikesCount(5);
        articleRepository.save(a1);

        LikeCreateDTO createDTO = new LikeCreateDTO();
        createDTO.setMeLiked(false);
        createDTO.setLikedObjectType(ARTICLE);
        createDTO.setLikedObjectId(a1.getId());

        LikeReadDTO readDTO = likeService.createLike(author.getId(), createDTO);

        Assert.assertEquals(readDTO.getLikedObjectId(), a1.getId());

        Article updatedArticle = articleRepository.findById(readDTO.getLikedObjectId()).get();
        Assert.assertEquals(updatedArticle.getDislikesCount(), (Integer) 6);
    }

    @Test
    public void testDeleteDislikeForMovie() {
        ApplicationUser user = testObjectFactory.createUser();

        Movie movie = testObjectFactory.createMovie();
        movie.setDislikesCount(5);
        movieRepository.save(movie);

        Like dislike = testObjectFactory.createLike(false, user, movie.getId(), MOVIE);

        likeService.deleteLike(user.getId(), dislike.getId());

        Movie updatedMovie = movieRepository.findById(movie.getId()).get();
        Assert.assertEquals(updatedMovie.getDislikesCount(), (Integer) 4);
    }

    @Test
    public void testDeleteDislikeForComment() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, movie.getId(), APPROVED, MOVIE);
        c1.setDislikesCount(5);
        commentRepository.save(c1);

        Like dislike = testObjectFactory.createLike(false, user, c1.getId(), COMMENT);

        likeService.deleteLike(user.getId(), dislike.getId());

        Comment updatedComment = commentRepository.findById(c1.getId()).get();
        Assert.assertEquals(updatedComment.getDislikesCount(), (Integer) 4);
    }

    @Test
    public void testDeleteDislikeForArticle() {
        ApplicationUser author = testObjectFactory.createUser();

        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a1.setDislikesCount(5);
        articleRepository.save(a1);

        Like dislike = testObjectFactory.createLike(false, author, a1.getId(), ARTICLE);

        likeService.deleteLike(author.getId(), dislike.getId());

        Article updatedArticle = articleRepository.findById(a1.getId()).get();
        Assert.assertEquals(updatedArticle.getDislikesCount(), (Integer) 4);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveLikeNotNullValidation() {
        Like like = new Like();
        likeRepository.save(like);
    }
}
