package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ArticleRepository;
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

import java.util.*;

import static com.golovko.backend.domain.TargetObjectType.ARTICLE;

public class ArticleServiceTest extends BaseTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Test
    public void testGetArticleById() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        ArticleReadDTO readDTO = articleService.getArticle(article.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(article, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), article.getAuthor().getId());
    }

    @Test
    public void testGetArticleExtended() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, ArticleStatus.PUBLISHED);

        ArticleReadExtendedDTO readDTO = articleService.getArticleExtended(article.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(article, "author");
        Assertions.assertThat(readDTO.getAuthor()).isEqualToComparingFieldByField(article.getAuthor());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetArticleWrongId() {
        articleService.getArticle(UUID.randomUUID());
    }

    @Test
    public void testGetAllArticles() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Article a2 = testObjectFactory.createArticle(user2, ArticleStatus.PUBLISHED);
        testObjectFactory.createArticle(user1, ArticleStatus.DRAFT);
        testObjectFactory.createArticle(user2, ArticleStatus.NEED_MODERATION);
        testObjectFactory.createArticle(user2, ArticleStatus.DRAFT);

        PageResult<ArticleReadDTO> articles = articleService.getAllPublishedArticles(Pageable.unpaged());

        Assertions.assertThat(articles.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId(), a2.getId());
    }

    @Test
    public void testGetArticlesWithPagingAndSorting() {
        ApplicationUser user1 = testObjectFactory.createUser();
        testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Article a3 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);
        Article a4 = testObjectFactory.createArticle(user1, ArticleStatus.PUBLISHED);

        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Assertions.assertThat(articleService.getAllPublishedArticles(pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(a4.getId(), a3.getId()));
    }

    @Test
    public void testCreateArticle() {
        ApplicationUser author = testObjectFactory.createUser();

        ArticleCreateDTO createDTO = new ArticleCreateDTO();
        createDTO.setTitle("Text title");
        createDTO.setText("Some text");
        createDTO.setStatus(ArticleStatus.DRAFT);
        createDTO.setAuthorId(author.getId());

        ArticleReadDTO readDTO = articleService.createArticle(createDTO);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Article article = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(article, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), article.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateArticleWrongAuthor() {
        ArticleCreateDTO createDTO = new ArticleCreateDTO();
        createDTO.setTitle("Text title");
        createDTO.setText("Some text");
        createDTO.setStatus(ArticleStatus.DRAFT);
        createDTO.setAuthorId(UUID.randomUUID());

        articleService.createArticle(createDTO);
    }

    @Test
    public void testUpdateArticle() {
        ArticlePutDTO updateDTO = new ArticlePutDTO();
        updateDTO.setTitle("Title");
        updateDTO.setText("Some text");
        updateDTO.setStatus(ArticleStatus.PUBLISHED);

        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);

        ArticleReadDTO readDTO = articleService.updateArticle(article.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        article = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(article).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(article.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testPatchArticle() {
        ArticlePatchDTO patchDTO = new ArticlePatchDTO();
        patchDTO.setTitle("Article title");
        patchDTO.setText("Article text");
        patchDTO.setStatus(ArticleStatus.NEED_MODERATION);

        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);

        ArticleReadDTO readDTO = articleService.patchArticle(article.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        Article articleAfterUpdate = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(articleAfterUpdate).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(articleAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testPatchArticleEmptyPatch() {
        ArticlePatchDTO patchDTO = new ArticlePatchDTO();

        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);

        ArticleReadDTO readDTO = articleService.patchArticle(article.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        Article articleAfterUpdate = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(articleAfterUpdate).hasNoNullFieldsOrProperties();
        Assertions.assertThat(articleAfterUpdate).isEqualToIgnoringGivenFields(article, "author");
        Assert.assertEquals(articleAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testDeleteArticle() {
        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);
        articleService.deleteArticle(article.getId());

        Assert.assertFalse(articleRepository.existsById(article.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteArticleNotFound() {
        articleService.deleteArticle(UUID.randomUUID());
    }

    @Test
    public void testDeleteArticleWithCompositeItems() {
        ApplicationUser author = testObjectFactory.createUser();
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);

        Comment c1 = testObjectFactory.createComment(user, article.getId(), CommentStatus.APPROVED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user, article.getId(), CommentStatus.APPROVED, ARTICLE);

        Like like1 = testObjectFactory.createLike(true, user, article.getId(), ARTICLE);
        Like like2 = testObjectFactory.createLike(true, author, article.getId(), ARTICLE);

        articleService.deleteArticle(article.getId());

        Assert.assertFalse(articleRepository.existsById(article.getId()));

        Assert.assertFalse(commentRepository.existsById(c1.getId()));
        Assert.assertFalse(commentRepository.existsById(c2.getId()));

        Assert.assertFalse(likeRepository.existsById(like1.getId()));
        Assert.assertFalse(likeRepository.existsById(like2.getId()));
    }

    @Test
    public void testGetArticlesByEmptyFilter() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        Article a2 = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);
        Article a3 = testObjectFactory.createArticle(author, ArticleStatus.BLOCKED);

        ArticleManagerFilter filter = new ArticleManagerFilter();

        PageResult<ArticleReadDTO> actualResult = articleService.getArticlesByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId(), a2.getId(), a3.getId());
    }

    @Test
    public void testGetArticlesByFilterWithEmptySet() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        Article a2 = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);
        Article a3 = testObjectFactory.createArticle(author, ArticleStatus.BLOCKED);

        ArticleManagerFilter filter = new ArticleManagerFilter();
        filter.setStatuses(new HashSet<ArticleStatus>());

        PageResult<ArticleReadDTO> actualResult = articleService.getArticlesByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId(), a2.getId(), a3.getId());
    }

    @Test
    public void testGetArticlesByAuthor() {
        ApplicationUser author1 = testObjectFactory.createUser();
        ApplicationUser author2 = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author1, ArticleStatus.PUBLISHED);
        Article a2 = testObjectFactory.createArticle(author1, ArticleStatus.DRAFT);
        testObjectFactory.createArticle(author2, ArticleStatus.BLOCKED);
        testObjectFactory.createArticle(author2, ArticleStatus.PUBLISHED);

        ArticleManagerFilter filter = new ArticleManagerFilter();
        filter.setAuthorId(author1.getId());

        PageResult<ArticleReadDTO> actualResult = articleService.getArticlesByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId(), a2.getId());
    }

    @Test
    public void testGetArticlesByStatus() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);
        testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        testObjectFactory.createArticle(author, ArticleStatus.BLOCKED);

        ArticleManagerFilter filter = new ArticleManagerFilter();
        filter.setStatuses(Set.of(ArticleStatus.DRAFT));

        PageResult<ArticleReadDTO> actualResult = articleService.getArticlesByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId());
    }

    @Test
    public void testGetArticlesByAllFilters() {
        ApplicationUser author1 = testObjectFactory.createUser();
        ApplicationUser author2 = testObjectFactory.createUser();

        Article a1 = testObjectFactory.createArticle(author1, ArticleStatus.DRAFT);
        testObjectFactory.createArticle(author1, ArticleStatus.PUBLISHED);
        testObjectFactory.createArticle(author1, ArticleStatus.BLOCKED);
        testObjectFactory.createArticle(author2, ArticleStatus.DRAFT);

        ArticleManagerFilter filter = new ArticleManagerFilter();
        filter.setAuthorId(author1.getId());
        filter.setStatuses(Set.of(ArticleStatus.DRAFT));

        PageResult<ArticleReadDTO> actualResult = articleService.getArticlesByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId());
    }

    @Test
    public void testGetArticlesWithEmptyFilterWithPagingAndSorting() {
        ApplicationUser author = testObjectFactory.createUser();

        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a1.setTitle("Begin ....");
        Article a2 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a2.setTitle("Mandatory ....");
        Article a3 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a3.setTitle("From ....");
        articleRepository.saveAll(List.of(a1, a2, a3));

        ArticleManagerFilter filter = new ArticleManagerFilter();
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "title"));
        Assertions.assertThat(articleService.getArticlesByFilter(filter, pageRequest).getData())
                .extracting("id").isEqualTo(Arrays.asList(a2.getId(), a3.getId()));
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveArticleNotNullValidation() {
        Article article = new Article();
        articleRepository.save(article);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveArticleMaxSizeValidation() {
        ApplicationUser author = testObjectFactory.createUser();

        Article article = new Article();
        article.setTitle("article title".repeat(100));
        article.setText("long long text".repeat(1000));
        article.setStatus(ArticleStatus.DRAFT);
        article.setAuthor(author);
        articleRepository.save(article);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveArticleMinSizeValidation() {
        ApplicationUser author = testObjectFactory.createUser();

        Article article = new Article();
        article.setTitle("");
        article.setText("");
        article.setStatus(ArticleStatus.DRAFT);
        article.setAuthor(author);
        articleRepository.save(article);
    }

}
