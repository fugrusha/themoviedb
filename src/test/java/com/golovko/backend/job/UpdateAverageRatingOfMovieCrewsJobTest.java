package com.golovko.backend.job;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCrew;
import com.golovko.backend.domain.Person;
import com.golovko.backend.repository.MovieCrewRepository;
import com.golovko.backend.service.MovieCrewService;
import com.golovko.backend.util.TestObjectFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.MOVIE_CREW;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {
        "delete from rating",
        "delete from application_user",
        "delete from person",
        "delete from movie",
        "delete from movie_crew"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UpdateAverageRatingOfMovieCrewsJobTest {

    @Autowired
    private UpdateAverageRatingOfMovieCrewsJob updateAverageRatingOfMovieCrewsJob;

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @SpyBean
    private MovieCrewService movieCrewService;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Test
    public void testUpdateAverageRatingOfMovieCrews() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();

        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        testObjectFactory.createRating(3, u1, movieCrew.getId(), MOVIE_CREW);
        testObjectFactory.createRating(6, u2, movieCrew.getId(), MOVIE_CREW);

        updateAverageRatingOfMovieCrewsJob.updateAverageRatingOfMovieCrews();

        movieCrew = movieCrewRepository.findById(movieCrew.getId()).get();
        Assert.assertEquals(4.5, movieCrew.getAverageRating(), Double.MIN_NORMAL);
    }

    @Test
    public void testMovieCrewsUpdateIndependently() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();

        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        testObjectFactory.createRating(3, u1, movieCrew.getId(), MOVIE_CREW);
        testObjectFactory.createRating(6, u2, movieCrew.getId(), MOVIE_CREW);

        UUID[] failedId = new UUID[1];
        Mockito.doAnswer(invocationOnMock -> {
            if (failedId[0] == null) {
                failedId[0] = invocationOnMock.getArgument(0);
                throw new RuntimeException();
            }
            return invocationOnMock.callRealMethod();
        }).when(movieCrewService).updateAverageRatingOfMovieCrew(Mockito.any());

        updateAverageRatingOfMovieCrewsJob.updateAverageRatingOfMovieCrews();

        for (MovieCrew mc : movieCrewRepository.findAll()) {
            if (mc.getId().equals(failedId[0])) {
                Assert.assertNull(mc.getAverageRating());
            } else {
                Assert.assertNotNull(mc.getAverageRating());
            }
        }
    }
}
