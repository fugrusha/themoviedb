package com.golovko.backend.dto.article;

import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.dto.user.UserReadDTO;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ArticleReadExtendedDTO {

    private UUID id;

    private String title;

    private String text;

    private ArticleStatus status;

    private UserReadDTO author;

    private Integer likesCount;

    private Integer dislikesCount;

    private Instant createdAt;

    private Instant lastModifiedAt;
}
