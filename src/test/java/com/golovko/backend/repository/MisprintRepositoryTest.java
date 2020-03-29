package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;
import java.util.stream.Stream;

import static com.golovko.backend.domain.TargetObjectType.MOVIE;

public class MisprintRepositoryTest extends BaseTest {

    @Autowired
    private MisprintRepository misprintRepository;

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser author = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Misprint misprint = testObjectFactory.createMisprint(movie.getId(), MOVIE, author, "misprint");

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
        Misprint misprint = testObjectFactory.createMisprint(movie.getId(), MOVIE, author, "misprint");

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
        Misprint misprint = testObjectFactory.createMisprint(movie.getId(), MOVIE, user1, "misprint");
        testObjectFactory.createMisprint(movie.getId(), MOVIE, user2, "misprint");

        Misprint savedMisprint = misprintRepository.findByIdAndAuthorId(misprint.getId(), user1.getId());

        Assert.assertEquals(savedMisprint.getId(), misprint.getId());
    }

    @Test
    public void testGetMisprintsByAuthorId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie1.getId(), MOVIE, user1, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(movie2.getId(), MOVIE, user1, "misprint");
        testObjectFactory.createMisprint(movie1.getId(), MOVIE, user2, "misprint");
        testObjectFactory.createMisprint(movie2.getId(), MOVIE, user2, "misprint");
        testObjectFactory.createMisprint(movie2.getId(), MOVIE, user2, "misprint");

        Page<Misprint> result = misprintRepository.findByAuthorId(user1.getId(), Pageable.unpaged());

        Assertions.assertThat(result).extracting(Misprint::getId)
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetAllMisprintsByTargetId() {
        ApplicationUser user1 = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie1.getId(), MOVIE, user1, "misprint");
        Misprint m2 = testObjectFactory.createMisprint(movie1.getId(), MOVIE, user1, "misprint");
        testObjectFactory.createMisprint(movie2.getId(), MOVIE, user1, "misprint");
        testObjectFactory.createMisprint(movie2.getId(), MOVIE, user1, "misprint");

        Page<Misprint> expectedResult = misprintRepository
                .findAllByTargetObjectId(movie1.getId(), Pageable.unpaged());

        Assertions.assertThat(expectedResult).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetMisprintByIdAndTargetId() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Misprint m1 = testObjectFactory.createMisprint(movie.getId(), MOVIE, user, "misprint");
        m1.setTargetObjectId(movie.getId());
        misprintRepository.save(m1);

        testObjectFactory.createMisprint(movie.getId(), MOVIE, user, "misprint");

        Misprint misprintFromDb = misprintRepository.findByIdAndTargetObjectId(m1.getId(), movie.getId());

        Assert.assertEquals(misprintFromDb.getId(), m1.getId());
    }

    @Test
    public void testFindSimilarMisprints() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();

        Misprint m1 = createMisprint(movie1.getId(), MOVIE, user, "misprint", ComplaintStatus.INITIATED);
        Misprint m2 = createMisprint(movie1.getId(), MOVIE, user, "misprint", ComplaintStatus.INITIATED);
        createMisprint(movie1.getId(), MOVIE, user, "Misprint", ComplaintStatus.DUPLICATE);
        createMisprint(movie1.getId(), MOVIE, user, "misprint", ComplaintStatus.DUPLICATE);
        createMisprint(movie2.getId(), MOVIE, user, "misprint", ComplaintStatus.INITIATED);

        transactionTemplate.executeWithoutResult(status -> {
            Stream<Misprint> misprints = misprintRepository
                    .findSimilarMisprints(movie1.getId(), "misprint", ComplaintStatus.INITIATED);

            Assertions.assertThat(misprints).extracting("id")
                    .containsExactlyInAnyOrder(m1.getId(), m2.getId());
        });
    }

    private Misprint createMisprint(
            UUID targetObjectId,
            TargetObjectType targetObjectType,
            ApplicationUser author,
            String misprintText,
            ComplaintStatus status
    ) {
        Misprint misprint = new Misprint();
        misprint.setMisprintText(misprintText);
        misprint.setReplaceTo("Some report text");
        misprint.setStatus(status);
        misprint.setAuthor(author);
        misprint.setModerator(null);
        misprint.setTargetObjectId(targetObjectId);
        misprint.setTargetObjectType(targetObjectType);
        return misprintRepository.save(misprint);
    }
}
