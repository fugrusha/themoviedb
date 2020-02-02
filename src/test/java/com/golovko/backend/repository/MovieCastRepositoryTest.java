package com.golovko.backend.repository;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.Person;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from person", "delete from movie", "delete from movie_cast"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieCastRepositoryTest {

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Test
    public void getMoviesWithActor() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4)); // releasedFrom
        Movie m2 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4));
        Movie m4 = createMovie(LocalDate.of(2000, 5, 4)); // releasedTo

        MovieCast movieCast1 = testObjectFactory.createMovieCast(person1, m1); // yes
        testObjectFactory.createMovieCast(person2, m2); // no
        testObjectFactory.createMovieCast(person1, m3); // no
        testObjectFactory.createMovieCast(person2, m4); // no
        testObjectFactory.createMovieCast(person1, m4); // no

        List<MovieCast> result = movieCastRepository.findMovieCastsWithActorInGivenInterval(person1.getId(),
                LocalDate.of(1992, 5, 4),
                LocalDate.of(2000, 5, 4));

        Assertions.assertThat(result).extracting(MovieCast::getId).containsExactlyInAnyOrder(movieCast1.getId());
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
