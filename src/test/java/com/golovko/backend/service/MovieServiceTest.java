package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.movie.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.job.UpdateAverageRatingOfMoviesJob;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.LikeRepository;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.repository.RatingRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionSystemException;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.golovko.backend.domain.TargetObjectType.MOVIE;
import static org.assertj.core.api.Assertions.assertThat;

public class MovieServiceTest extends BaseTest {

    @Autowired
    private MovieService movieService;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private UpdateAverageRatingOfMoviesJob updateAverageRatingOfMoviesJob;

    @Test
    public void testGetMovieById() {
        Movie movie = testObjectFactory.createMovie();

        MovieReadDTO readDTO = movieService.getMovie(movie.getId());
        assertThat(readDTO).isEqualToComparingFieldByField(movie);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetMovieWrongId() {
        movieService.getMovie(UUID.randomUUID());
    }

    @Test
    public void testGetMovieExtended() {
        Genre genre = testObjectFactory.createGenre("Horror");
        Movie movie = testObjectFactory.createMovie();
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        MovieCast movieCast = testObjectFactory.createMovieCast(person1, movie);
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person2, movie);

        movie.setGenres(List.of(genre));
        movie.setMovieCasts(Set.of(movieCast));
        movie.setMovieCrews(Set.of(movieCrew));
        Movie extendedMovie = movieRepository.save(movie);

        MovieReadExtendedDTO extendedDTO = movieService.getMovieExtended(extendedMovie.getId());

        assertThat(extendedDTO).isEqualToIgnoringGivenFields(extendedMovie,
                "genres", "movieCasts", "movieCrews");
        assertThat(extendedDTO.getGenres()).extracting("id").containsExactlyInAnyOrder(genre.getId());
        assertThat(extendedDTO.getMovieCasts()).extracting("id").containsExactlyInAnyOrder(movieCast.getId());
        assertThat(extendedDTO.getMovieCrews()).extracting("id").containsExactlyInAnyOrder(movieCrew.getId());
    }

    @Test
    public void testCreateMovie() {
        MovieCreateDTO createDTO = new MovieCreateDTO();
        createDTO.setMovieTitle("title");
        createDTO.setDescription("description");
        createDTO.setIsReleased(true);
        createDTO.setReleaseDate(LocalDate.parse("1900-01-01"));

        MovieReadDTO readDTO = movieService.createMovie(createDTO);

        assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        Movie movie = movieRepository.findById(readDTO.getId()).get();
        assertThat(readDTO).isEqualToComparingFieldByField(movie);
    }

    @Test
    public void testPatchMovie() {
        Movie movie = testObjectFactory.createMovie();

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setMovieTitle("another title");
        patchDTO.setDescription("another description");
        patchDTO.setIsReleased(true);
        patchDTO.setReleaseDate(LocalDate.parse("2002-02-03"));
        patchDTO.setPosterUrl("poster url");
        patchDTO.setTrailerUrl("trailer url");

        MovieReadDTO readDTO = movieService.patchMovie(movie.getId(), patchDTO);

        assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        movie = movieRepository.findById(readDTO.getId()).get();
        assertThat(movie).isEqualToIgnoringGivenFields(readDTO,
                "movieCrews", "movieCasts", "genres", "articles");
    }

    @Test
    public void testPatchMovieEmptyPatch() {
        Movie movie = testObjectFactory.createMovie();

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        MovieReadDTO readDTO = movieService.patchMovie(movie.getId(), patchDTO);

        assertThat(readDTO).hasNoNullFieldsOrPropertiesExcept("averageRating", "predictedAverageRating",
                "likesCount", "dislikesCount");

        Movie afterUpdate = movieRepository.findById(readDTO.getId()).get();

        assertThat(afterUpdate).hasNoNullFieldsOrPropertiesExcept("averageRating", "predictedAverageRating",
                "likesCount", "dislikesCount");

        assertThat(movie).isEqualToIgnoringGivenFields(afterUpdate,
                "movieCrews", "movieCasts", "genres", "articles");
    }

    @Test
    public void testUpdateMovie() {
        Movie movie = testObjectFactory.createMovie();

        MoviePutDTO updateDTO = new MoviePutDTO();
        updateDTO.setMovieTitle("new title");
        updateDTO.setDescription("some NEW description");
        updateDTO.setIsReleased(false);
        updateDTO.setReleaseDate(LocalDate.parse("1900-07-10"));

        MovieReadDTO readDTO = movieService.updateMovie(movie.getId(), updateDTO);

        assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        movie = movieRepository.findById(readDTO.getId()).get();
        assertThat(movie).isEqualToIgnoringGivenFields(readDTO,
                "movieCrews", "movieCasts", "genres", "articles");
    }

