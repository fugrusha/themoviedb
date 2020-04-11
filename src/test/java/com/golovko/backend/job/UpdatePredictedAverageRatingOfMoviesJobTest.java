package com.golovko.backend.job;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Person;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.service.MovieService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDate;
import java.util.UUID;

public class UpdatePredictedAverageRatingOfMoviesJobTest extends BaseTest {

    @Autowired
    private UpdatePredictedAverageRatingOfMoviesJob updatePredictedAverageRatingOfMoviesJob;

    @Autowired
    private MovieRepository movieRepository;

    @SpyBean
    private MovieService movieService;

    @Test
    public void testUpdatePredictedAverageRatingOfMovies() {
        Movie m1 = testObjectFactory.createMovie(LocalDate.of(2025,10, 5), false);
        Person p1 = testObjectFactory.createPerson(5.0);
        Person p2 = testObjectFactory.createPerson(3.0);

        testObjectFactory.createMovieCast(p1, m1);
        testObjectFactory.createMovieCast(p2, m1);

        updatePredictedAverageRatingOfMoviesJob.updatePredictedAverageRating();

        Movie movie = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals(4.0, movie.getPredictedAverageRating(), Double.MIN_NORMAL);
    }

    @Test
    public void testUpdateMoviesIndependently() {
        Movie m1 = testObjectFactory.createMovie(LocalDate.of(2025,10, 5), false);
        Movie m2 = testObjectFactory.createMovie(LocalDate.of(2030,10, 5), false);
        Person p1 = testObjectFactory.createPerson(5.0);
        Person p2 = testObjectFactory.createPerson(3.0);

        testObjectFactory.createMovieCast(p1, m1);
        testObjectFactory.createMovieCast(p2, m1);
        testObjectFactory.createMovieCast(p1, m2);
        testObjectFactory.createMovieCast(p2, m2);

        UUID[] failedId = new UUID[1];
        Mockito.doAnswer(invocationOnMock -> {
            if (failedId[0] == null) {
                failedId[0] = invocationOnMock.getArgument(0);
                throw new RuntimeException();
            }
            return invocationOnMock.callRealMethod();
        }).when(movieService).updatePredictedAverageRatingOfMovie(Mockito.any());

        updatePredictedAverageRatingOfMoviesJob.updatePredictedAverageRating();

        for (Movie movie : movieRepository.findAll()) {
            if (movie.getId().equals(failedId[0])) {
                Assert.assertNull(movie.getPredictedAverageRating());
            } else {
                Assert.assertNotNull(movie.getPredictedAverageRating());
            }
        }
    }
}
