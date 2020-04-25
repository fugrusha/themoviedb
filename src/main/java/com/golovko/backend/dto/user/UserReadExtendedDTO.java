package com.golovko.backend.dto.user;

import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.dto.userrole.UserRoleReadDTO;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class UserReadExtendedDTO {

    private UUID id;

    private String username;

    private String email;

    private Boolean isBlocked;

    private Double trustLevel;

    private List<UserRoleReadDTO> userRoles;

    private List<ArticleReadDTO> articles;

    private List<LikeReadDTO> likes;

    private List<CommentReadDTO> comments;

    private Instant createdAt;

    private Instant updatedAt;
}
