package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "spring.liquibase.change-log=classpath:db/changelog/db.changelog-master.xml")
public class LiquibaseLoadDataTest extends BaseTest {

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

    @Autowired
    private MisprintRepository misprintRepository;

    @Autowired
    private LikeRepository likeRepository;

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
        Assert.assertTrue(misprintRepository.count() > 0);
        Assert.assertTrue(likeRepository.count() > 0);
    }
}