    @Test
    public void testDeleteMovie() {
        Movie movie = testObjectFactory.createMovie();
        movieService.deleteMovie(movie.getId());

        Assert.assertFalse(movieRepository.existsById(movie.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteMovieNotFound() {
        movieService.deleteMovie(UUID.randomUUID());
    }

    @Test
    public void testDeleteMovieWithCompositeItems() {
        ApplicationUser author = testObjectFactory.createUser();
        ApplicationUser user = testObjectFactory.createUser();

        Movie movie = testObjectFactory.createMovie();
        Comment c1 = testObjectFactory.createComment(author, movie.getId(), CommentStatus.APPROVED, MOVIE);
        Comment c2 = testObjectFactory.createComment(author, movie.getId(), CommentStatus.APPROVED, MOVIE);

        Like like1 = testObjectFactory.createLike(true, user, movie.getId(), MOVIE);
        Like like2 = testObjectFactory.createLike(true, author, movie.getId(), MOVIE);

        Rating r1 = testObjectFactory.createRating(5, user, movie.getId(), MOVIE);
        Rating r2 = testObjectFactory.createRating(5, author, movie.getId(), MOVIE);

        movieService.deleteMovie(movie.getId());

        Assert.assertFalse(movieRepository.existsById(movie.getId()));

        Assert.assertFalse(commentRepository.existsById(c1.getId()));
        Assert.assertFalse(commentRepository.existsById(c2.getId()));

        Assert.assertFalse(likeRepository.existsById(like1.getId()));
        Assert.assertFalse(likeRepository.existsById(like2.getId()));

        Assert.assertFalse(ratingRepository.existsById(r1.getId()));
        Assert.assertFalse(ratingRepository.existsById(r2.getId()));
    }

    @Test
    public void testGetMoviesWithEmptyFilter() {
        Movie m1 = testObjectFactory.createMovie(LocalDate.of(1992, 5, 4), false);
        Movie m2 = testObjectFactory.createMovie(LocalDate.of(1992, 5, 4), false);
        Movie m3 = testObjectFactory.createMovie(LocalDate.of(1980, 5, 4), false);

        MovieFilter filter = new MovieFilter();
        assertThat(movieService.getMovies(filter, Pageable.unpaged()).getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId(), m3.getId());
    }

    @Test
    public void testGetMoviesWithEmptySetsOfFilter() {
        Movie m1 = testObjectFactory.createMovie(LocalDate.of(1992, 5, 4), false);
        Movie m2 = testObjectFactory.createMovie(LocalDate.of(1992, 5, 4), false);
        Movie m3 = testObjectFactory.createMovie(LocalDate.of(1980, 5, 4), false);

        MovieFilter filter = new MovieFilter();
        filter.setGenreNames(new HashSet<String>());
        filter.setMovieCrewTypes(new HashSet<MovieCrewType>());
        assertThat(movieService.getMovies(filter, Pageable.unpaged()).getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId(), m3.getId());
    }

    @Test
    public void testGetMoviesByPerson() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie(LocalDate.of(1992, 5, 4), false);
        Movie m2 = testObjectFactory.createMovie(LocalDate.of(1992, 5, 4), false);
        Movie m3 = testObjectFactory.createMovie(LocalDate.of(1980, 5, 4), false);
        testObjectFactory.createMovie(LocalDate.of(1944, 5, 4), false);

        testObjectFactory.createMovieCrew(person2, m1);
        testObjectFactory.createMovieCrew(person2, m2);
        testObjectFactory.createMovieCrew(person1, m3);

        MovieFilter filter = new MovieFilter();
        filter.setPersonId(person2.getId());
        assertThat(movieService.getMovies(filter, Pageable.unpaged()).getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetMoviesByPartTypes() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie(LocalDate.of(1992, 5, 4), false);
        Movie m2 = testObjectFactory.createMovie(LocalDate.of(1990, 5, 4), false);
        Movie m3 = testObjectFactory.createMovie(LocalDate.of(1980, 5, 4), false);
        Movie m4 = testObjectFactory.createMovie(LocalDate.of(1944, 5, 4), false);

        testObjectFactory.createMovieCrew(person2, m1, MovieCrewType.SOUND);
        testObjectFactory.createMovieCrew(person2, m2, MovieCrewType.WRITER);
        testObjectFactory.createMovieCrew(person1, m3, MovieCrewType.PRODUCER);
        testObjectFactory.createMovieCrew(person2, m4, MovieCrewType.COSTUME_DESIGNER);

        MovieFilter filter = new MovieFilter();
        filter.setMovieCrewTypes(Set.of(MovieCrewType.SOUND, MovieCrewType.WRITER));
        List<MovieReadDTO> filteredMovies = movieService.getMovies(filter, Pageable.unpaged()).getData();
        assertThat(filteredMovies).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetMoviesByGenreNames() {
        Genre genre1 = testObjectFactory.createGenre("Comedy");
        Genre genre2 = testObjectFactory.createGenre("Horror");

        Movie m1 = testObjectFactory.createMovie();
        m1.setGenres(List.of(genre1));
        Movie m2 = testObjectFactory.createMovie();
        m2.setGenres(List.of(genre2));
        movieRepository.saveAll(List.of(m1, m2));

        MovieFilter filter = new MovieFilter();
        filter.setGenreNames(Set.of(genre1.getGenreName()));

        assertThat(movieService.getMovies(filter, Pageable.unpaged()).getData()).extracting("id")
                .containsExactlyInAnyOrder(m1.getId());
    }

    @Test
    public void testGetMoviesByReleasedInterval() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = testObjectFactory.createMovie(LocalDate.of(1992, 5, 4), false);
        Movie m2 = testObjectFactory.createMovie(LocalDate.of(1990, 5, 4), false);
        Movie m3 = testObjectFactory.createMovie(LocalDate.of(1980, 5, 4), false);
        testObjectFactory.createMovie(LocalDate.of(1944, 5, 4), false);

        testObjectFactory.createMovieCrew(person2, m1);
        testObjectFactory.createMovieCrew(person2, m2);
        testObjectFactory.createMovieCrew(person1, m3);

        MovieFilter filter = new MovieFilter();
        filter.setReleasedFrom(LocalDate.of(1980, 5, 4));
        filter.setReleasedTo(LocalDate.of(1992, 5, 4));
        assertThat(movieService.getMovies(filter, Pageable.unpaged()).getData()).extracting("id")
                .containsExactlyInAnyOrder(m2.getId(), m3.getId());
    }

    @Test
    public void testGetMoviesByAllFilters() {
        Genre genre = testObjectFactory.createGenre("Comedy");

        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();

        Movie m1 = testObjectFactory.createMovie(LocalDate.of(1992, 5, 4), false); // no
        Movie m2 = testObjectFactory.createMovie(LocalDate.of(1990, 5, 4), false); // yes
        m2.setGenres(List.of(genre));
        movieRepository.save(m2);
        Movie m3 = testObjectFactory.createMovie(LocalDate.of(1980, 5, 4), false); // no
        Movie m4 = testObjectFactory.createMovie(LocalDate.of(1987, 5, 4), false);

        testObjectFactory.createMovieCrew(person2, m1, MovieCrewType.SOUND);
        testObjectFactory.createMovieCrew(person2, m2, MovieCrewType.WRITER);
        testObjectFactory.createMovieCrew(person1, m3, MovieCrewType.PRODUCER);
        testObjectFactory.createMovieCrew(person2, m4, MovieCrewType.COSTUME_DESIGNER);

        MovieFilter filter = new MovieFilter();
        filter.setPersonId(person2.getId());
        filter.setMovieCrewTypes(Set.of(MovieCrewType.SOUND, MovieCrewType.WRITER));
        filter.setReleasedFrom(LocalDate.of(1980, 5, 4));
        filter.setReleasedTo(LocalDate.of(1992, 5, 4));
        filter.setGenreNames(Set.of(genre.getGenreName()));

        List<MovieReadDTO> filteredMovies = movieService.getMovies(filter, Pageable.unpaged()).getData();
        assertThat(filteredMovies).extracting("id")
                .containsExactlyInAnyOrder(m2.getId());
    }

    @Test
    public void testCalcAverageRatingOfMovie() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        testObjectFactory.createRating(3, u1, movie.getId(), MOVIE);
        testObjectFactory.createRating(6, u2, movie.getId(), MOVIE);

        movieService.updateAverageRatingOfMovie(movie.getId());

        movie = movieRepository.findById(movie.getId()).get();
        Assert.assertEquals(4.5, movie.getAverageRating(), Double.MIN_NORMAL);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieNotNullValidation() {
        Movie movie = new Movie();
        movieRepository.save(movie);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieMaxSizeValidation() {
        Movie movie = new Movie();
        movie.setMovieTitle("movie title".repeat(100));
        movie.setDescription("movie title".repeat(1000));
        movie.setIsReleased(true);
        movie.setReleaseDate(LocalDate.of(2019, 5, 12));
        movieRepository.save(movie);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieMinSizeValidation() {
        Movie movie = new Movie();
        movie.setMovieTitle("");
        movie.setDescription("");
        movie.setIsReleased(true);
        movie.setReleaseDate(LocalDate.of(2019, 5, 12));
        movieRepository.save(movie);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieMinRatingValidation() {
        Movie movie = new Movie();
        movie.setMovieTitle("text");
        movie.setDescription("text");
        movie.setIsReleased(true);
        movie.setReleaseDate(LocalDate.of(2019, 5, 12));
        movie.setAverageRating(-0.01);
        movieRepository.save(movie);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieMaxRatingValidation() {
        Movie movie = new Movie();
        movie.setMovieTitle("text");
        movie.setDescription("text");
        movie.setIsReleased(true);
        movie.setReleaseDate(LocalDate.of(2019, 5, 12));
        movie.setAverageRating(10.01);
        movieRepository.save(movie);
    }

    @Test
    public void testGetMoviesWithFilterWithPagingAndSorting() {
        Movie m1 = testObjectFactory.createMovie(LocalDate.of(1992, 5, 4), false);
        Movie m2 = testObjectFactory.createMovie(LocalDate.of(1990, 5, 4), false);
        testObjectFactory.createMovie(LocalDate.of(1980, 5, 4), false);

        MovieFilter filter = new MovieFilter();
        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.DESC, "releaseDate"));

        Assertions.assertThat(movieService.getMovies(filter, pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(m1.getId(), m2.getId()));
    }

    @Test
    public void testUpdateReleasedStatusOfMovieFutureDate() {
        Movie movie = testObjectFactory.createMovie(LocalDate.of(2022, 5, 4), false);

        movieService.updateReleasedStatusOfMovie(movie.getId());

        Movie updatedMovie = movieRepository.findById(movie.getId()).get();
        Assert.assertEquals(false, updatedMovie.getIsReleased());
    }

    @Test
    public void testUpdateReleasedStatusOfMoviePastDate() {
        Movie movie = testObjectFactory.createMovie(LocalDate.of(2006, 5, 4), false);

        movieService.updateReleasedStatusOfMovie(movie.getId());

        Movie updatedMovie = movieRepository.findById(movie.getId()).get();
        Assert.assertEquals(true, updatedMovie.getIsReleased());
    }

    @Test
    public void testUpdateReleasedStatusOfMovieTodayDate() {
        Movie movie = testObjectFactory.createMovie(LocalDate.now(), false);

        movieService.updateReleasedStatusOfMovie(movie.getId());

        Movie updatedMovie = movieRepository.findById(movie.getId()).get();
        Assert.assertEquals(true, updatedMovie.getIsReleased());
    }

    @Test
    public void testGetTopRatedMovies() {
        Set<UUID> movieIds = new HashSet<>();
        for (int i = 0; i < 100; ++i) {
            movieIds.add(testObjectFactory.createMovieInLeaderBoard().getId());
        }

        PageRequest pageRequest = PageRequest.of(0, 100,
                Sort.by(Sort.Direction.DESC, "averageRating"));

        PageResult<MoviesTopRatedDTO> actualResult = movieService.getTopRatedMovies(pageRequest);

        Assertions.assertThat(actualResult.getData()).isSortedAccordingTo(
                Comparator.comparing(MoviesTopRatedDTO::getAverageRating).reversed());

        Assert.assertEquals(movieIds, actualResult.getData().stream()
                .map(MoviesTopRatedDTO::getId)
                .collect(Collectors.toSet()));

        for (MoviesTopRatedDTO m : actualResult.getData()) {
            Assert.assertNotNull(m.getAverageRating());
            Assert.assertNotNull(m.getDislikesCount());
            Assert.assertNotNull(m.getLikesCount());
            Assert.assertNotNull(m.getMovieTitle());
        }
    }

    @Test
    public void testUpdatePredictedAverageRatingOfMovie() {
        Movie m1 = testObjectFactory.createMovie();
        Person p1 = testObjectFactory.createPerson(5.0);
        Person p2 = testObjectFactory.createPerson(3.0);

        testObjectFactory.createMovieCast(p1, m1);
        testObjectFactory.createMovieCast(p2, m1);

        movieService.updatePredictedAverageRatingOfMovie(m1.getId());

        Movie updatedMovie = movieRepository.findById(m1.getId()).get();
        Assert.assertEquals(4.0, updatedMovie.getPredictedAverageRating(), Double.MIN_NORMAL);
    }
}
