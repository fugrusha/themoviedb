package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieParticipation;
import com.golovko.backend.domain.PartType;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.movieparticipation.MoviePartCreateDTO;
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
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
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

    @Transactional
    @Test
    public void getMovieParticipationTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation movieParticipation = createMovieParticipation(person, movie);

        MoviePartReadDTO readDTO = movieParticipationService.getMovieParticipation(movieParticipation.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieParticipation,
                "movieId", "personId");
        Assertions.assertThat(readDTO.getMovieId()).isEqualToComparingFieldByField(movie.getId());
        Assertions.assertThat(readDTO.getPersonId()).isEqualToComparingFieldByField(person.getId());
    }

    @Transactional
    @Test
    public void getMovieParticipationExtendedTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation movieParticipation = createMovieParticipation(person, movie);

        MoviePartReadExtendedDTO readDTO = movieParticipationService
                .getExtendedMovieParticipation(movieParticipation.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieParticipation,
                "movie", "person");
        Assertions.assertThat(readDTO.getMovie()).isEqualToComparingFieldByField(movie);
        Assertions.assertThat(readDTO.getPerson()).isEqualToComparingFieldByField(person);
    }

    @Transactional
    @Test
    public void createMovieParticipationTest() {
        MoviePartCreateDTO createDTO = new MoviePartCreateDTO();
        createDTO.setPartInfo("some text");
        Set<PartType> types = new HashSet<>();
        types.add(PartType.WRITER);
        types.add(PartType.COSTUME_DESIGNER);
        createDTO.setPartTypes(types);

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
    public void deleteMovieParticipationTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieParticipation movieParticipation = createMovieParticipation(person, movie);

        movieParticipationService.deleteMovieParticipation(movieParticipation.getId());

        Assert.assertFalse(movieParticipationRepository.existsById(movieParticipation.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteMovieNotFound() {
        movieParticipationService.deleteMovieParticipation(UUID.randomUUID());
    }

    private MovieParticipation createMovieParticipation(Person person, Movie movie) {
        MovieParticipation movieParticipation = new MovieParticipation();
        movieParticipation.setPartInfo("Some text");
        movieParticipation.setAverageRating(5.0);
        movieParticipation.setPerson(person);
        movieParticipation.setMovie(movie);

        Set<PartType> types = new HashSet<>();
        types.add(PartType.WRITER);
        types.add(PartType.COSTUME_DESIGNER);
        movieParticipation.setPartTypes(types);

        movieParticipation = movieParticipationRepository.save(movieParticipation);
        return movieParticipation;
    }
}
