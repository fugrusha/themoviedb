package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Article;
import com.golovko.backend.dto.article.ArticleReadDTO;
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

    @Test
    public void getArticleTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        Article article = testObjectFactory.createArticle(user, now);

        ArticleReadDTO readDTO = articleService.getArticle(article.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(article, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), article.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getComplaintWrongIdTest() {
        articleService.getArticle(UUID.randomUUID());
    }
}
