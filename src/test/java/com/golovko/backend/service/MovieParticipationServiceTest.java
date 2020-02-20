package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieParticipation;
import com.golovko.backend.domain.PartType;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.movieparticipation.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieParticipationRepository;
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

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from person", "delete from movie", "delete from movie_participation"},
    executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieParticipationServiceTest {

    @Autowired
    private MovieParticipationService movieParticipationService;

    @Autowired
    private MovieParticipationRepository movieParticipationRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Test
    public void getMovieParticipationTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation movieParticipation = testObjectFactory.createMovieParticipation(person, movie);

        MoviePartReadDTO readDTO = movieParticipationService
                .getMovieParticipation(movie.getId(), movieParticipation.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieParticipation,
                "movieId", "personId");
        Assertions.assertThat(readDTO.getMovieId()).isEqualToComparingFieldByField(movie.getId());
        Assertions.assertThat(readDTO.getPersonId()).isEqualToComparingFieldByField(person.getId());
    }

    @Test
    public void getMovieParticipationExtendedTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation movieParticipation = testObjectFactory.createMovieParticipation(person, movie);

        MoviePartReadExtendedDTO readDTO = movieParticipationService
                .getExtendedMovieParticipation(movie.getId(), movieParticipation.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieParticipation,
                "movie", "person");
        Assertions.assertThat(readDTO.getMovie()).isEqualToComparingFieldByField(movie);
        Assertions.assertThat(readDTO.getPerson()).isEqualToComparingFieldByField(person);
    }

    @Test
    public void getListOfMovieParticipationTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation moviePart = testObjectFactory.createMovieParticipation(person, movie);

        List<MoviePartReadDTO> resultList = movieParticipationService.getListOfMovieParticipation(movie.getId());

        Assertions.assertThat(resultList).extracting(MoviePartReadDTO::getId)
                .containsExactlyInAnyOrder(moviePart.getId());
    }

    @Test
    public void createMovieParticipationTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();

        MoviePartCreateDTO createDTO = new MoviePartCreateDTO();
        createDTO.setPersonId(person.getId());
        createDTO.setPartInfo("some text");
        createDTO.setPartType(PartType.COSTUME_DESIGNER);

        MoviePartReadDTO readDTO =
                movieParticipationService.createMovieParticipation(createDTO, movie.getId());

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        MovieParticipation movieParticipation = movieParticipationRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieParticipation,
                "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movieParticipation.getMovie().getId());
        Assert.assertEquals(readDTO.getPersonId(), movieParticipation.getPerson().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void createMovieParticipationWrongPersonTest() {
        Movie movie = testObjectFactory.createMovie();

        MoviePartCreateDTO createDTO = new MoviePartCreateDTO();
        createDTO.setPersonId(UUID.randomUUID());
        createDTO.setPartInfo("some text");
        createDTO.setPartType(PartType.COSTUME_DESIGNER);

        movieParticipationService.createMovieParticipation(createDTO, movie.getId());
    }

    @Test
    public void patchMovieParticipationTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation moviePart = testObjectFactory.createMovieParticipation(person, movie);

        MoviePartPatchDTO patchDTO = new MoviePartPatchDTO();
        patchDTO.setPartType(PartType.COMPOSER);
        patchDTO.setPartInfo("New text");
        patchDTO.setPersonId(person.getId());;

        MoviePartReadDTO readDTO = movieParticipationService
                .patchMovieParticipation(movie.getId(), moviePart.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        moviePart = movieParticipationRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(moviePart,
                "movieId", "personId");
        Assert.assertEquals(moviePart.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(moviePart.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void patchMovieCastEmptyPatchTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation moviePart = testObjectFactory.createMovieParticipation(person, movie);

        MoviePartPatchDTO patchDTO = new MoviePartPatchDTO();

        MoviePartReadDTO readDTO = movieParticipationService
                .patchMovieParticipation(movie.getId(), moviePart.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        moviePart = movieParticipationRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(moviePart,
                "movieId", "personId");
        Assert.assertEquals(moviePart.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(moviePart.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void updateMovieParticipationTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation moviePart = testObjectFactory.createMovieParticipation(person, movie);

        MoviePartPutDTO updateDTO = new MoviePartPutDTO();
        updateDTO.setPartInfo("New text");
        updateDTO.setPersonId(person.getId());
        updateDTO.setPartType(PartType.PRODUCER);

        MoviePartReadDTO readDTO = movieParticipationService
                .updateMovieParticipation(movie.getId(), moviePart.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        moviePart = movieParticipationRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(moviePart,
                "movieId", "personId");
        Assert.assertEquals(moviePart.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(moviePart.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void deleteMovieParticipationTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation movieParticipation = testObjectFactory.createMovieParticipation(person, movie);

        movieParticipationService.deleteMovieParticipation(movie.getId(), movieParticipation.getId());

        Assert.assertFalse(movieParticipationRepository.existsById(movieParticipation.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteMovieParticipationNotFound() {
        movieParticipationService.deleteMovieParticipation(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test(expected = EntityNotFoundException.class)
    public void getMovieParticipationWrongIdTest() {
        movieParticipationService.getMovieParticipation(UUID.randomUUID(), UUID.randomUUID());
    }
}
