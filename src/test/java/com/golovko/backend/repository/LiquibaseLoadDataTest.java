package com.golovko.backend.repository;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml")
@Sql(statements = {
        "delete from rating",
        "delete from genre_movie",
        "delete from genre",
        "delete from comment",
        "delete from complaint",
        "delete from article",
        "delete from application_user",
        "delete from movie_cast",
        "delete from movie_crew",
        "delete from person",
        "delete from movie"
        },
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class LiquibaseLoadDataTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    public void testLoadData() {
        Assert.assertTrue(applicationUserRepository.count() > 0);
        Assert.assertTrue(complaintRepository.count() > 0);
        Assert.assertTrue(articleRepository.count() > 0);
        Assert.assertTrue(movieCastRepository.count() > 0);
        Assert.assertTrue(movieCrewRepository.count() > 0);
        Assert.assertTrue(movieRepository.count() > 0);
        Assert.assertTrue(personRepository.count() > 0);
        Assert.assertTrue(commentRepository.count() > 0);
        Assert.assertTrue(genreRepository.count() > 0);
        Assert.assertTrue(ratingRepository.count() > 0);
    }
}
