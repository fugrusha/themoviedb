package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieParticipation;
import com.golovko.backend.domain.PartType;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.movieparticipation.MoviePartCreateDTO;
import com.golovko.backend.dto.movieparticipation.MoviePartPatchDTO;
import com.golovko.backend.dto.movieparticipation.MoviePartReadDTO;
import com.golovko.backend.dto.movieparticipation.MoviePartReadExtendedDTO;
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
import org.springframework.transaction.support.TransactionTemplate;

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

    @Autowired
    private TransactionTemplate transactionTemplate;

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

        inTransaction(() -> {
            MoviePartReadExtendedDTO readDTO = movieParticipationService
                    .getExtendedMovieParticipation(movie.getId(), movieParticipation.getId());

            Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieParticipation,
                    "movie", "person");
            Assertions.assertThat(readDTO.getMovie()).isEqualToComparingFieldByField(movie);
            Assertions.assertThat(readDTO.getPerson()).isEqualToComparingFieldByField(person);
        });
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
        MoviePartCreateDTO createDTO = new MoviePartCreateDTO();
        createDTO.setPartInfo("some text");
        createDTO.setPartType(PartType.COSTUME_DESIGNER);

        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();

        MoviePartReadDTO readDTO =
                movieParticipationService.createMovieParticipation(createDTO, movie.getId(), person.getId());

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        MovieParticipation movieParticipation = movieParticipationRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieParticipation, "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movieParticipation.getMovie().getId());
        Assert.assertEquals(readDTO.getPersonId(), movieParticipation.getPerson().getId());
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
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(moviePart, "movieId", "personId");
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
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(moviePart, "movieId", "personId");
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
    public void deleteMovieNotFound() {
        movieParticipationService.deleteMovieParticipation(UUID.randomUUID(), UUID.randomUUID());
    }

    private void inTransaction(Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}
