package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.movie.MovieCreateDTO;
import com.golovko.backend.dto.movie.MoviePatchDTO;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
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

import java.time.LocalDate;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = "delete from movie", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieServiceTest {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Test
    public void getMovieTest() {
        Movie movie = testObjectFactory.createMovie();

        MovieReadDTO readDTO = movieService.getMovie(movie.getId());
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(movie);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getMovieWrongIdTest() {
        movieService.getMovie(UUID.randomUUID());
    }

    @Test
    public void createMovieTest() {
        MovieCreateDTO createDTO = new MovieCreateDTO();
        createDTO.setMovieTitle("title");
        createDTO.setDescription("description");
        createDTO.setReleased(true);
        createDTO.setReleaseDate(LocalDate.parse("1900-01-01"));
        createDTO.setAverageRating(0.0);

        MovieReadDTO readDTO = movieService.createMovie(createDTO);

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Movie movie = movieRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(movie);
    }

    @Test
    public void patchMovieTest() {
        Movie movie = testObjectFactory.createMovie();

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setMovieTitle("another title");
        patchDTO.setDescription("another description");
        patchDTO.setIsReleased(true);
        patchDTO.setReleaseDate(LocalDate.parse("2002-02-03"));
        patchDTO.setAverageRating(10.0);

        MovieReadDTO readDTO = movieService.patchMovie(movie.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        movie = movieRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(movie).isEqualToIgnoringGivenFields(readDTO, "movieParticipations");
    }

    @Test
    public void patchMovieEmptyPatchTest() {
        Movie movie = testObjectFactory.createMovie();

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        MovieReadDTO readDTO = movieService.patchMovie(movie.getId(), patchDTO);

        Assert.assertNotNull(readDTO.getMovieTitle());
        Assert.assertNotNull(readDTO.getDescription());
        Assert.assertNotNull(readDTO.getReleaseDate());
        Assert.assertNotNull(readDTO.isReleased());
        Assert.assertNotNull(readDTO.getAverageRating());

        Movie movieAfterUpdate = movieRepository.findById(readDTO.getId()).get();

        Assert.assertNotNull(movieAfterUpdate.getMovieTitle());
        Assert.assertNotNull(movieAfterUpdate.getDescription());
        Assert.assertNotNull(movieAfterUpdate.getReleaseDate());
        Assert.assertNotNull(movieAfterUpdate.getIsReleased());
        Assert.assertNotNull(movieAfterUpdate.getAverageRating());

        Assertions.assertThat(movie).isEqualToIgnoringGivenFields(movieAfterUpdate, "movieParticipations");
    }

    @Test
    public void deleteMovieTest() {
        Movie movie = testObjectFactory.createMovie();
        movieService.deleteMovie(movie.getId());

        Assert.assertFalse(movieRepository.existsById(movie.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteMovieNotFound() {
        movieService.deleteMovie(UUID.randomUUID());
    }
}
