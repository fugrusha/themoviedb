package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from article", "delete from user_role", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ArticleRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);

        Instant createdAtBeforeReload = article.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        article = articleRepository.findById(article.getId()).get();

        Instant createdAtAfterReload = article.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        ApplicationUser author = testObjectFactory.createUser();
        Article article = new Article();
        article.setText("text");
        article.setTitle("title");
        article.setStatus(ArticleStatus.DRAFT);
        article.setAuthor(author);
        article = articleRepository.save(article);

        Instant modifiedAtBeforeReload = article.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        article = articleRepository.findById(article.getId()).get();
        article.setStatus(ArticleStatus.PUBLISHED);
        article = articleRepository.save(article);
        Instant modifiedAtAfterReload = article.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testGetArticlesByStatus() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);
        Article a2 = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);
        Article a3 = testObjectFactory.createArticle(author, ArticleStatus.DRAFT);
        testObjectFactory.createArticle(author, ArticleStatus.NEED_MODERATION);

        List<Article> articles = articleRepository.findByStatusOrderByCreatedAtDesc(ArticleStatus.DRAFT);

        Assertions.assertThat(articles).extracting("id")
                .containsExactlyInAnyOrder(a1.getId(), a2.getId(), a3.getId());
    }

    @Test
    public void testIncrementLikesCountField() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a1.setLikesCount(5);
        articleRepository.save(a1);

        transactionTemplate.executeWithoutResult(status -> {
            articleRepository.incrementLikesCountField(a1.getId());
        });

        Article updatedArticle = articleRepository.findById(a1.getId()).get();
        Assert.assertEquals((Integer) 6, updatedArticle.getLikesCount());
    }

    @Test
    public void testDecrementLikesCountField() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a1.setLikesCount(5);
        articleRepository.save(a1);

        transactionTemplate.executeWithoutResult(status -> {
            articleRepository.decrementLikesCountField(a1.getId());
        });

        Article updatedArticle = articleRepository.findById(a1.getId()).get();
        Assert.assertEquals((Integer) 4, updatedArticle.getLikesCount());
    }

    @Test
    public void testIncrementDislikesCountField() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a1.setDislikesCount(5);
        articleRepository.save(a1);

        transactionTemplate.executeWithoutResult(status -> {
            articleRepository.incrementDislikesCountField(a1.getId());
        });

        Article updatedArticle = articleRepository.findById(a1.getId()).get();
        Assert.assertEquals((Integer) 6, updatedArticle.getDislikesCount());
    }

    @Test
    public void testDecrementDislikesCountField() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, ArticleStatus.PUBLISHED);
        a1.setDislikesCount(5);
        articleRepository.save(a1);

        transactionTemplate.executeWithoutResult(status -> {
            articleRepository.decrementDislikesCountField(a1.getId());
        });

        Article updatedArticle = articleRepository.findById(a1.getId()).get();
        Assert.assertEquals((Integer) 4, updatedArticle.getDislikesCount());
    }
}
