package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ArticleRepository;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = {"delete from article", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ArticleServiceTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void getArticleTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Article article = testObjectFactory.createArticle(user, now);

        ArticleReadDTO readDTO = articleService.getArticle(article.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(article, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), article.getAuthor().getId());
    }

    @Transactional // FIXME remove this annotation
    @Test
    public void getArticleExtendedTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Article article = testObjectFactory.createArticle(user, now);

        ArticleReadExtendedDTO readDTO = articleService.getArticleExtended(article.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(article, "author");
        Assertions.assertThat(readDTO.getAuthor()).isEqualToComparingFieldByField(article.getAuthor());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getArticleWrongIdTest() {
        articleService.getArticle(UUID.randomUUID());
    }

    @Test
    public void createArticleTest() {
        ArticleCreateDTO createDTO = new ArticleCreateDTO();
        createDTO.setTitle("Text title");
        createDTO.setText("Some text");
        createDTO.setStatus(ArticleStatus.DRAFT);

        ApplicationUser author = testObjectFactory.createUser();

        ArticleReadDTO readDTO = articleService.createArticle(createDTO, author);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Article article = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(article, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), article.getAuthor().getId());
    }

    @Test
    public void updateArticleTest() {
        ArticlePutDTO updateDTO = new ArticlePutDTO();
        updateDTO.setTitle("Title");
        updateDTO.setText("Some text");
        updateDTO.setStatus(ArticleStatus.PUBLISHED);

        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, Instant.now());

        ArticleReadDTO readDTO = articleService.updateArticle(article.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        article = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(article).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(article.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void patchArticleTest() {
        ArticlePatchDTO patchDTO = new ArticlePatchDTO();
        patchDTO.setTitle("Article title");
        patchDTO.setText("Article text");
        patchDTO.setStatus(ArticleStatus.NEED_MODERATION);

        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, Instant.now());

        ArticleReadDTO readDTO = articleService.patchArticle(article.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        Article articleAfterUpdate = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(articleAfterUpdate).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(articleAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void patchArticleEmptyPatchTest() {
        ArticlePatchDTO patchDTO = new ArticlePatchDTO();

        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, Instant.parse("2016-08-18T06:17:10.225Z"));

        ArticleReadDTO readDTO = articleService.patchArticle(article.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        inTransaction(() -> {
            Article articleAfterUpdate = articleRepository.findById(readDTO.getId()).get();
            Assertions.assertThat(articleAfterUpdate).hasNoNullFieldsOrProperties();
            Assertions.assertThat(articleAfterUpdate).isEqualToIgnoringGivenFields(article, "author");
            Assert.assertEquals(articleAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
        });
    }

    @Test
    public void deleteArticleTest() {
        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, Instant.now());
        articleService.deleteArticle(article.getId());

        Assert.assertFalse(articleRepository.existsById(article.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteComplaintNotFound() {
        articleService.deleteArticle(UUID.randomUUID());
    }

    private void inTransaction(Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}