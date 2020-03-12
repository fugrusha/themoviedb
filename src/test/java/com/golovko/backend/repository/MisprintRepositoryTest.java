package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Misprint;
import com.golovko.backend.domain.Movie;
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
import java.util.List;

import static com.golovko.backend.domain.TargetObjectType.MOVIE;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {
        "delete from movie",
        "delete from misprint",
        "delete from user_role",
        "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MisprintRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private MisprintRepository misprintRepository;

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser author = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Misprint misprint = testObjectFactory.createMisprint(movie.getId(), MOVIE, author);

        Instant createdAtBeforeReload = misprint.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        misprint = misprintRepository.findById(misprint.getId()).get();

        Instant createdAtAfterReload = misprint.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        ApplicationUser author = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Misprint misprint = testObjectFactory.createMisprint(movie.getId(), MOVIE, author);

        Instant modifiedAtBeforeReload = misprint.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        misprint = misprintRepository.findById(misprint.getId()).get();
        misprint.setReplaceTo("another text");
        misprint = misprintRepository.save(misprint);
        Instant modifiedAtAfterReload = misprint.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testGetMisprintByIdAndAuthorId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Misprint misprint = testObjectFactory.createMisprint(movie.getId(), MOVIE, user1);
        testObjectFactory.createMisprint(movie.getId(), MOVIE, user2);

        Misprint savedMisprint = misprintRepository.findByIdAndAuthorId(misprint.getId(), user1.getId());

        Assert.assertEquals(savedMisprint.getId(), misprint.getId());
    }

    @Test
    public void testGetMisprintsByAuthorId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie1.getId(), MOVIE, user1);
        Misprint m2 = testObjectFactory.createMisprint(movie2.getId(), MOVIE, user1);
        testObjectFactory.createMisprint(movie1.getId(), MOVIE, user2);
        testObjectFactory.createMisprint(movie2.getId(), MOVIE, user2);
        testObjectFactory.createMisprint(movie2.getId(), MOVIE, user2);

        List<Misprint> result = misprintRepository.findByAuthorIdOrderByCreatedAtAsc(user1.getId());

        Assertions.assertThat(result).extracting(Misprint::getId)
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }
}
