package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Genre;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public class GenreRepositoryTest extends BaseTest {

    @Autowired
    private GenreRepository genreRepository;

    @Test
    public void testCreatedAtIsSet() {
        Genre genre = testObjectFactory.createGenre("Comedy");

        Instant createdAtBeforeReload = genre.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        genre = genreRepository.findById(genre.getId()).get();

        Instant createdAtAfterReload = genre.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
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


        Page<Genre> genres = genreRepository.findAll(Pageable.unpaged());

        Assertions.assertThat(genres).extracting(Genre::getId)
                .containsExactlyInAnyOrder(g3.getId(), g2.getId(), g1.getId());

    }
}
