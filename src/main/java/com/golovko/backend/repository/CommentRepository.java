package com.golovko.backend.repository;

import com.golovko.backend.domain.Comment;
import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.TargetObjectType;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends CrudRepository<Comment, UUID>, CommentRepositoryCustom {

    @Query("select c from Comment c where c.id = :commentId"
            + " and c.targetObjectId = :targetId")
    Comment findByIdAndTargetId(UUID commentId, UUID targetId);

    @Query("select c from Comment c where c.targetObjectId = :targetId"
            + " and c.status = :status"
            + " order by c.createdAt asc")
    List<Comment> findAllByStatusAndTarget(UUID targetId, CommentStatus status);

    @Modifying
    @Query("delete from Comment c where c.targetObjectId = :targetId"
            + " and c.targetObjectType = :targetType")
    void deleteCommentsByTargetObjectId(UUID targetId, TargetObjectType targetType);
}
