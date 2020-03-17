package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.movie.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.job.UpdateAverageRatingOfMoviesJob;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.LikeRepository;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.repository.RatingRepository;
import com.golovko.backend.util.TestObjectFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.MOVIE;
import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {
        "delete from like",
        "delete from comment",
        "delete from genre_movie",
        "delete from genre",
        "delete from rating",
        "delete from user_role",
        "delete from application_user",
        "delete from genre_movie",
        "delete from genre",
        "delete from movie_cast",
        "delete from movie_crew",
        "delete from person",
        "delete from movie"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class MovieServiceTest {

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
    private TestObjectFactory testObjectFactory;

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

        movie.setGenres(Set.of(genre));
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

        MovieReadDTO readDTO = movieService.patchMovie(movie.getId(), patchDTO);

        assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        movie = movieRepository.findById(readDTO.getId()).get();
        assertThat(movie).isEqualToIgnoringGivenFields(readDTO,
                "movieCrews", "movieCasts", "genres");
    }

    @Test
    public void testPatchMovieEmptyPatch() {
        Movie movie = testObjectFactory.createMovie();

        MoviePatchDTO patchDTO = new MoviePatchDTO();
        MovieReadDTO readDTO = movieService.patchMovie(movie.getId(), patchDTO);

        assertThat(readDTO).hasNoNullFieldsOrPropertiesExcept("averageRating");

        Movie movieAfterUpdate = movieRepository.findById(readDTO.getId()).get();

        assertThat(movieAfterUpdate).hasNoNullFieldsOrPropertiesExcept("averageRating");
        assertThat(movie).isEqualToIgnoringGivenFields(movieAfterUpdate,
                "movieCrews", "movieCasts", "genres");
    }

    @Test
    public void testUpdateMovie() {
        Movie movie = testObjectFactory.createMovie();

        MoviePutDTO updateDTO = new MoviePutDTO();
        updateDTO.setMovieTitle("new title");
        updateDTO.setDescription("some NEW description");
        updateDTO.setIsReleased(false);
        updateDTO.setReleaseDate(LocalDate.parse("1900-07-10"));
        updateDTO.setAverageRating(5.5);

        MovieReadDTO readDTO = movieService.updateMovie(movie.getId(), updateDTO);

        assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        movie = movieRepository.findById(readDTO.getId()).get();
        assertThat(movie).isEqualToIgnoringGivenFields(readDTO,
                "movieCrews", "movieCasts", "genres");
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
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m2 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4));

        MovieFilter filter = new MovieFilter();
        assertThat(movieService.getMovies(filter)).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId(), m3.getId());
    }

    @Test
    public void testGetMoviesWithEmptySetsOfFilter() {
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m2 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4));

        MovieFilter filter = new MovieFilter();
        filter.setGenreNames(new HashSet<String>());
        filter.setMovieCrewTypes(new HashSet<MovieCrewType>());
        assertThat(movieService.getMovies(filter)).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId(), m3.getId());
    }

    @Test
    public void testGetMoviesByPerson() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m2 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4));
        createMovie(LocalDate.of(1944, 5, 4));

        testObjectFactory.createMovieCrew(person2, m1);
        testObjectFactory.createMovieCrew(person2, m2);
        testObjectFactory.createMovieCrew(person1, m3);

        MovieFilter filter = new MovieFilter();
        filter.setPersonId(person2.getId());
        assertThat(movieService.getMovies(filter)).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetMoviesByPartTypes() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m2 = createMovie(LocalDate.of(1990, 5, 4));
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4));
        Movie m4 = createMovie(LocalDate.of(1944, 5, 4));

        testObjectFactory.createMovieCrewForFilter(person2, m1, MovieCrewType.COMPOSER);
        testObjectFactory.createMovieCrewForFilter(person2, m2, MovieCrewType.WRITER);
        testObjectFactory.createMovieCrewForFilter(person1, m3, MovieCrewType.PRODUCER);
        testObjectFactory.createMovieCrewForFilter(person2, m4, MovieCrewType.COSTUME_DESIGNER);

        MovieFilter filter = new MovieFilter();
        filter.setMovieCrewTypes(Set.of(MovieCrewType.COMPOSER, MovieCrewType.WRITER));
        List<MovieReadDTO> filteredMovies = movieService.getMovies(filter);
        assertThat(filteredMovies).extracting("id")
                .containsExactlyInAnyOrder(m1.getId(), m2.getId());
    }

    @Test
    public void testGetMoviesByGenreNames() {
        Genre genre1 = testObjectFactory.createGenre("Comedy");
        Genre genre2 = testObjectFactory.createGenre("Horror");

        Movie m1 = testObjectFactory.createMovie();
        m1.setGenres(Set.of(genre1));
        Movie m2 = testObjectFactory.createMovie();
        m2.setGenres(Set.of(genre2));
        movieRepository.saveAll(List.of(m1, m2));

        MovieFilter filter = new MovieFilter();
        filter.setGenreNames(Set.of(genre1.getGenreName()));

        assertThat(movieService.getMovies(filter)).extracting("id")
                .containsExactlyInAnyOrder(m1.getId());
    }

    @Test
    public void testGetMoviesByReleasedInterval() {
        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();
        Movie m1 = createMovie(LocalDate.of(1992, 5, 4));
        Movie m2 = createMovie(LocalDate.of(1990, 5, 4));
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4));
        createMovie(LocalDate.of(1944, 5, 4));

        testObjectFactory.createMovieCrew(person2, m1);
        testObjectFactory.createMovieCrew(person2, m2);
        testObjectFactory.createMovieCrew(person1, m3);

        MovieFilter filter = new MovieFilter();
        filter.setReleasedFrom(LocalDate.of(1980, 5, 4));
        filter.setReleasedTo(LocalDate.of(1992, 5, 4));
        assertThat(movieService.getMovies(filter)).extracting("id")
                .containsExactlyInAnyOrder(m2.getId(), m3.getId());
    }

    @Test
    public void testGetMoviesByAllFilters() {
        Genre genre = testObjectFactory.createGenre("Comedy");

        Person person1 = testObjectFactory.createPerson();
        Person person2 = testObjectFactory.createPerson();

        Movie m1 = createMovie(LocalDate.of(1992, 5, 4)); // no
        Movie m2 = createMovie(LocalDate.of(1990, 5, 4)); // yes
        m2.setGenres(Set.of(genre));
        movieRepository.save(m2);
        Movie m3 = createMovie(LocalDate.of(1980, 5, 4)); // no
        Movie m4 = createMovie(LocalDate.of(1987, 5, 4));

        testObjectFactory.createMovieCrewForFilter(person2, m1, MovieCrewType.COMPOSER);
        testObjectFactory.createMovieCrewForFilter(person2, m2, MovieCrewType.WRITER);
        testObjectFactory.createMovieCrewForFilter(person1, m3, MovieCrewType.PRODUCER);
        testObjectFactory.createMovieCrewForFilter(person2, m4, MovieCrewType.COSTUME_DESIGNER);

        MovieFilter filter = new MovieFilter();
        filter.setPersonId(person2.getId());
        filter.setMovieCrewTypes(Set.of(MovieCrewType.COMPOSER, MovieCrewType.WRITER));
        filter.setReleasedFrom(LocalDate.of(1980, 5, 4));
        filter.setReleasedTo(LocalDate.of(1992, 5, 4));
        filter.setGenreNames(Set.of(genre.getGenreName()));

        List<MovieReadDTO> filteredMovies = movieService.getMovies(filter);
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
