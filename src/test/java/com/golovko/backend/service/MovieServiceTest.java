package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.MovieCreateDTO;
import com.golovko.backend.dto.MoviePatchDTO;
import com.golovko.backend.dto.MovieReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieRepository;
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

    private Movie createMovie() {
        Movie movie = new Movie();
        movie.setMovieTitle("Title of the Movie");
        movie.setDescription("movie description");
        movie.setReleased(false);
        movie.setReleaseDate(LocalDate.parse("1990-05-14"));
        movie.setAverageRating(5.0);
        movie = movieRepository.save(movie);
        return movie;
    }

    @Test
    public void getMovieTest() {
        Movie movie = createMovie();

        MovieReadDTO readDTO = movieService.getMovie(movie.getId());
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(movie);
    }

    @Test(expected = EntityNotFoundException.class)
    public void getMovieWrongId() {
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
        Movie movie = createMovie();

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setMovieTitle("another title");
        patchDTO.setDescription("another description");
        patchDTO.setReleased(true);
        patchDTO.setReleaseDate(LocalDate.parse("2002-02-03"));
        patchDTO.setAverageRating(10.0);

        MovieReadDTO readDTO = movieService.patchMovie(movie.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        movie = movieRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(movie).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void patchMovieEmptyPatchTest() {
        Movie movie = createMovie();

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        MovieReadDTO readDTO = movieService.patchMovie(movie.getId(), patchDTO);

        Assert.assertNotNull(readDTO.getMovieTitle());
        Assert.assertNotNull(readDTO.getDescription());
        Assert.assertNotNull(readDTO.getReleaseDate());
        Assertions.assertThat(readDTO.isReleased()).isEqualTo(movie.isReleased());
        Assert.assertNotNull(readDTO.getAverageRating());

        Movie movieAfterUpdate = movieRepository.findById(readDTO.getId()).get();

        Assert.assertNotNull(movieAfterUpdate.getMovieTitle());
        Assert.assertNotNull(movieAfterUpdate.getDescription());
        Assert.assertNotNull(movieAfterUpdate.getReleaseDate());
        Assertions.assertThat(readDTO.isReleased()).isEqualTo(movie.isReleased());
        Assert.assertNotNull(movieAfterUpdate.getAverageRating());

        Assertions.assertThat(movie).isEqualToComparingFieldByField(movieAfterUpdate);
    }

    @Test
    public void deleteMovieTest() {
        Movie movie = createMovie();
        movieService.deleteMovie(movie.getId());

        Assert.assertFalse(movieRepository.existsById(movie.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteMovieNotFound() {
        movieService.deleteMovie(UUID.randomUUID());
    }
}
