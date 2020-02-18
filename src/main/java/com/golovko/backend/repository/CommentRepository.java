package com.golovko.backend.repository;

import com.golovko.backend.domain.Comment;
import com.golovko.backend.domain.CommentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CommentRepository extends CrudRepository<Comment, UUID> {

    Comment findByIdAndParentId(UUID commentId, UUID parentId);

    List<Comment> findByParentIdOrderByCreatedAtAsc(UUID parentId);

    @Query("select c from Comment c where c.parentId = :parentId and" +
            " c.status = :status " +
            " order by c.createdAt asc")
    List<Comment> findAllByStatusAndByParentId(UUID parentId, CommentStatus status);
}
