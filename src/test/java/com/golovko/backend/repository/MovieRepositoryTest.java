package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.movie.MoviesTopRatedDTO;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MovieRepositoryTest extends BaseTest {

    @Autowired
    private MovieRepository movieRepository;

    @Test
    public void testCreatedAtIsSet() {
        Movie movie = testObjectFactory.createMovie();

        Instant createdAtBeforeReload = movie.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        movie = movieRepository.findById(movie.getId()).get();

        Instant createdAtAfterReload = movie.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        Movie movie = testObjectFactory.createMovie();

        Instant modifiedAtBeforeReload = movie.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        movie = movieRepository.findById(movie.getId()).get();
        movie.setMovieTitle("Another Movie Title");
        movie = movieRepository.save(movie);

        Instant modifiedAtAfterReload = movie.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }

    @Test
    public void testGetIdsOfMovies() {
        Set<UUID> expectedResult = new HashSet<>();
        expectedResult.add(testObjectFactory.createMovie().getId());
        expectedResult.add(testObjectFactory.createMovie().getId());
        expectedResult.add(testObjectFactory.createMovie().getId());

        transactionTemplate.executeWithoutResult(status -> {
            Set<UUID> actualResult = movieRepository.getIdsOfMovies().collect(Collectors.toSet());
            Assert.assertEquals(expectedResult, actualResult);
        });
    }

    @Test
    public void testCalcAverageRatingOfPersonMovies() {
        Movie m1 = testObjectFactory.createMovie(5.0);
        Movie m2 = testObjectFactory.createMovie(4.0);
        Movie m3 = testObjectFactory.createMovie((Double) null);
        Movie m4 = testObjectFactory.createMovie(9.0);

        Person p1 = testObjectFactory.createPerson();
        Person p2 = testObjectFactory.createPerson();

        testObjectFactory.createMovieCast(p1, m1);
        testObjectFactory.createMovieCast(p1, m2);
        testObjectFactory.createMovieCast(p1, m3);
        testObjectFactory.createMovieCast(p2, m4); // wrong person
        testObjectFactory.createMovieCast(p2, m3); // wrong person

        Double result = movieRepository.calcAverageRatingOfPersonMovies(p1.getId());
        Assert.assertEquals(4.5, result, Double.MIN_NORMAL);
    }

    @Test
    public void testIncrementLikesCountField() {
        Movie m1 = testObjectFactory.createMovie();
        m1.setLikesCount(5);
        movieRepository.save(m1);

        transactionTemplate.executeWithoutResult(status -> {
            movieRepository.incrementLikesCountField(m1.getId());
        });

        Movie updatedMovie = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals((Integer) 6, updatedMovie.getLikesCount());
    }

    @Test
    public void testDecrementLikesCountField() {
        Movie m1 = testObjectFactory.createMovie();
        m1.setLikesCount(5);
        movieRepository.save(m1);

        transactionTemplate.executeWithoutResult(status ->  {
            movieRepository.decrementLikesCountField(m1.getId());
        });

        Movie updatedMovie = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals((Integer) 4, updatedMovie.getLikesCount());
    }

    @Test
    public void testIncrementDislikesCountField() {
        Movie m1 = testObjectFactory.createMovie();
        m1.setDislikesCount(5);
        movieRepository.save(m1);

        transactionTemplate.executeWithoutResult(status -> {
            movieRepository.incrementDislikesCountField(m1.getId());
        });

        Movie updatedMovie = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals((Integer) 6, updatedMovie.getDislikesCount());
    }

    @Test
    public void testDecrementDislikesCountField() {
        Movie m1 = testObjectFactory.createMovie();
        m1.setDislikesCount(5);
        movieRepository.save(m1);

        transactionTemplate.executeWithoutResult(status ->  {
            movieRepository.decrementDislikesCountField(m1.getId());
        });

        Movie updatedMovie = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals((Integer) 4, updatedMovie.getDislikesCount());
    }

    @Test
    public void testGetIdsOfUnreleasedMovies() {
        Movie m1 = testObjectFactory.createMovie();
        Movie m2 = testObjectFactory.createMovie();
        Movie m3 = testObjectFactory.createMovie();

        m1.setIsReleased(false);
        m2.setIsReleased(true);
        m3.setIsReleased(false);
        movieRepository.saveAll(List.of(m1, m2, m3));

        Set<UUID> expectedResult = Set.of(m1.getId(), m3.getId());

        transactionTemplate.executeWithoutResult(status -> {
            Set<UUID> actualResult = movieRepository.getIdsOfUnreleasedMovies().collect(Collectors.toSet());
            Assert.assertEquals(expectedResult, actualResult);
        });
    }

    @Test
    public void testGetTopRatedMovies() {
        Set<UUID> movieIds = new HashSet<>();
        for (int i = 0; i < 100; ++i) {
            movieIds.add(testObjectFactory.createMovieInLeaderBoard().getId());
        }

        PageRequest pageRequest = PageRequest.of(0, 100,
                Sort.by(Sort.Direction.DESC, "averageRating"));
        Page<MoviesTopRatedDTO> actualResult = movieRepository.getTopRatedMovies(pageRequest);

        Assertions.assertThat(actualResult.getContent()).isSortedAccordingTo(
                Comparator.comparing(MoviesTopRatedDTO::getAverageRating).reversed());

        Assert.assertEquals(movieIds, actualResult.stream()
                .map(MoviesTopRatedDTO::getId)
                .collect(Collectors.toSet()));

        for (MoviesTopRatedDTO m : actualResult) {
            Assert.assertNotNull(m.getAverageRating());
            Assert.assertNotNull(m.getDislikesCount());
            Assert.assertNotNull(m.getLikesCount());
            Assert.assertNotNull(m.getMovieTitle());
        }
    }

    @Test
    public void testExistsMovieByMovieTitle() {
        LocalDate releaseDate = LocalDate.of(1995, 5, 5);

        Movie m1 = testObjectFactory.createMovie(releaseDate, true);
        testObjectFactory.createMovie(releaseDate, true);

        String movieTitle = m1.getMovieTitle();

        Assert.assertTrue(movieRepository.existsMovieByMovieTitleAndReleaseDate(movieTitle, releaseDate));
    }
}
