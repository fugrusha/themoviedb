package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Genre;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.genre.*;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.GenreRepository;
import com.golovko.backend.repository.MovieRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionSystemException;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class GenreServiceTest extends BaseTest {

    @Autowired
    private GenreService genreService;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    public void testGetGenreById() {
        Genre genre = testObjectFactory.createGenre("Comedy");

        GenreReadDTO readDTO = genreService.getGenre(genre.getId());

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(genre);
    }

    // TODO to be or not to be // Will filter be better?
    @Test
    public void testGetExtendedGenre() {
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
    public void testGetAllGenres() {
        Genre g1 = testObjectFactory.createGenre("Thriller");
        Genre g2 = testObjectFactory.createGenre("Horror");
        Genre g3 = testObjectFactory.createGenre("Comedy");

        PageResult<GenreReadDTO> genres = genreService.getGenres(Pageable.unpaged());

        Assertions.assertThat(genres.getData()).extracting(GenreReadDTO::getId)
                .containsExactlyInAnyOrder(g3.getId(), g2.getId(), g1.getId());
    }

    @Test
    public void testGetGenresWithPagingAndSorting() {
        testObjectFactory.createGenre("Thriller");
        Genre g2 = testObjectFactory.createGenre("Horror");
        Genre g3 = testObjectFactory.createGenre("Comedy");

        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "genreName"));

        Assertions.assertThat(genreService.getGenres(pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(g3.getId(), g2.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetGenreByWrongId() {
        genreService.getGenre(UUID.randomUUID());
    }

    @Test
    public void testCreateGenre() {
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
    public void testPatchGenre() {
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
    public void testPatchGenreEmptyPatch() {
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
    public void testUpdateGenre() {
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
    public void testDeleteGenre() {
        Genre genre = testObjectFactory.createGenre("Thriller");

        genreService.deleteGenre(genre.getId());

        Assert.assertFalse(genreRepository.existsById(genre.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteGenreNotFound() {
        genreService.deleteGenre(UUID.randomUUID());
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveGenreNotNullValidation() {
        Genre genre = new Genre();
        genreRepository.save(genre);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveGenreMinSizeValidation() {
        Genre genre = new Genre();
        genre.setGenreName("");
        genre.setDescription("");
        genreRepository.save(genre);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveGenreMaxSizeValidation() {
        Genre genre = new Genre();
        genre.setGenreName("very long genre name".repeat(100));
        genre.setDescription("very long genre name".repeat(1000));
        genreRepository.save(genre);
    }
}
