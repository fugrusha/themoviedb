package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.Like;
import com.golovko.backend.dto.like.LikeCreateDTO;
import com.golovko.backend.dto.like.LikePatchDTO;
import com.golovko.backend.dto.like.LikePutDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.LikeRepository;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.ARTICLE;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {
        "delete from like",
        "delete from article",
        "delete from user_role",
        "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class LikeServiceTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private LikeService likeService;

    @Test
    public void testGetRatingById() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Like like = testObjectFactory.createLike(true, user, article.getId());

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

    @Test
    public void testPatchLike() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);
        Like like = testObjectFactory.createLike(true, user, article.getId());

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
        Like like = testObjectFactory.createLike(true, user, article.getId());

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
        Like like = testObjectFactory.createLike(true, user, article.getId());

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
        Like like = testObjectFactory.createLike(true, user, article.getId());

        likeService.deleteLike(user.getId(), like.getId());

        Assert.assertFalse(likeRepository.existsById(like.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteLikeNotFound() {
        likeService.deleteLike(UUID.randomUUID(), UUID.randomUUID());
    }
}
