package com.golovko.backend.repository;

import com.golovko.backend.domain.Like;
import com.golovko.backend.domain.TargetObjectType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LikeRepository extends CrudRepository<Like, UUID> {

    @Query("select l from Like l where l.id = :likeId"
            + " and l.author.id = :userId")
    Like findByIdAndUserId(UUID likeId, UUID userId);

    @Query("select l from Like l where l.author.id = :userId"
            + " and l.likedObjectId = :likedObjectId")
    Like findByUserIdAndLikedObjectId(UUID userId, UUID likedObjectId);

    @Modifying
    @Query("delete from Like l where l.likedObjectId = :targetId"
            + " and l.likedObjectType = :targetType")
    void deleteLikesByTargetObjectId(UUID targetId, TargetObjectType targetType);
}
