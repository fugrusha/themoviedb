package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.MovieCastRepository;
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

import java.util.Arrays;
import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.MOVIE_CAST;

public class MovieCastServiceTest extends BaseTest {

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private MovieCastService movieCastService;

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

        PageResult<MovieCastReadDTO> result = movieCastService.getAllMovieCasts(movie.getId(), Pageable.unpaged());

        Assertions.assertThat(result.getData()).extracting(MovieCastReadDTO::getId)
                .containsExactlyInAnyOrder(movieCast.getId());
    }

    @Test
    public void testGetMovieCastsWithPagingAndSorting() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast mc1 = testObjectFactory.createMovieCast(person, movie);
        MovieCast mc2 = testObjectFactory.createMovieCast(person, movie);
        testObjectFactory.createMovieCast(person, movie);
        testObjectFactory.createMovieCast(person, movie);

        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "createdAt"));

        Assertions.assertThat(movieCastService.getAllMovieCasts(movie.getId(), pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(mc1.getId(), mc2.getId()));
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
        patchDTO.setPersonId(person.getId());
        patchDTO.setGender(Gender.MALE);
        patchDTO.setOrderNumber(5);

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
    public void testDeleteMovieCastWithCompositeItems() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCast mc = testObjectFactory.createMovieCast(person, movie);

        ApplicationUser author = testObjectFactory.createUser();
        Comment c1 = testObjectFactory.createComment(author, mc.getId(), CommentStatus.APPROVED, MOVIE_CAST);
        Comment c2 = testObjectFactory.createComment(author, mc.getId(), CommentStatus.APPROVED, MOVIE_CAST);

        Rating r1 = testObjectFactory.createRating(5, author, mc.getId(), MOVIE_CAST);

        movieCastService.deleteMovieCast(mc.getId(), movie.getId());

        Assert.assertTrue(movieRepository.existsById(movie.getId()));
        Assert.assertFalse(movieCastRepository.existsById(mc.getId()));

        Assert.assertFalse(commentRepository.existsById(c1.getId()));
        Assert.assertFalse(commentRepository.existsById(c2.getId()));

        Assert.assertFalse(ratingRepository.existsById(r1.getId()));
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

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieCastNotNullException() {
        MovieCast mc = new MovieCast();
        movieCastRepository.save(mc);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieCastMaxSizeValidation() {
        Movie movie = testObjectFactory.createMovie();

        MovieCast mc = new MovieCast();
        mc.setDescription("Long long text".repeat(1000));
        mc.setCharacter("Long long text".repeat(1000));
        mc.setMovie(movie);
        movieCastRepository.save(mc);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieCastMinSizeValidation() {
        Movie movie = testObjectFactory.createMovie();

        MovieCast mc = new MovieCast();
        mc.setDescription("");
        mc.setCharacter("");
        mc.setMovie(movie);
        movieCastRepository.save(mc);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieCastMinRatingValidation() {
        Movie movie = testObjectFactory.createMovie();

        MovieCast mc = new MovieCast();
        mc.setDescription("text");
        mc.setCharacter("text");
        mc.setMovie(movie);
        mc.setAverageRating(-0.01);
        movieCastRepository.save(mc);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieCastMaxRatingValidation() {
        Movie movie = testObjectFactory.createMovie();

        MovieCast mc = new MovieCast();
        mc.setDescription("text");
        mc.setCharacter("text");
        mc.setMovie(movie);
        mc.setAverageRating(10.01);
        movieCastRepository.save(mc);
    }
}
