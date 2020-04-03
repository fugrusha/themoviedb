package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Rating;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.RatingRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionSystemException;

import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.MOVIE;

public class RatingServiceTest extends BaseTest {

    @Autowired
    private RatingService ratingService;

    @Autowired
    private RatingRepository ratingRepository;

    @Test
    public void testGetRatingById() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Rating rating = testObjectFactory.createRating(4, user, movie.getId(), MOVIE);

        RatingReadDTO readDTO = ratingService.getRating(movie.getId(), rating.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(rating, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), rating.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetRatingWrongId() {
        ratingService.getRating(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test
    public void testGetAllRatings() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Movie movie1 = testObjectFactory.createMovie();
        Movie movie2 = testObjectFactory.createMovie();
        Rating r1 = testObjectFactory.createRating(2, user1, movie1.getId(), MOVIE);
        Rating r2 = testObjectFactory.createRating(3, user2, movie1.getId(), MOVIE);
        testObjectFactory.createRating(4, user1, movie2.getId(), MOVIE);
        testObjectFactory.createRating(5, user2, movie2.getId(), MOVIE);

        PageResult<RatingReadDTO> ratings = ratingService
                .getRatingsByTargetObjectId(movie1.getId(), Pageable.unpaged());

        Assertions.assertThat(ratings.getData()).extracting("id")
                .containsExactlyInAnyOrder(r1.getId(), r2.getId());
    }

    @Test
    public void testGetRatingsWithPagingAndSorting() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Rating r1 = testObjectFactory.createRating(2, user, movie.getId(), MOVIE);
        Rating r2 = testObjectFactory.createRating(3, user, movie.getId(), MOVIE);
        testObjectFactory.createRating(4, user, movie.getId(), MOVIE);
        testObjectFactory.createRating(5, user, movie.getId(), MOVIE);

        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.ASC, "rating"));
        PageResult<RatingReadDTO> ratings = ratingService
                .getRatingsByTargetObjectId(movie.getId(), pageRequest);

        Assertions.assertThat(ratings.getData()).extracting("id")
                .containsExactlyInAnyOrder(r1.getId(), r2.getId());
    }

    @Test
    public void testCreateRating() {
        ApplicationUser author = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(6);
        createDTO.setAuthorId(author.getId());
        createDTO.setRatedObjectType(TargetObjectType.MOVIE);

        RatingReadDTO readDTO = ratingService.createRating(movie.getId(), createDTO);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Rating rating = ratingRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(rating, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), rating.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateRatingWrongAuthor() {
        Movie movie = testObjectFactory.createMovie();

        RatingCreateDTO createDTO = new RatingCreateDTO();
        createDTO.setRating(6);
        createDTO.setAuthorId(UUID.randomUUID());
        createDTO.setRatedObjectType(TargetObjectType.MOVIE);

        ratingService.createRating(movie.getId(), createDTO);
    }

    @Test
    public void testPatchRating() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Rating rating = testObjectFactory.createRating(4, user, movie.getId(), MOVIE);

        RatingPatchDTO patchDTO = new RatingPatchDTO();
        patchDTO.setRating(8);

        RatingReadDTO readDTO = ratingService.patchRating(movie.getId(), rating.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        Rating ratingAfterUpdate = ratingRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(ratingAfterUpdate).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(ratingAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testPatchRatingEmptyPatch() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Rating rating = testObjectFactory.createRating(4, user, movie.getId(), MOVIE);

        RatingPatchDTO patchDTO = new RatingPatchDTO();

        RatingReadDTO readDTO = ratingService.patchRating(movie.getId(), rating.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        Rating ratingAfterUpdate = ratingRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(ratingAfterUpdate).hasNoNullFieldsOrProperties();
        Assertions.assertThat(ratingAfterUpdate).isEqualToIgnoringGivenFields(rating, "author");
        Assert.assertEquals(ratingAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testUpdateRating() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Rating rating = testObjectFactory.createRating(4, user, movie.getId(), MOVIE);

        RatingPutDTO putDTO = new RatingPutDTO();
        putDTO.setRating(10);

        RatingReadDTO readDTO = ratingService.updateRating(movie.getId(), rating.getId(), putDTO);

        Assertions.assertThat(putDTO).isEqualToComparingFieldByField(readDTO);

        Rating ratingAfterUpdate = ratingRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(ratingAfterUpdate).isEqualToIgnoringGivenFields(readDTO, "author");
        Assert.assertEquals(ratingAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testDeleteRating() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();
        Rating rating = testObjectFactory.createRating(4, user, movie.getId(), MOVIE);

        ratingService.deleteRating(movie.getId(), rating.getId());

        Assert.assertFalse(ratingRepository.existsById(rating.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteRatingNotFound() {
        ratingService.deleteRating(UUID.randomUUID(), UUID.randomUUID());
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveRatingNotNullValidation() {
        Rating rating = new Rating();
        ratingRepository.save(rating);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveRatingMinRatingValidation() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Rating rating = new Rating();
        rating.setRating(0);
        rating.setRatedObjectId(movie.getId());
        rating.setRatedObjectType(MOVIE);
        rating.setAuthor(user);
        ratingRepository.save(rating);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveRatingMaxRatingValidation() {
        ApplicationUser user = testObjectFactory.createUser();
        Movie movie = testObjectFactory.createMovie();

        Rating rating = new Rating();
        rating.setRating(11);
        rating.setRatedObjectId(movie.getId());
        rating.setRatedObjectType(MOVIE);
        rating.setAuthor(user);
        ratingRepository.save(rating);
    }
}
