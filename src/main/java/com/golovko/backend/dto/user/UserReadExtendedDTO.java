package com.golovko.backend.dto.user;

import com.golovko.backend.domain.Gender;
import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.dto.userrole.UserRoleReadDTO;
import com.golovko.backend.dto.watchlist.WatchlistReadDTO;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class UserReadExtendedDTO {

    private UUID id;

    private String username;

    private String email;

    private Gender gender;

    private Boolean isBlocked;

    private Double trustLevel;

    private List<UserRoleReadDTO> userRoles;

    private List<ArticleReadDTO> articles;

    private List<LikeReadDTO> likes;

    private List<CommentReadDTO> comments;

    private List<UserReadDTO> topMatches;

    private List<WatchlistReadDTO> watchlists;

    private Instant createdAt;

    private Instant updatedAt;
}
