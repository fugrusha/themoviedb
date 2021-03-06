package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.moviecrew.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.CommentRepository;
import com.golovko.backend.repository.MovieCrewRepository;
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

import static com.golovko.backend.domain.TargetObjectType.MOVIE_CREW;

public class MovieCrewServiceTest extends BaseTest {

    @Autowired
    private MovieCrewService movieCrewService;

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    public void testGetMovieCrew() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        MovieCrewReadDTO readDTO = movieCrewService.getMovieCrew(movie.getId(), movieCrew.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movie.getId());
        Assert.assertEquals(readDTO.getPersonId(), person.getId());
    }

    @Test
    public void testGetMovieCrewExtended() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        MovieCrewReadExtendedDTO readDTO = movieCrewService.getExtendedMovieCrew(movie.getId(), movieCrew.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movie", "person");
        Assertions.assertThat(readDTO.getMovie()).isEqualToComparingFieldByField(movie);
        Assertions.assertThat(readDTO.getPerson()).isEqualToComparingFieldByField(person);
    }

    @Test
    public void testGetAllMovieCrewsByMovieId() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        PageResult<MovieCrewReadDTO> result = movieCrewService.getAllMovieCrews(movie.getId(), Pageable.unpaged());

        Assertions.assertThat(result.getData()).extracting(MovieCrewReadDTO::getId)
                .containsExactlyInAnyOrder(movieCrew.getId());
    }

    @Test
    public void testGetMovieCrewsWithPagingAndSorting() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew mc1 = testObjectFactory.createMovieCrew(person, movie);
        MovieCrew mc2 = testObjectFactory.createMovieCrew(person, movie);
        testObjectFactory.createMovieCrew(person, movie);
        testObjectFactory.createMovieCrew(person, movie);

        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.ASC, "createdAt"));

        Assertions.assertThat(movieCrewService.getAllMovieCrews(movie.getId(), pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(mc1.getId(), mc2.getId()));
    }

    @Test
    public void testCreateMovieCrew() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();

        MovieCrewCreateDTO createDTO = new MovieCrewCreateDTO();
        createDTO.setPersonId(person.getId());
        createDTO.setDescription("some text");
        createDTO.setMovieCrewType(MovieCrewType.COSTUME_DESIGNER);

        MovieCrewReadDTO readDTO = movieCrewService.createMovieCrew(createDTO, movie.getId());

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        MovieCrew movieCrew = movieCrewRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movieCrew.getMovie().getId());
        Assert.assertEquals(readDTO.getPersonId(), movieCrew.getPerson().getId());
    }

    @Test
    public void testCreateMovieCrewWithoutPerson() {
        Movie movie = testObjectFactory.createMovie();

        MovieCrewCreateDTO createDTO = new MovieCrewCreateDTO();
        createDTO.setPersonId(null);
        createDTO.setDescription("Some text");
        createDTO.setMovieCrewType(MovieCrewType.DIRECTOR);

        MovieCrewReadDTO readDTO = movieCrewService.createMovieCrew(createDTO, movie.getId());

        Assertions.assertThat(createDTO).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());
        Assert.assertNull(readDTO.getPersonId());

        MovieCrew movieCrew = movieCrewRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movieId", "personId");
        Assert.assertEquals(readDTO.getMovieId(), movie.getId());
        Assert.assertNull(movieCrew.getPerson());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateMovieCrewWrongPersonId() {
        Movie movie = testObjectFactory.createMovie();

        MovieCrewCreateDTO createDTO = new MovieCrewCreateDTO();
        createDTO.setPersonId(UUID.randomUUID());
        createDTO.setDescription("some text");
        createDTO.setMovieCrewType(MovieCrewType.COSTUME_DESIGNER);

        movieCrewService.createMovieCrew(createDTO, movie.getId());
    }

    @Test
    public void testPatchMovieCrew() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        MovieCrewPatchDTO patchDTO = new MovieCrewPatchDTO();
        patchDTO.setMovieCrewType(MovieCrewType.SOUND);
        patchDTO.setDescription("New text");
        patchDTO.setPersonId(person.getId());;

        MovieCrewReadDTO readDTO = movieCrewService.patchMovieCrew(movie.getId(), movieCrew.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        movieCrew = movieCrewRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movieId", "personId");
        Assert.assertEquals(movieCrew.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(movieCrew.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void testPatchMovieCrewEmptyPatch() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        MovieCrewPatchDTO patchDTO = new MovieCrewPatchDTO();

        MovieCrewReadDTO readDTO = movieCrewService.patchMovieCrew(movie.getId(), movieCrew.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrPropertiesExcept("averageRating");

        inTransaction(() -> {
            MovieCrew moviePartAfterUpdate = movieCrewRepository.findById(readDTO.getId()).get();
            Assertions.assertThat(moviePartAfterUpdate).isEqualToIgnoringGivenFields(movieCrew,
                    "person", "movie");
            Assert.assertEquals(movieCrew.getMovie().getId(), moviePartAfterUpdate.getMovie().getId());
            Assert.assertEquals(movieCrew.getPerson().getId(), moviePartAfterUpdate.getPerson().getId());
        });
    }

    @Test
    public void testUpdateMovieCrew() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        MovieCrewPutDTO updateDTO = new MovieCrewPutDTO();
        updateDTO.setDescription("New text");
        updateDTO.setPersonId(person.getId());
        updateDTO.setMovieCrewType(MovieCrewType.PRODUCER);

        MovieCrewReadDTO readDTO = movieCrewService.updateMovieCrew(movie.getId(), movieCrew.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        movieCrew = movieCrewRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(movieCrew,
                "movieId", "personId");
        Assert.assertEquals(movieCrew.getMovie().getId(), readDTO.getMovieId());
        Assert.assertEquals(movieCrew.getPerson().getId(), readDTO.getPersonId());
    }

    @Test
    public void testDeleteMovieCrew() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        movieCrewService.deleteMovieCrew(movie.getId(), movieCrew.getId());

        Assert.assertFalse(movieCrewRepository.existsById(movieCrew.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteMovieCrewNotFound() {
        movieCrewService.deleteMovieCrew(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetMovieCrewWrongId() {
        movieCrewService.getMovieCrew(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testDeleteMovieCrewWithCompositeItems() {
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        ApplicationUser author = testObjectFactory.createUser();
        Comment c1 = testObjectFactory.createComment(author, movieCrew.getId(), CommentStatus.APPROVED, MOVIE_CREW);
        Comment c2 = testObjectFactory.createComment(author, movieCrew.getId(), CommentStatus.APPROVED, MOVIE_CREW);

        Rating r1 = testObjectFactory.createRating(5, author, movieCrew.getId(), MOVIE_CREW);

        movieCrewService.deleteMovieCrew(movie.getId(), movieCrew.getId());

        Assert.assertTrue(movieRepository.existsById(movie.getId()));
        Assert.assertFalse(movieCrewRepository.existsById(movieCrew.getId()));

        Assert.assertFalse(commentRepository.existsById(c1.getId()));
        Assert.assertFalse(commentRepository.existsById(c2.getId()));

        Assert.assertFalse(ratingRepository.existsById(r1.getId()));
    }

    @Test
    public void testCalcAverageRatingOfMovieCrew() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Person person = testObjectFactory.createPerson();

        MovieCrew movieCrew = testObjectFactory.createMovieCrew(person, movie);

        testObjectFactory.createRating(3, u1, movieCrew.getId(), MOVIE_CREW);
        testObjectFactory.createRating(6, u2, movieCrew.getId(), MOVIE_CREW);

        movieCrewService.updateAverageRatingOfMovieCrew(movieCrew.getId());

        movieCrew = movieCrewRepository.findById(movieCrew.getId()).get();
        Assert.assertEquals(4.5, movieCrew.getAverageRating(), Double.MIN_NORMAL);
    }


    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieCrewNotNullException() {
        MovieCrew mc = new MovieCrew();
        movieCrewRepository.save(mc);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieCrewMaxSizeValidation() {
        Movie movie = testObjectFactory.createMovie();

        MovieCrew mc = new MovieCrew();
        mc.setDescription("Long long text".repeat(1000));
        mc.setMovieCrewType(MovieCrewType.DIRECTOR);
        mc.setMovie(movie);
        movieCrewRepository.save(mc);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieCrewMinSizeValidation() {
        Movie movie = testObjectFactory.createMovie();

        MovieCrew mc = new MovieCrew();
        mc.setDescription("");
        mc.setMovieCrewType(MovieCrewType.DIRECTOR);
        mc.setMovie(movie);
        movieCrewRepository.save(mc);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieCrewMinRatingValidation() {
        Movie movie = testObjectFactory.createMovie();

        MovieCrew mc = new MovieCrew();
        mc.setDescription("text");
        mc.setMovieCrewType(MovieCrewType.DIRECTOR);
        mc.setMovie(movie);
        mc.setAverageRating(-0.01);
        movieCrewRepository.save(mc);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveMovieCrewMaxRatingValidation() {
        Movie movie = testObjectFactory.createMovie();

        MovieCrew mc = new MovieCrew();
        mc.setDescription("text");
        mc.setMovieCrewType(MovieCrewType.DIRECTOR);
        mc.setMovie(movie);
        mc.setAverageRating(10.01);
        movieCrewRepository.save(mc);
    }
}
