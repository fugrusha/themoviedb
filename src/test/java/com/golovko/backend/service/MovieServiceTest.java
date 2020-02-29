package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCrewType;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.movie.*;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from movie_crew", "delete from person", "delete from movie"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
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
        createDTO.setIsReleased(true);
        createDTO.setReleaseDate(LocalDate.parse("1900-01-01"));

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

        MovieReadDTO readDTO = movieService.patchMovie(movie.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        movie = movieRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(movie).isEqualToIgnoringGivenFields(readDTO,
                "movieCrews", "movieCast", "genres");
    }

    @Test
    public void patchMovieEmptyPatchTest() {
        Movie movie = testObjectFactory.createMovie();

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        MovieReadDTO readDTO = movieService.patchMovie(movie.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        Movie movieAfterUpdate = movieRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(movieAfterUpdate).hasNoNullFieldsOrProperties();
        Assertions.assertThat(movie).isEqualToIgnoringGivenFields(movieAfterUpdate,
                "movieCrews", "movieCast", "genres");
    }

    @Test
    public void updateMovieTest() {
        Movie movie = testObjectFactory.createMovie();

        MoviePutDTO updateDTO = new MoviePutDTO();
        updateDTO.setMovieTitle("new title");
        updateDTO.setDescription("some NEW description");
        updateDTO.setIsReleased(false);
        updateDTO.setReleaseDate(LocalDate.parse("1900-07-10"));
        updateDTO.setAverageRating(5.5);

        MovieReadDTO readDTO = movieService.updateMovie(movie.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        movie = movieRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(movie).isEqualToIgnoringGivenFields(readDTO,
                "movieCrews", "movieCast", "genres");
    }

    @Test
    public void deleteMovieTest() {
        Movie movie = testObjectFactory.createMovie();
        movieService.deleteMovie(movie.getId());

        Assert.assertFalse(movieRepository.existsById(movie.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteMovieNotFoundTest() {
        movieService.deleteMovie(UUID.randomUUID());
    }

    @Test
    public void getMoviesWithEmptyFilterTest() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m2 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4));
        createMovie(LocalDate.of(1944, 5, 4));

        testObjectFactory.createMovieCrew(person2, m1);
        testObjectFactory.createMovieCrew(person2, m2);
        testObjectFactory.createMovieCrew(person1, m3);

        MovieFilter filter = new MovieFilter();
        Assertions.assertThat(movieService.getMovies(filter)).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId(), m3.getId());
    }

    @Test
    public void getMoviesByPersonTest() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m2 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4));
        createMovie(LocalDate.of(1944, 5, 4));

        testObjectFactory.createMovieCrew(person2, m1);
        testObjectFactory.createMovieCrew(person2, m2);
        testObjectFactory.createMovieCrew(person1, m3);

        MovieFilter filter = new MovieFilter();
        filter.setPersonId(person2.getId());
        Assertions.assertThat(movieService.getMovies(filter)).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void getMoviesByPartTypesTest() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m2 = createMovie(LocalDate.of(1990, 5, 4));
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4));
        Movie m4 = createMovie(LocalDate.of(1944, 5, 4));

        testObjectFactory.createMovieCrewForFilter(person2, m1, MovieCrewType.COMPOSER);
        testObjectFactory.createMovieCrewForFilter(person2, m2, MovieCrewType.WRITER);
        testObjectFactory.createMovieCrewForFilter(person1, m3, MovieCrewType.PRODUCER);
        testObjectFactory.createMovieCrewForFilter(person2, m4, MovieCrewType.COSTUME_DESIGNER);

        MovieFilter filter = new MovieFilter();
        filter.setMovieCrewTypes(Set.of(MovieCrewType.COMPOSER, MovieCrewType.WRITER));
        List<MovieReadDTO> filteredMovies = movieService.getMovies(filter);
        Assertions.assertThat(filteredMovies).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void getMoviesByReleasedIntervalTest() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m2 = createMovie(LocalDate.of(1990, 5, 4));
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4));
        createMovie(LocalDate.of(1944, 5, 4));

        testObjectFactory.createMovieCrew(person2, m1);
        testObjectFactory.createMovieCrew(person2, m2);
        testObjectFactory.createMovieCrew(person1, m3);

        MovieFilter filter = new MovieFilter();
        filter.setReleasedFrom(LocalDate.of(1980, 5, 4));
        filter.setReleasedTo(LocalDate.of(1992, 5, 4));
        Assertions.assertThat(movieService.getMovies(filter)).extracting("id")
                .containsExactlyInAnyOrder(m2.getId(), m3.getId());
    }

    @Test
    public void getMoviesByAllFiltersTest() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4)); // no
        Movie m2 = createMovie(LocalDate.of(1990, 5, 4)); // yes
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4)); // no
        Movie m4 = createMovie(LocalDate.of(1987, 5, 4));

        testObjectFactory.createMovieCrewForFilter(person2, m1, MovieCrewType.COMPOSER);
        testObjectFactory.createMovieCrewForFilter(person2, m2, MovieCrewType.WRITER);
        testObjectFactory.createMovieCrewForFilter(person1, m3, MovieCrewType.PRODUCER);
        testObjectFactory.createMovieCrewForFilter(person2, m4, MovieCrewType.COSTUME_DESIGNER);

        MovieFilter filter = new MovieFilter();
        filter.setPersonId(person2.getId());
        filter.setMovieCrewTypes(Set.of(MovieCrewType.COMPOSER, MovieCrewType.WRITER));
        filter.setReleasedFrom(LocalDate.of(1980, 5, 4));
        filter.setReleasedTo(LocalDate.of(1992, 5, 4));
        List<MovieReadDTO> filteredMovies = movieService.getMovies(filter);
        Assertions.assertThat(movieService.getMovies(filter)).extracting("id")
                .containsExactlyInAnyOrder(m2.getId());
    }

    private Movie createMovie(LocalDate releasedDate) {
        Movie movie = new Movie();
        movie.setMovieTitle("Title of the Movie");
        movie.setDescription("movie description");
        movie.setIsReleased(true);
        movie.setReleaseDate(releasedDate);
        movie.setAverageRating(5.0);
        movie = movieRepository.save(movie);
        return movie;
    }
}
