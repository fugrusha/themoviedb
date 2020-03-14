package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
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
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.MOVIE_CAST;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("test")
@Sql(statements = {
        "delete from rating",
        "delete from user_role",
        "delete from application_user",
        "delete from person",
        "delete from movie",
        "delete from movie_cast"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieCastServiceTest {

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private MovieCastService movieCastService;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Test
    public void testGetMovieCast() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        MovieCastReadDTO readDTO = movieCastService.getMovieCast(movieCast.getId(), movie.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast,
                "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movie.getId());
        Assert.assertEquals(readDTO.getPersonId(), person.getId());
    }

    @Test
    public void testGetMovieCastExtended() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        MovieCastReadExtendedDTO extendedDTO =
                movieCastService.getMovieCastExtended(movieCast.getId(), movie.getId());

        Assertions.assertThat(extendedDTO).isEqualToIgnoringGivenFields(movieCast,
                "movie", "person");
        Assertions.assertThat(extendedDTO.getMovie()).isEqualToComparingFieldByField(movie);
        Assertions.assertThat(extendedDTO.getPerson()).isEqualToComparingFieldByField(person);
    }

    @Test
    public void testGetAllMovieCast() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        List<MovieCastReadDTO> result = movieCastService.getAllMovieCasts(movie.getId());

        Assertions.assertThat(result).extracting(MovieCastReadDTO::getId)
                .containsExactlyInAnyOrder(movieCast.getId());
    }

    @Test
    public void testDeleteMovieCast() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        movieCastService.deleteMovieCast(movieCast.getId(), movie.getId());

        Assert.assertFalse(movieCastRepository.existsById(movieCast.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteMovieCastNotFound() {
        movieCastService.deleteMovieCast(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testCreateMovieCast() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();

        MovieCastCreateDTO createDTO = new MovieCastCreateDTO();
        createDTO.setPersonId(person.getId());
        createDTO.setDescription("Some text");
        createDTO.setCharacter("vally");

        MovieCastReadDTO readDTO = movieCastService.createMovieCast(createDTO, movie.getId());

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        MovieCast movieCast = movieCastRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast,
                "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movie.getId());
        Assert.assertEquals(readDTO.getPersonId(), person.getId());
    }

    @Test
    public void testCreateMovieCastWithoutPerson() {
        Movie movie = testObjectFactory.createMovie();

        MovieCastCreateDTO createDTO = new MovieCastCreateDTO();
        createDTO.setPersonId(null);
        createDTO.setDescription("Some text");
        createDTO.setCharacter("vally");

        MovieCastReadDTO readDTO = movieCastService.createMovieCast(createDTO, movie.getId());

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());
        Assert.assertNull(readDTO.getPersonId());

        MovieCast movieCast = movieCastRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast,
                "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movie.getId());
        Assert.assertNull(movieCast.getPerson());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateMovieCastWrongPersonId() {
        Movie movie = testObjectFactory.createMovie();

        MovieCastCreateDTO createDTO = new MovieCastCreateDTO();
        createDTO.setPersonId(UUID.randomUUID());
        createDTO.setDescription("Some text");
        createDTO.setCharacter("vally");

        movieCastService.createMovieCast(createDTO, movie.getId());
    }

    @Test
    public void testUpdateMovieCast() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        MovieCastPutDTO updateDTO = new MovieCastPutDTO();
        updateDTO.setCharacter("New Character");
        updateDTO.setDescription("New text");
        updateDTO.setPersonId(person.getId());

        MovieCastReadDTO readDTO = movieCastService.updateMovieCast(updateDTO, movieCast.getId(), movie.getId());

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        movieCast = movieCastRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast,
                "movieId", "personId");
        Assert.assertEquals(movieCast.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(movieCast.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void testPatchMovieCast() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        MovieCastPatchDTO patchDTO = new MovieCastPatchDTO();
        patchDTO.setCharacter("New Character");
        patchDTO.setDescription("New text");
        patchDTO.setPersonId(person.getId());;

        MovieCastReadDTO readDTO = movieCastService.patchMovieCast(patchDTO, movieCast.getId(), movie.getId());

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        movieCast = movieCastRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast,
                "movieId", "personId");
        Assert.assertEquals(movieCast.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(movieCast.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void testPatchMovieCastEmptyPatch() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        MovieCastPatchDTO patchDTO = new MovieCastPatchDTO();

        MovieCastReadDTO readDTO = movieCastService.patchMovieCast(patchDTO, movieCast.getId(), movie.getId());

        Assertions.assertThat(readDTO).hasNoNullFieldsOrPropertiesExcept("averageRating");

        inTransaction(() -> {
            MovieCast movieCastAfterUpdate = movieCastRepository.findById(readDTO.getId()).get();
            Assertions.assertThat(movieCastAfterUpdate).isEqualToIgnoringGivenFields(movieCast,
                    "person", "movie");
            Assert.assertEquals(movieCast.getMovie().getId(), movieCastAfterUpdate.getMovie().getId());
            Assert.assertEquals(movieCast.getPerson().getId(), movieCastAfterUpdate.getPerson().getId());
        });
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetMovieCastWrongId() {
        movieCastService.getMovieCast(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testCalcAverageRatingOfMovieCast() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();

        MovieCast movieCast = testObjectFactory.createMovieCast(person, movie);

        testObjectFactory.createRating(3, u1, movieCast.getId(), MOVIE_CAST);
        testObjectFactory.createRating(6, u2, movieCast.getId(), MOVIE_CAST);

        movieCastService.updateAverageRatingOfMovieCast(movieCast.getId());

        movieCast = movieCastRepository.findById(movieCast.getId()).get();
        Assert.assertEquals(4.5, movieCast.getAverageRating(), Double.MIN_NORMAL);
    }

    private void inTransaction (Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}
