package com.golovko.backend.job;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.Person;
import com.golovko.backend.repository.MovieCastRepository;
import com.golovko.backend.service.MovieCastService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.MOVIE_CAST;

public class UpdateAverageRatingOfMovieCastsJobTest extends BaseTest {

    @Autowired
    private UpdateAverageRatingOfMovieCastsJob updateAverageRatingOfMovieCastsJob;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @SpyBean
    private MovieCastService movieCastService;

    @Test
    public void testUpdateAverageRatingOfMovieCasts() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();

        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        testObjectFactory.createRating(3, u1, movieCast.getId(), MOVIE_CAST);
        testObjectFactory.createRating(6, u2, movieCast.getId(), MOVIE_CAST);

        updateAverageRatingOfMovieCastsJob.updateAverageRatingOfMovieCast();

        movieCast = movieCastRepository.findById(movieCast.getId()).get();
        Assert.assertEquals(4.5, movieCast.getAverageRating(), Double.MIN_NORMAL);
    }

    @Test
    public void testMovieCastsUpdateIndependently() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();

        MovieCast movieCast1 = testObjectFactory.createMovieCast(person, movie1);
        MovieCast movieCast2 = testObjectFactory.createMovieCast(person, movie2);

        testObjectFactory.createRating(3, u1, movieCast1.getId(), MOVIE_CAST);
        testObjectFactory.createRating(6, u2, movieCast1.getId(), MOVIE_CAST);
        testObjectFactory.createRating(6, u2, movieCast2.getId(), MOVIE_CAST);

        UUID[] failedId = new UUID[1];
        Mockito.doAnswer(invocationOnMock -> {
            if (failedId[0] == null) {
                failedId[0] = invocationOnMock.getArgument(0);
                throw new RuntimeException();
            }
            return invocationOnMock.callRealMethod();
        }).when(movieCastService).updateAverageRatingOfMovieCast(Mockito.any());

        updateAverageRatingOfMovieCastsJob.updateAverageRatingOfMovieCast();

        for (MovieCast mc : movieCastRepository.findAll()) {
            if (mc.getId().equals(failedId[0])) {
                Assert.assertNull(mc.getAverageRating());
            } else {
                Assert.assertNotNull(mc.getAverageRating());
            }
        }
    }
}
