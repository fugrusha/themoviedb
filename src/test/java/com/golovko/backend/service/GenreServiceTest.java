package com.golovko.backend.service;

import com.golovko.backend.domain.Genre;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.genre.*;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.GenreRepository;
import com.golovko.backend.repository.MovieRepository;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = {"delete from genre_movie", "delete from genre", "delete from movie"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class GenreServiceTest {

    @Autowired
    private GenreService genreService;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void getGenreTest() {
        Genre genre = testObjectFactory.createGenre("Comedy");

        GenreReadDTO readDTO = genreService.getGenre(genre.getId());

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(genre);
    }

    // TODO to be or not to be // Will filter be better?
    @Test
    public void getExtendedGenreTest() {
        Genre genre = testObjectFactory.createGenre("comedy");
        UUID genreId = genre.getId();

        Movie m1 = testObjectFactory.createMovie();
        m1.setGenres(Set.of(genre));
        Movie m2 = testObjectFactory.createMovie();
        m2.setGenres(Set.of(genre));
        movieRepository.saveAll(List.of(m1, m2));

        testObjectFactory.createMovie();
        testObjectFactory.createMovie();

        GenreReadExtendedDTO extendedDTO = genreService.getExtendedGenre(genre.getId());

        Genre savedGenre = genreRepository.findById(genreId).get();
        Assertions.assertThat(extendedDTO).isEqualToIgnoringGivenFields(savedGenre, "movies");
        Assertions.assertThat(extendedDTO.getMovies()).extracting(MovieReadDTO::getId)
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void getAllGenresTest() {
        Genre g1 = testObjectFactory.createGenre("Thriller");
        Genre g2 = testObjectFactory.createGenre("Horror");
        Genre g3 = testObjectFactory.createGenre("Comedy");
        Genre g4 = testObjectFactory.createGenre("Fantasy");

        List<GenreReadDTO> genres = genreService.getAllGenres();

        Assertions.assertThat(genres).extracting(GenreReadDTO::getId)
                .containsSequence(g3.getId(), g4.getId(), g2.getId(), g1.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getGenreByWrongIdTest() {
        genreService.getGenre(UUID.randomUUID());
    }

    @Test
    public void createGenreTest() {
        GenreCreateDTO createDTO = new GenreCreateDTO();
        createDTO.setGenreName("genre name");
        createDTO.setDescription("some description");

        GenreReadDTO readDTO = genreService.createGenre(createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Genre genre = genreRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(genre);
    }

    @Test
    public void patchGenreTest() {
        Genre genre = testObjectFactory.createGenre("Thriller");

        GenrePatchDTO patchDTO = new GenrePatchDTO();
        patchDTO.setGenreName("new genre");
        patchDTO.setDescription("new text description");

        GenreReadDTO readDTO = genreService.patchGenre(genre.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        Genre updatedGenre = genreRepository.findById(genre.getId()).get();
        Assertions.assertThat(updatedGenre).isEqualToIgnoringGivenFields(readDTO, "movies");
    }

    @Test
    public void patchGenreEmptyPatchTest() {
        Genre genre = testObjectFactory.createGenre("Thriller");

        GenrePatchDTO patchDTO = new GenrePatchDTO();

        GenreReadDTO readDTO = genreService.patchGenre(genre.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        inTransaction(() -> {
            Genre genreAfterUpdate = genreRepository.findById(readDTO.getId()).get();
            Assertions.assertThat(genreAfterUpdate).hasNoNullFieldsOrPropertiesExcept("movies");
            Assertions.assertThat(genreAfterUpdate).isEqualToComparingFieldByField(genre);
        });
    }

    @Test
    public void updateGenreTest() {
        Genre genre = testObjectFactory.createGenre("Thriller");

        GenrePutDTO putDTO = new GenrePutDTO();
        putDTO.setGenreName("new genre");
        putDTO.setDescription("new text description");

        GenreReadDTO readDTO = genreService.updateGenre(genre.getId(), putDTO);

        Assertions.assertThat(putDTO).isEqualToComparingFieldByField(readDTO);

        Genre genreAfterUpdate = genreRepository.findById(readDTO.getId()).get();;
        Assertions.assertThat(genreAfterUpdate).isEqualToIgnoringGivenFields(readDTO, "movies");
    }

    @Test
    public void deleteGenreTest() {
        Genre genre = testObjectFactory.createGenre("Thriller");

        genreService.deleteGenre(genre.getId());

        Assert.assertFalse(genreRepository.existsById(genre.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteGenreNotFoundTest() {
        genreService.deleteGenre(UUID.randomUUID());
    }

    private void inTransaction (Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}
