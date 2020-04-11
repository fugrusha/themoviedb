package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.comment.*;
import com.golovko.backend.exception.BlockedUserException;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.WrongTargetObjectTypeException;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.LikeRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionSystemException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static com.golovko.backend.domain.CommentStatus.*;
import static com.golovko.backend.domain.TargetObjectType.*;

public class CommentServiceTest extends BaseTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Test
    public void testGetCommentById() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Comment comment = testObjectFactory.createComment(user, article.getId(), APPROVED, ARTICLE);

        CommentReadDTO readDTO = commentService.getComment(article.getId(), comment.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(comment, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), comment.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetCommentWrongId() {
        commentService.getComment(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testGetAllPublishedComments() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article1 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment comment1 = testObjectFactory.createComment(user2, article1.getId(), APPROVED, ARTICLE);
        Comment comment2 = testObjectFactory.createComment(user2, article1.getId(), APPROVED, ARTICLE);
        testObjectFactory.createComment(user2, article1.getId(), NEED_MODERATION, ARTICLE);
        testObjectFactory.createComment(user1, article1.getId(), PENDING, ARTICLE);
        testObjectFactory.createComment(user1, article1.getId(), BLOCKED, ARTICLE);

        PageResult<CommentReadDTO> comments = commentService
                .getPublishedComments(article1.getId(), Pageable.unpaged());

        Assertions.assertThat(comments.getData()).extracting("id")
                .containsExactlyInAnyOrder(comment1.getId(), comment2.getId());
    }

    @Test
    public void testCreateCommentForArticle() {
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(ARTICLE);

        CommentReadDTO readDTO = commentService.createComment(article.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(comment, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), comment.getAuthor().getId());
        Assert.assertEquals(article.getId(), comment.getTargetObjectId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateCommentForArticleWrongArticleId() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(ARTICLE);

        UUID wrongArticleId = UUID.randomUUID();

        commentService.createComment(wrongArticleId, createDTO);
    }

    @Test
    public void testCreateCommentForMovie() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(MOVIE);

        CommentReadDTO readDTO = commentService.createComment(movie.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(comment, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), comment.getAuthor().getId());
        Assert.assertEquals(movie.getId(), comment.getTargetObjectId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateCommentForMovieWrongMovieId() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(MOVIE);

        UUID wrongMovieId = UUID.randomUUID();

        commentService.createComment(wrongMovieId, createDTO);
    }

    @Test
    public void testCreateCommentForMovieCast() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(MOVIE_CAST);

        CommentReadDTO readDTO = commentService.createComment(movieCast.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(comment, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), comment.getAuthor().getId());
        Assert.assertEquals(movieCast.getId(), comment.getTargetObjectId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateCommentForMovieCastWrongMovieCastId() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(MOVIE_CAST);

        UUID wrongMovieCastId = UUID.randomUUID();

        commentService.createComment(wrongMovieCastId, createDTO);
    }

    @Test
    public void testCreateCommentForMovieCrew() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(MOVIE_CREW);

        CommentReadDTO readDTO = commentService.createComment(movieCrew.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(comment, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), comment.getAuthor().getId());
        Assert.assertEquals(movieCrew.getId(), comment.getTargetObjectId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateCommentForMovieCrewWrongMovieCrewId() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(MOVIE_CREW);

        UUID wrongMovieCrewId = UUID.randomUUID();

        commentService.createComment(wrongMovieCrewId, createDTO);
    }

    @Test(expected = WrongTargetObjectTypeException.class)
    public void testCreateCommentForPerson() {
        ApplicationUser commentAuthor = testObjectFactory.createUser();
        Person person = testObjectFactory.createPerson();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(PERSON);

        commentService.createComment(person.getId(), createDTO);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateCommentWrongCommentAuthor() {
        ApplicationUser articleAuthor = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(articleAuthor, ArticleStatus.PUBLISHED);

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setTargetObjectType(ARTICLE);

        commentService.createComment(article.getId(), createDTO);
    }

    @Test
    public void testCreateCommentWithUserTrustLevelLessThanFive() {
        ApplicationUser commentAuthor = testObjectFactory.createUser(4.9, false);
        Movie movie = testObjectFactory.createMovie();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(MOVIE);

        CommentReadDTO readDTO = commentService.createComment(movie.getId(), createDTO);

        Assert.assertEquals(readDTO.getStatus(), PENDING);

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assert.assertEquals(comment.getStatus(), PENDING);
    }

    @Test
    public void testCreateCommentWithUserTrustLevelGraterThanFiveAndMore() {
        ApplicationUser commentAuthor = testObjectFactory.createUser(5.0, false);
        Movie movie = testObjectFactory.createMovie();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(MOVIE);

        CommentReadDTO readDTO = commentService.createComment(movie.getId(), createDTO);

        Assert.assertEquals(readDTO.getStatus(), APPROVED);

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assert.assertEquals(comment.getStatus(), APPROVED);
    }

    @Test(expected = BlockedUserException.class)
    public void testCreateCommentWithBlockedUser() {
        ApplicationUser commentAuthor = testObjectFactory.createUser(5.0, true);
        Movie movie = testObjectFactory.createMovie();

        CommentCreateDTO createDTO = new CommentCreateDTO();
        createDTO.setMessage("message text");
        createDTO.setAuthorId(commentAuthor.getId());
        createDTO.setTargetObjectType(MOVIE);

        commentService.createComment(movie.getId(), createDTO);
    }

    @Test
    public void testPatchComment() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        CommentPatchDTO patchDTO = new CommentPatchDTO();
        patchDTO.setMessage("New message");

        CommentReadDTO readDTO = commentService.patchComment(article.getId(), c.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        Comment commentAfterUpdate = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(commentAfterUpdate).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(commentAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testPatchCommentEmptyPatch() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        CommentPatchDTO patchDTO = new CommentPatchDTO();

        CommentReadDTO readDTO = commentService.patchComment(article.getId(), c.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        Comment commentAfterUpdate = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(commentAfterUpdate).hasNoNullFieldsOrProperties();
        Assertions.assertThat(commentAfterUpdate).isEqualToIgnoringGivenFields(c, "author");
        Assert.assertEquals(commentAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testUpdateComment() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        CommentPutDTO putDTO = new CommentPutDTO();
        putDTO.setMessage("message text");

        CommentReadDTO readDTO = commentService.updateComment(article.getId(), c.getId(), putDTO);

        Assertions.assertThat(putDTO).isEqualToComparingFieldByField(readDTO);

        Comment comment = commentRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(comment).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(comment.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testDeleteComment() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        commentService.deleteComment(article.getId(), c.getId());

        Assert.assertFalse(commentRepository.existsById(c.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteCommentNotFound() {
        commentService.deleteComment(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testDeleteCommentsWithLikes() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Comment c = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        Like like = testObjectFactory.createLike(true, user1, c.getId(), COMMENT);

        commentService.deleteComment(article.getId(), c.getId());

        Assert.assertFalse(commentRepository.existsById(c.getId()));
        Assert.assertFalse(likeRepository.existsById(like.getId()));
    }

    @Test
    public void testGetCommentsByEmptyFilter() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, article.getId(), APPROVED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user, article.getId(), BLOCKED, ARTICLE);
        Comment c3 = testObjectFactory.createComment(user, movie.getId(), APPROVED, MOVIE);

        CommentFilter filter = new CommentFilter();

        PageResult<CommentReadDTO> actualResult = commentService.getCommentsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());
    }

    @Test
    public void testGetCommentsWithEmptySetsOfFilter() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, article.getId(), APPROVED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user, article.getId(), BLOCKED, ARTICLE);
        Comment c3 = testObjectFactory.createComment(user, movie.getId(), APPROVED, MOVIE);

        CommentFilter filter = new CommentFilter();
        filter.setStatuses(new HashSet<CommentStatus>());
        filter.setTypes(new HashSet<TargetObjectType>());

        PageResult<CommentReadDTO> actualResult = commentService.getCommentsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId(), c3.getId());
    }

    @Test
    public void testGetCommentsByAuthor() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        testObjectFactory.createComment(user1, article.getId(), APPROVED, ARTICLE);
        Comment c1 = testObjectFactory.createComment(user2, article.getId(), BLOCKED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user2, movie.getId(), APPROVED, MOVIE);

        CommentFilter filter = new CommentFilter();
        filter.setAuthorId(user2.getId());

        PageResult<CommentReadDTO> actualResult = commentService.getCommentsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetCommentsByStatus() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        testObjectFactory.createComment(user1, article.getId(), BLOCKED, ARTICLE);
        Comment c1 = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user2, movie.getId(), APPROVED, MOVIE);

        CommentFilter filter = new CommentFilter();
        filter.setStatuses(Set.of(APPROVED));

        PageResult<CommentReadDTO> actualResult = commentService.getCommentsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());
    }

    @Test
    public void testGetCommentsByTargetObjectType() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        testObjectFactory.createComment(user1, article.getId(), BLOCKED, ARTICLE);
        testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);
        Comment c1 = testObjectFactory.createComment(user2, movie.getId(), APPROVED, MOVIE);

        CommentFilter filter = new CommentFilter();
        filter.setTypes(Set.of(MOVIE));

        PageResult<CommentReadDTO> actualResult = commentService.getCommentsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId());
    }

    @Test
    public void testGetCommentsByAllFilters() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user1, movie.getId(), NEED_MODERATION, MOVIE);
        testObjectFactory.createComment(user1, movie.getId(), BLOCKED, MOVIE); // wrong status
        testObjectFactory.createComment(user1, article.getId(), NEED_MODERATION, ARTICLE); // wrong targetObjectType
        testObjectFactory.createComment(user2, movie.getId(), NEED_MODERATION, MOVIE); // wrong user

        CommentFilter filter = new CommentFilter();
        filter.setStatuses(Set.of(NEED_MODERATION));
        filter.setTypes(Set.of(MOVIE));
        filter.setAuthorId(user1.getId());

        PageResult<CommentReadDTO> actualResult = commentService.getCommentsByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(c1.getId());
    }

    @Test
    public void testModerateCommentWithoutNewMessage() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, movie.getId(), PENDING, MOVIE);

        CommentModerateDTO dto = new CommentModerateDTO();
        dto.setNewStatus(APPROVED);

        CommentReadDTO actualResult = commentService.moderateComment(c1.getId(), dto);

        Assert.assertEquals(actualResult.getStatus(), dto.getNewStatus());

        Comment updatedComment = commentRepository.findById(c1.getId()).get();
        Assertions.assertThat(actualResult).isEqualToIgnoringGivenFields(updatedComment, "authorId");
        Assert.assertEquals(actualResult.getAuthorId(), updatedComment.getAuthor().getId());

        Assert.assertEquals(updatedComment.getStatus(), dto.getNewStatus());
    }

    @Test
    public void testModerateCommentWithNewMessage() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment c1 = testObjectFactory.createComment(user, movie.getId(), PENDING, MOVIE);

        CommentModerateDTO dto = new CommentModerateDTO();
        dto.setNewStatus(APPROVED);
        dto.setNewMessage("new text");

        CommentReadDTO actualResult = commentService.moderateComment(c1.getId(), dto);

        Assert.assertEquals(actualResult.getStatus(), dto.getNewStatus());

        Comment updatedComment = commentRepository.findById(c1.getId()).get();
        Assertions.assertThat(actualResult).isEqualToIgnoringGivenFields(updatedComment, "authorId");
        Assert.assertEquals(actualResult.getAuthorId(), updatedComment.getAuthor().getId());

        Assert.assertEquals(updatedComment.getStatus(), dto.getNewStatus());
        Assert.assertEquals(updatedComment.getMessage(), dto.getNewMessage());

    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveCommentNotNullValidation() {
        Comment comment = new Comment();
        commentRepository.save(comment);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveCommentMaxSizeValidation() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment comment = testObjectFactory.createComment(user, movie.getId(), PENDING, MOVIE);
        comment.setMessage("long message".repeat(100));
        commentRepository.save(comment);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveCommentMinSizeValidation() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Comment comment = testObjectFactory.createComment(user, movie.getId(), PENDING, MOVIE);
        comment.setMessage("");
        commentRepository.save(comment);
    }

    @Test
    public void testGetCommentsWithFilterWithPagingAndSorting() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        testObjectFactory.createComment(user1, article.getId(), BLOCKED, ARTICLE);
        Comment c1 = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user2, movie.getId(), APPROVED, MOVIE);

        CommentFilter filter = new CommentFilter();
        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "status, targetObjectType"));

        Assertions.assertThat(commentService.getCommentsByFilter(filter, pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(c1.getId(), c2.getId()));
    }

    @Test
    public void testGetPublishedCommentsWithFilterWithPagingAndSorting() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);

        testObjectFactory.createComment(user1, article.getId(), BLOCKED, ARTICLE);
        Comment c1 = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user2, article.getId(), APPROVED, ARTICLE);

        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "createdAt"));

        Assertions.assertThat(commentService.getPublishedComments(article.getId(), pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(c1.getId(), c2.getId()));
    }
}
