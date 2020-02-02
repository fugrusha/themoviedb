package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieCastRepository;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = {"delete from person", "delete from movie", "delete from movie_cast"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieCastServiceTest {

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private MovieCastService movieCastService;

    @Test
    public void getMovieCastTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        MovieCastReadDTO readDTO = movieCastService.getMovieCast(movieCast.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast,
                "movieId", "personId");
        Assertions.assertThat(readDTO.getMovieId()).isEqualToComparingFieldByField(movie.getId());
        Assertions.assertThat(readDTO.getPersonId()).isEqualToComparingFieldByField(person.getId());
    }

    @Transactional
    @Test
    public void getMovieCastExtendedTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast =  testObjectFactory.createMovieCast(person, movie);

        MovieCastReadExtendedDTO extendedDTO = movieCastService.getMovieCastExtended(movieCast.getId());

        Assertions.assertThat(extendedDTO).isEqualToIgnoringGivenFields(movieCast,
                "movie", "person");
        Assertions.assertThat(extendedDTO.getMovie()).isEqualToComparingFieldByField(movie);
        Assertions.assertThat(extendedDTO.getPerson()).isEqualToComparingFieldByField(person);
    }

    @Test
    public void deleteMovieCastTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast =  testObjectFactory.createMovieCast(person, movie);

        movieCastService.deleteMovieCast(movieCast.getId());

        Assert.assertFalse(movieCastRepository.existsById(movieCast.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteMovieCastNotFound() {
        movieCastService.deleteMovieCast(UUID.randomUUID());
    }

    @Test
    public void createMovieCastTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();

        MovieCastCreateDTO createDTO = new MovieCastCreateDTO();
        createDTO.setPartInfo("Some text");
        createDTO.setCharacter("vally");

        MovieCastReadDTO readDTO = movieCastService.createMovieCast(createDTO, movie.getId(), person.getId());

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        MovieCast movieCast = movieCastRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast, "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movie.getId());
        Assert.assertEquals(readDTO.getPersonId(), person.getId());
    }

    @Test
    public void updateMovieCastTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        MovieCastPutDTO updateDTO = new MovieCastPutDTO();
        updateDTO.setCharacter("New Character");
        updateDTO.setPartInfo("New text");
        updateDTO.setMovieId(movie.getId());
        updateDTO.setPersonId(person.getId());

        MovieCastReadDTO readDTO = movieCastService.updateMovieCast(updateDTO, movieCast.getId());

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        movieCast = movieCastRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast, "movieId", "personId");
        Assert.assertEquals(movieCast.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(movieCast.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void patchMovieCastTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        MovieCastPatchDTO patchDTO = new MovieCastPatchDTO();
        patchDTO.setCharacter("New Character");
        patchDTO.setPartInfo("New text");
        patchDTO.setMovieId(movie.getId());
        patchDTO.setPersonId(person.getId());;

        MovieCastReadDTO readDTO = movieCastService.patchMovieCast(patchDTO, movieCast.getId());

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        movieCast = movieCastRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast, "movieId", "personId");
        Assert.assertEquals(movieCast.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(movieCast.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void patchMovieCastEmptyPatchTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        MovieCastPatchDTO patchDTO = new MovieCastPatchDTO();

        MovieCastReadDTO readDTO = movieCastService.patchMovieCast(patchDTO, movieCast.getId());

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        movieCast = movieCastRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast, "movieId", "personId");
        Assert.assertEquals(movieCast.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(movieCast.getPerson().getId(), readDTO.getPersonId());
    }
}
