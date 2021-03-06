package com.golovko.backend.job;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.service.MovieService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.MOVIE;

public class UpdateAverageRatingOfMoviesJobTest extends BaseTest {

    @Autowired
    private UpdateAverageRatingOfMoviesJob updateAverageRatingOfMoviesJob;

    @Autowired
    private MovieRepository movieRepository;

    @SpyBean
    private MovieService movieService;

    @Test
    public void testUpdateAverageRatingOfMovies() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        testObjectFactory.createRating(3, u1, movie.getId(), MOVIE);
        testObjectFactory.createRating(6, u2, movie.getId(), MOVIE);

        updateAverageRatingOfMoviesJob.updateAverageRating();

        movie = movieRepository.findById(movie.getId()).get();
        Assert.assertEquals(4.5, movie.getAverageRating(), Double.MIN_NORMAL);
    }

    @Test
    public void testUpdateMoviesIndependently() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();

        testObjectFactory.createRating(3, u1, m1.getId(), MOVIE);
        testObjectFactory.createRating(6, u2, m2.getId(), MOVIE);

        UUID[] failedId = new UUID[1];
        Mockito.doAnswer(invocationOnMock -> {
            if (failedId[0] == null) {
                failedId[0] = invocationOnMock.getArgument(0);
                throw new RuntimeException();
            }
            return invocationOnMock.callRealMethod();
        }).when(movieService).updateAverageRatingOfMovie(Mockito.any());

        updateAverageRatingOfMoviesJob.updateAverageRating();

        for (Movie movie : movieRepository.findAll()) {
            if (movie.getId().equals(failedId[0])) {
                Assert.assertNull(movie.getAverageRating());
            } else {
                Assert.assertNotNull(movie.getAverageRating());
            }
        }
    }
}
