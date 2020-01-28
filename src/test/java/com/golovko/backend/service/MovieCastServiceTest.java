package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.PartType;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieCastRepository;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.UUID;

@SpringBootTest
@RunWith(SpringRunner.class)
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
        MovieCast movieCast = createMovieCast(person, movie);

        MovieCastReadDTO readDTO = movieCastService.getMovieCast(movieCast.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCast,
                "movieId", "personId");
        Assertions.assertThat(readDTO.getMovieId()).isEqualToComparingFieldByField(movie.getId());
        Assertions.assertThat(readDTO.getPersonId()).isEqualToComparingFieldByField(person.getId());
    }

    @Test
    public void deleteMovieCastTest() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast movieCast = createMovieCast(person, movie);

        movieCastService.deleteMovieCast(movieCast.getId());

        Assert.assertFalse(movieCastRepository.existsById(movieCast.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void deleteMovieCastNotFound() {
        movieCastService.deleteMovieCast(UUID.randomUUID());
    }

    private MovieCast createMovieCast(Person person, Movie movie) {
        MovieCast movieCast = new MovieCast();
        movieCast.setPartInfo("Some text");
        movieCast.setAverageRating(5.0);
        movieCast.setPerson(person);
        movieCast.setMovie(movie);
        movieCast.setPartType(PartType.CAST);
        movieCast.setCharacter("Leon");

        movieCast = movieCastRepository.save(movieCast);
        return movieCast;
    }

}
