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
            + " and r.targetObjectId = :targetId")
    Rating findByIdAndTargetId(UUID ratingId, UUID targetId);

    @Query("select r from Rating r where r.targetObjectId = :targetId")
    List<Rating> findAllByTargetId(UUID targetId);

    @Query("select avg(r.rating) from Rating r where r.targetObjectId = :targetObjectId")
    Double calcAverageRating(UUID targetObjectId);
}
