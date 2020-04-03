package com.golovko.backend.job;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.service.MovieService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.UUID;

public class UpdateReleasedStatusOfMoviesJobTest extends BaseTest {

    @Autowired
    private UpdateReleasedStatusOfMoviesJob updateReleasedStatusOfMoviesJob;

    @SpyBean
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    public void testUpdateReleasedStatusOfMoviesJob() {
        Movie m1 = testObjectFactory.createMovie();
        m1.setIsReleased(false);
        movieRepository.save(m1);

        updateReleasedStatusOfMoviesJob.updateReleasedStatus();

        m1 = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals(true, m1.getIsReleased());
    }

    @Test
    public void testReleasedStatusUpdatedIndependently() {
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        m1.setIsReleased(false);
        m2.setIsReleased(false);
        movieRepository.saveAll(List.of(m1, m2));

        UUID[] failedId = new UUID[1];
        Mockito.doAnswer(invocationOnMock -> {
            if (failedId[0] == null) {
                failedId[0] = invocationOnMock.getArgument(0);
                throw new RuntimeException();
            }
            return invocationOnMock.callRealMethod();
        }).when(movieService).updateReleasedStatusOfMovie(Mockito.any());

        updateReleasedStatusOfMoviesJob.updateReleasedStatus();

        for (Movie m : movieRepository.findAll()) {
            if (m.getId().equals(failedId[0])) {
                Assert.assertFalse(m.getIsReleased());
            } else {
                Assert.assertTrue(m.getIsReleased());
            }
        }
    }
}
