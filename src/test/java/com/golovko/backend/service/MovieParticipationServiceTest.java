package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieParticipation;
import com.golovko.backend.domain.PartType;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.movieParticipation.MoviePartReadDTO;
import com.golovko.backend.dto.movieParticipation.MoviePartReadExtendedDTO;
import com.golovko.backend.repository.MovieParticipationRepository;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
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
