package com.golovko.backend.repository;

import com.golovko.backend.domain.Comment;
import com.golovko.backend.dto.comment.CommentFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {

    Page<Comment> findByFilter(CommentFilter filter, Pageable pageable);
}
