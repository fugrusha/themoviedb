package com.golovko.backend.repository;

import com.golovko.backend.domain.Rating;
import com.golovko.backend.domain.TargetObjectType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RatingRepository extends CrudRepository<Rating, UUID> {

    @Query("select r from Rating r where r.id = :ratingId"
            + " and r.ratedObjectId = :ratedObjectId")
    Rating findByIdAndTargetId(UUID ratingId, UUID ratedObjectId);

    @Query("select r from Rating r where r.ratedObjectId = :ratedObjectId")
    Page<Rating> findAllByTargetId(UUID ratedObjectId, Pageable pageable);

    @Query("select avg(r.rating) from Rating r where r.ratedObjectId = :ratedObjectId")
    Double calcAverageRating(UUID ratedObjectId);

    @Modifying
    @Query("delete from Rating r where r.ratedObjectId = :targetId"
            + " and r.ratedObjectType = :targetType")
    void deleteRatingsByRatedObjectId(UUID targetId, TargetObjectType targetType);
}
