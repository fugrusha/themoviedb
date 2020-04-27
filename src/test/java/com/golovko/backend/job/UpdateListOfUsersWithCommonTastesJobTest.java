package com.golovko.backend.job;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.service.UserMatchService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.UUID;

public class UpdateListOfUsersWithCommonTastesJobTest extends BaseTest {

    @Autowired
    private UpdateListOfUsersWithCommonTastesJob job;

    @SpyBean
    private UserMatchService userMatchService;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Test
    public void testUpdateListOfUsersWithCommonTastes() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        ApplicationUser u3 = testObjectFactory.createUser();
        ApplicationUser u4 = testObjectFactory.createUser();

        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        Movie m3 = testObjectFactory.createMovie();
        Movie m4 = testObjectFactory.createMovie();

        testObjectFactory.createRating(6, u1, m1.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(10, u1, m3.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(7, u1, m2.getId(), TargetObjectType.MOVIE);

        testObjectFactory.createRating(4, u2, m1.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(7, u2, m2.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(8, u2, m3.getId(), TargetObjectType.MOVIE);

        testObjectFactory.createRating(9, u3, m4.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(4, u3, m3.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(5, u3, m1.getId(), TargetObjectType.MOVIE);

        testObjectFactory.createRating(8, u4, m4.getId(), TargetObjectType.MOVIE);

        job.updateListOfUsersWithCommonTastes();

        inTransaction(() -> {
            ApplicationUser updatedUser1 = applicationUserRepository.findById(u1.getId()).get();
            Assert.assertEquals(3, updatedUser1.getTopMatches().size());
            Assertions.assertThat(updatedUser1.getTopMatches()).extracting("id")
                    .doesNotContain(u1.getId());

            ApplicationUser updatedUser2 = applicationUserRepository.findById(u2.getId()).get();
            Assert.assertEquals(3, updatedUser2.getTopMatches().size());
            Assertions.assertThat(updatedUser2.getTopMatches()).extracting("id")
                    .doesNotContain(u2.getId());

            ApplicationUser updatedUser3 = applicationUserRepository.findById(u3.getId()).get();
            Assert.assertEquals(3, updatedUser3.getTopMatches().size());
            Assertions.assertThat(updatedUser3.getTopMatches()).extracting("id")
                    .doesNotContain(u3.getId());

            ApplicationUser updatedUser4 = applicationUserRepository.findById(u4.getId()).get();
            Assert.assertEquals(3, updatedUser4.getTopMatches().size());
            Assertions.assertThat(updatedUser4.getTopMatches()).extracting("id")
                    .doesNotContain(u4.getId());
        });
    }

    @Test
    public void testUpdateUserIndependently() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();

        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        Movie m3 = testObjectFactory.createMovie();

        testObjectFactory.createRating(6, u1, m1.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(10, u1, m2.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(7, u1, m3.getId(), TargetObjectType.MOVIE);

        testObjectFactory.createRating(4, u2, m1.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(7, u2, m2.getId(), TargetObjectType.MOVIE);
        testObjectFactory.createRating(8, u2, m3.getId(), TargetObjectType.MOVIE);

        UUID[] failedId = new UUID[1];
        Mockito.doAnswer(invocationOnMock -> {
            if (failedId[0] == null) {
                failedId[0] = invocationOnMock.getArgument(0);
                throw new RuntimeException();
            }
            return invocationOnMock.callRealMethod();
        }).when(userMatchService).updateUserTopMatches(Mockito.any());

        job.updateListOfUsersWithCommonTastes();

        inTransaction(() -> {
            for (ApplicationUser user : applicationUserRepository.findAll()) {
                if (user.getId().equals(failedId[0])) {
                    Assert.assertTrue(user.getTopMatches().isEmpty());
                } else {
                    Assert.assertFalse(user.getTopMatches().isEmpty());
                }
            }
        });
    }
}
