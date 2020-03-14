package com.golovko.backend.repository;

import com.golovko.backend.domain.Rating;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RatingRepository extends CrudRepository<Rating, UUID> {

    @Query("select r from Rating r where r.id = :ratingId"
            + " and r.ratedObjectId = :ratedObjectId")
    Rating findByIdAndTargetId(UUID ratingId, UUID ratedObjectId);

    @Query("select r from Rating r where r.ratedObjectId = :ratedObjectId")
    List<Rating> findAllByTargetId(UUID ratedObjectId);

    @Query("select avg(r.rating) from Rating r where r.ratedObjectId = :ratedObjectId")
    Double calcAverageRating(UUID ratedObjectId);
}
