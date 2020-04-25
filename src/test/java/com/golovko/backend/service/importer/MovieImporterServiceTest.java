package com.golovko.backend.service.importer;

import com.golovko.backend.BaseTest;
import com.golovko.backend.client.themoviedb.TheMovieDbClient;
import com.golovko.backend.client.themoviedb.dto.GenreShortDTO;
import com.golovko.backend.client.themoviedb.dto.MovieReadDTO;
import com.golovko.backend.domain.Genre;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.exception.ImportAlreadyPerformedException;
import com.golovko.backend.exception.ImportedEntityAlreadyExistsException;
import com.golovko.backend.repository.MovieRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

public class MovieImporterServiceTest extends BaseTest {

    @MockBean
    private TheMovieDbClient theMovieDbClient;

    @Autowired
    private MovieRepository movieRepository;

    @SpyBean
    private CreditsImporterService creditsImporterService;

    @Autowired
    private MovieImporterService movieImporterService;

    @Test
    public void testMovieImport()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";
        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(readDTO);
        Mockito.doNothing().when(creditsImporterService).importMovieCredits(any(), any());

        UUID movieId = movieImporterService.importMovie(externalMovieId);
        Movie movie = movieRepository.findById(movieId).get();

        Assert.assertEquals(readDTO.getTitle(), movie.getMovieTitle());
    }

    @Test
    public void testMovieImportAlreadyExists() {
        String externalMovieId = "id200";

        Movie existingMovie = testObjectFactory.createMovie();

        MovieReadDTO readDTO = createMovieReadDTO();
        readDTO.setTitle(existingMovie.getMovieTitle());
        readDTO.setReleaseDate(existingMovie.getReleaseDate());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(readDTO);

        ImportedEntityAlreadyExistsException ex = Assertions.catchThrowableOfType(
                () -> movieImporterService.importMovie(externalMovieId),
                ImportedEntityAlreadyExistsException.class);

        Assertions.assertThat(ex.getMessage()).contains(existingMovie.getMovieTitle());
    }

    @Test
    public void testNoCallToClientOnDuplicateImport()
            throws ImportAlreadyPerformedException, ImportedEntityAlreadyExistsException {
        String externalMovieId = "id200";
        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(readDTO);
        Mockito.doNothing().when(creditsImporterService).importMovieCredits(any(), any());

        movieImporterService.importMovie(externalMovieId);
        Mockito.verify(theMovieDbClient).getMovie(externalMovieId, null);
        Mockito.reset(theMovieDbClient);

        Assertions.assertThatThrownBy(() -> movieImporterService.importMovie(externalMovieId))
                .isInstanceOf(ImportAlreadyPerformedException.class);

        Mockito.verifyNoInteractions(theMovieDbClient);
    }

    @Test
    public void testMovieImportWithNotExistedGenre()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        GenreShortDTO genreDTO = createGenreShortDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(List.of(genreDTO));

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.doNothing().when(creditsImporterService).importMovieCredits(any(), any());

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertNotNull(movie.getGenres());
        });
    }

    @Test
    public void testMovieImportWithExistedGenre()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        Genre genre = testObjectFactory.createGenre("Thriller");
        GenreShortDTO genreDTO = createGenreShortDTO();
        genreDTO.setName(genre.getGenreName());

        MovieReadDTO movieDTO = createMovieWithGenresDTO(List.of(genreDTO));

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.doNothing().when(creditsImporterService).importMovieCredits(any(), any());

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertEquals(1, movieDTO.getGenres().size());
            Assertions.assertThat(movie.getGenres()).extracting("genreName")
                    .contains(genreDTO.getName());
        });
    }

    @Test
    public void testMovieImportWithEmptyGenres()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.doNothing().when(creditsImporterService).importMovieCredits(any(), any());

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());
        });
    }

    private MovieReadDTO createMovieReadDTO() {
        MovieReadDTO dto = generateObject(MovieReadDTO.class);
        dto.setReleaseDate(LocalDate.of(2000,10,10));
        dto.setStatus("Released");
        return dto;
    }

    private MovieReadDTO createMovieWithGenresDTO(List<GenreShortDTO> genres) {
        MovieReadDTO dto = generateObject(MovieReadDTO.class);
        dto.setReleaseDate(LocalDate.of(2000,10,10));
        dto.setStatus("Released");
        dto.setGenres(genres);
        return dto;
    }

    private GenreShortDTO createGenreShortDTO() {
        return generateObject(GenreShortDTO.class);
    }
}
