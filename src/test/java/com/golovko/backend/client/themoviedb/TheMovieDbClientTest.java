package com.golovko.backend.client.themoviedb;

import com.golovko.backend.BaseTest;
import com.golovko.backend.client.themoviedb.dto.*;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class TheMovieDbClientTest extends BaseTest {

    @Autowired
    private TheMovieDbClient theMovieDbClient;

    @Test
    public void testGetMovie() {
        String movieId = "200";

        MovieReadDTO readDTO = theMovieDbClient.getMovie(movieId, "ru");

        Assert.assertEquals(movieId, readDTO.getId());
        Assert.assertEquals("Star Trek: Insurrection", readDTO.getOriginalTitle());
        Assert.assertEquals("Звёздный путь 9: Восстание", readDTO.getTitle());
        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();
    }

    @Test
    public void testGetMovieDefaultLanguage() {
        String movieId = "200";

        MovieReadDTO readDTO = theMovieDbClient.getMovie(movieId, null);

        Assert.assertEquals(movieId, readDTO.getId());
        Assert.assertEquals("Star Trek: Insurrection", readDTO.getOriginalTitle());
        Assert.assertEquals(readDTO.getOriginalTitle(), readDTO.getTitle());
    }

    @Test
    public void testGetTopRatedMovies() {
        MoviesPageDTO moviesPageDTO = theMovieDbClient.getTopRatedMovies();

        Assert.assertTrue(moviesPageDTO.getTotalPages() > 0);
        Assert.assertTrue(moviesPageDTO.getTotalResults() > 0);
        Assert.assertTrue(moviesPageDTO.getResults().size() > 0);


        for (MovieReadShortDTO dto : moviesPageDTO.getResults()) {
            Assert.assertNotNull(dto.getId());
            Assert.assertNotNull(dto.getTitle());
        }
    }

    @Test
    public void testGetMovieWithGenres() {
        String movieId = "200";

        MovieReadDTO readDTO = theMovieDbClient.getMovie(movieId, "ru");

        Assert.assertEquals(movieId, readDTO.getId());
        Assert.assertEquals("Star Trek: Insurrection", readDTO.getOriginalTitle());
        Assert.assertEquals("Звёздный путь 9: Восстание", readDTO.getTitle());
        Assert.assertNotNull(readDTO.getGenres());
    }

    @Test
    public void testGetMovieCredits() {
        String movieId = "200";

        MovieCreditsReadDTO readDTO = theMovieDbClient.getMovieCastAndCrew(movieId, null);

        Assert.assertNotNull(readDTO.getCast());
        Assertions.assertThat(readDTO.getCast().get(0)).hasNoNullFieldsOrProperties();
        Assertions.assertThat(readDTO.getCast()).extracting("character")
                .contains("Captain Jean-Luc Picard");

        Assert.assertNotNull(readDTO.getCrew());
        Assertions.assertThat(readDTO.getCrew().get(0)).hasNoNullFieldsOrProperties();
        Assertions.assertThat(readDTO.getCrew()).extracting("department")
                .contains("Sound");
    }

    @Test
    public void testGetPerson() {
        String personId = "1100";

        PersonReadDTO readDTO = theMovieDbClient.getPerson(personId, null);

        Assert.assertEquals("Arnold Schwarzenegger", readDTO.getName());
        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();
    }
}
