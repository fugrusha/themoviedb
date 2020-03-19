package com.golovko.backend.repository;

import com.golovko.backend.domain.Comment;
import com.golovko.backend.dto.comment.CommentFilter;

import java.util.List;

public interface CommentRepositoryCustom {

    List<Comment> findByFilter(CommentFilter filter);
}
