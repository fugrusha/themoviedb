package com.golovko.backend.repository;

import com.golovko.backend.domain.Comment;
import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.TargetObjectType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CommentRepository extends CrudRepository<Comment, UUID>, CommentRepositoryCustom {

    @Query("select c from Comment c where c.id = :commentId"
            + " and c.targetObjectId = :targetId")
    Comment findByIdAndTargetId(UUID commentId, UUID targetId);

    @Query("select c from Comment c where c.targetObjectId = :targetId"
            + " and c.status = :status")
    Page<Comment> findAllByStatusAndTarget(UUID targetId, CommentStatus status, Pageable pageable);

    @Modifying
    @Query("delete from Comment c where c.targetObjectId = :targetId"
            + " and c.targetObjectType = :targetType")
    void deleteCommentsByTargetObjectId(UUID targetId, TargetObjectType targetType);

    @Modifying
    @Query("update Comment c set c.likesCount=(coalesce(c.likesCount, 0) + 1)"
            + " where c.id = :commentId")
    void incrementLikesCountField(UUID commentId);

    @Modifying
    @Query("update Comment c set c.likesCount=(coalesce(c.likesCount, 0) - 1)"
            + " where c.id = :commentId")
    void decrementLikesCountField(UUID commentId);

    @Modifying
    @Query("update Comment c set c.dislikesCount=(coalesce(c.dislikesCount, 0) + 1)"
            + " where c.id = :commentId")
    void incrementDislikesCountField(UUID commentId);

    @Modifying
    @Query("update Comment c set c.dislikesCount=(coalesce(c.dislikesCount, 0) - 1)"
            + " where c.id = :commentId")
    void decrementDislikesCountField(UUID commentId);
}
