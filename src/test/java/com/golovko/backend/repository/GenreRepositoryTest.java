package com.golovko.backend.repository;

import com.golovko.backend.domain.Genre;
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

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from genre"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class GenreRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private GenreRepository genreRepository;

    @Test
    public void testCreateAtIsSet() {
        Genre genre = testObjectFactory.createGenre("Comedy");

        Instant createdAtBeforeReload = genre.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        genre = genreRepository.findById(genre.getId()).get();

        Instant createdAtAfterReload = genre.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testModifiedAtIsSet() {
        Genre genre = testObjectFactory.createGenre("Comedy");

        Instant modifiedAtBeforeReload = genre.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        genre = genreRepository.findById(genre.getId()).get();
        genre.setDescription("Another Description");
        genre = genreRepository.save(genre);
        Instant modifiedAtAfterReload = genre.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testFindAllByOrderByGenreNameAsc() {
        Genre g1 = testObjectFactory.createGenre("Thriller");
        Genre g2 = testObjectFactory.createGenre("Horror");
        Genre g3 = testObjectFactory.createGenre("Comedy");
        Genre g4 = testObjectFactory.createGenre("Fantasy");

        List<Genre> genres = genreRepository.findAllByOrderByGenreNameAsc();

        Assertions.assertThat(genres).extracting(Genre::getId)
                .containsSequence(g3.getId(), g4.getId(), g2.getId(), g1.getId());

    }
}
