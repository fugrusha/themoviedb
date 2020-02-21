package com.golovko.backend.repository;

import com.golovko.backend.domain.Comment;
import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.ParentType;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends CrudRepository<Comment, UUID> {

    @Query("select c from Comment c where c.id = :commentId" +
            " and c.parentId = :parentId" +
            " and c.parentType = :parentType")
    Comment findByIdAndParentId(UUID commentId, UUID parentId, ParentType parentType);

    @Query("select c from Comment c where c.parentId = :parentId" +
            " and c.parentType = :parentType" +
            " order by c.createdAt asc")
    List<Comment> findByParentIdOrderByCreatedAtAsc(UUID parentId, ParentType parentType);

    @Query("select c from Comment c where c.parentId = :parentId" +
            " and c.status = :status" +
            " and c.parentType = :parentType" +
            " order by c.createdAt asc")
    List<Comment> findAllByStatusAndParent(UUID parentId, CommentStatus status, ParentType parentType);
}
