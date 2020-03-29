package com.golovko.backend.job;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Person;
import com.golovko.backend.repository.PersonRepository;
import com.golovko.backend.service.PersonService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.UUID;

public class UpdateAverageRatingOfPersonMoviesJobTest extends BaseTest {

    @Autowired
    private UpdateAverageRatingOfPersonMoviesJob updateAverageRatingOfPersonMoviesJob;

    @Autowired
    private PersonRepository personRepository;

    @SpyBean
    private PersonService personService;

    @Test
    public void testUpdateAverageRatingOfPersonMoviesJob() {
        Person p1 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie(3.0);
        Movie m2 = testObjectFactory.createMovie(4.5);

        testObjectFactory.createMovieCast(p1, m1);
        testObjectFactory.createMovieCast(p1, m2);

        updateAverageRatingOfPersonMoviesJob.updateAverageRating();

        p1 = personRepository.findById(p1.getId()).get();
        Assert.assertEquals(3.75, p1.getAverageRatingByMovies(), Double.MIN_NORMAL);
    }

    @Test
    public void testPersonUpdatedIndependently() {
        Person p1 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie(3.0);
        Movie m2 = testObjectFactory.createMovie(4.5);

        testObjectFactory.createMovieCast(p1, m1);
        testObjectFactory.createMovieCast(p1, m2);

        UUID[] failedId = new UUID[1];
        Mockito.doAnswer(invocationOnMock -> {
            if (failedId[0] == null) {
                failedId[0] = invocationOnMock.getArgument(0);
                throw new RuntimeException();
            }
            return invocationOnMock.callRealMethod();
        }).when(personService).updateAverageRatingOfPersonMovies(Mockito.any());

        updateAverageRatingOfPersonMoviesJob.updateAverageRating();

        for (Person p : personRepository.findAll()) {
            if (p.getId().equals(failedId[0])) {
                Assert.assertNull(p.getAverageRatingByMovies());
            } else {
                Assert.assertNotNull(p.getAverageRatingByMovies());
            }
        }
    }
}
