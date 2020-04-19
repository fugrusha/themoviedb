package com.golovko.backend.client.themoviedb;

import com.golovko.backend.client.themoviedb.dto.MovieCreditsReadDTO;
import com.golovko.backend.client.themoviedb.dto.MovieReadDTO;
import com.golovko.backend.client.themoviedb.dto.MoviesPageDTO;
import com.golovko.backend.client.themoviedb.dto.PersonReadDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(value = "api.themoviedb.org",
        url = "${themoviedb.api.url}",
        configuration = TheMovieDbClientConfig.class)
public interface TheMovieDbClient {

    @RequestMapping(method = RequestMethod.GET, value = "/movie/{movieId}")
    MovieReadDTO getMovie(@PathVariable("movieId") String movieId,
                          @RequestParam(defaultValue = "en") String language);

    @RequestMapping(method = RequestMethod.GET, value = "/movie/top_rated")
    MoviesPageDTO getTopRatedMovies();

    @RequestMapping(method = RequestMethod.GET, value = "/movie/{movieId}/credits")
    MovieCreditsReadDTO getMovieCastAndCrew(@PathVariable("movieId") String movieId,
                                            @RequestParam(defaultValue = "en") String language);

    @RequestMapping(method = RequestMethod.GET, value = "/person/{personId}")
    PersonReadDTO getPerson(@PathVariable("personId") String personId,
                            @RequestParam(defaultValue = "en") String language);
}
