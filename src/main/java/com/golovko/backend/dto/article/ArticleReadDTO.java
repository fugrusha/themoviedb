package com.golovko.backend.dto.article;

import com.golovko.backend.domain.ArticleStatus;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ArticleReadDTO {

    private UUID id;

    private String title;

    private Instant publishedDate;

    private String text;

    private ArticleStatus status;

    private UUID authorId;

//    private List<CommentReadDTO> comments;

//    private List<UUID> relatedPeopleId;
//
//    private List<UUID> relatedMoviesId;

    private Integer likesCount;

    private Integer dislikesCount;
}
