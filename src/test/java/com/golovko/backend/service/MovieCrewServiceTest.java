package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCrew;
import com.golovko.backend.domain.MovieCrewType;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.moviecrew.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieCrewRepository;
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
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from person", "delete from movie", "delete from movie_crew"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieCrewServiceTest {

    @Autowired
    private MovieCrewService movieCrewService;

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Test
    public void getMovieCrewTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        MovieCrewReadDTO readDTO = movieCrewService.getMovieCrew(movie.getId(), movieCrew.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movie.getId());
        Assert.assertEquals(readDTO.getPersonId(), person.getId());
    }

    @Test
    public void getMovieCrewExtendedTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        MovieCrewReadExtendedDTO readDTO = movieCrewService.getExtendedMovieCrew(movie.getId(), movieCrew.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movie", "person");
        Assertions.assertThat(readDTO.getMovie()).isEqualToComparingFieldByField(movie);
        Assertions.assertThat(readDTO.getPerson()).isEqualToComparingFieldByField(person);
    }

    @Test
    public void getListOfMovieCrewTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew moviePart = testObjectFactory.createMovieCrew(person, movie);

        List<MovieCrewReadDTO> resultList = movieCrewService.getAllMovieCrews(movie.getId());

        Assertions.assertThat(resultList).extracting(MovieCrewReadDTO::getId)
                .containsExactlyInAnyOrder(moviePart.getId());
    }

    @Test
    public void createMovieCrewTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();

        MovieCrewCreateDTO createDTO = new MovieCrewCreateDTO();
        createDTO.setPersonId(person.getId());
        createDTO.setDescription("some text");
        createDTO.setMovieCrewType(MovieCrewType.COSTUME_DESIGNER);

        MovieCrewReadDTO readDTO = movieCrewService.createMovieCrew(createDTO, movie.getId());

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        MovieCrew movieCrew = movieCrewRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movieCrew.getMovie().getId());
        Assert.assertEquals(readDTO.getPersonId(), movieCrew.getPerson().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void createMovieCrewnWrongPersonTest() {
        Movie movie = testObjectFactory.createMovie();

        MovieCrewCreateDTO createDTO = new MovieCrewCreateDTO();
        createDTO.setPersonId(UUID.randomUUID());
        createDTO.setDescription("some text");
        createDTO.setMovieCrewType(MovieCrewType.COSTUME_DESIGNER);

        movieCrewService.createMovieCrew(createDTO, movie.getId());
    }

    @Test
    public void patchMovieCrewTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        MovieCrewPatchDTO patchDTO = new MovieCrewPatchDTO();
        patchDTO.setMovieCrewType(MovieCrewType.COMPOSER);
        patchDTO.setDescription("New text");
        patchDTO.setPersonId(person.getId());;

        MovieCrewReadDTO readDTO = movieCrewService.patchMovieCrew(movie.getId(), movieCrew.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        movieCrew = movieCrewRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movieId", "personId");
        Assert.assertEquals(movieCrew.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(movieCrew.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void patchMovieCrewEmptyPatchTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        MovieCrewPatchDTO patchDTO = new MovieCrewPatchDTO();

        MovieCrewReadDTO readDTO = movieCrewService.patchMovieCrew(movie.getId(), movieCrew.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        inTransaction(() -> {
            MovieCrew moviePartAfterUpdate = movieCrewRepository.findById(readDTO.getId()).get();
            Assertions.assertThat(moviePartAfterUpdate).isEqualToIgnoringGivenFields(movieCrew,
                    "person", "movie");
            Assert.assertEquals(movieCrew.getMovie().getId(), moviePartAfterUpdate.getMovie().getId());
            Assert.assertEquals(movieCrew.getPerson().getId(), moviePartAfterUpdate.getPerson().getId());
        });
    }

    @Test
    public void updateMovieCrewTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        MovieCrewPutDTO updateDTO = new MovieCrewPutDTO();
        updateDTO.setDescription("New text");
        updateDTO.setPersonId(person.getId());
        updateDTO.setMovieCrewType(MovieCrewType.PRODUCER);

        MovieCrewReadDTO readDTO = movieCrewService.updateMovieCrew(movie.getId(), movieCrew.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        movieCrew = movieCrewRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movieId", "personId");
        Assert.assertEquals(movieCrew.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(movieCrew.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void deleteMovieCrewTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        movieCrewService.deleteMovieCrew(movie.getId(), movieCrew.getId());

        Assert.assertFalse(movieCrewRepository.existsById(movieCrew.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteMovieCrewNotFound() {
        movieCrewService.deleteMovieCrew(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getMovieCrewWrongIdTest() {
        movieCrewService.getMovieCrew(UUID.randomUUID(), UUID.randomUUID());
    }

    private void inTransaction (Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}
