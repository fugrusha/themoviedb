package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.util.TestObjectFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from article", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ArticleRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private ArticleRepository articleRepository;

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
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));    }
}
