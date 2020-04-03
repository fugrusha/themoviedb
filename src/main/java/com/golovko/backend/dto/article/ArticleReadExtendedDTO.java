package com.golovko.backend.dto.article;

import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
public class ArticleReadExtendedDTO {

    private UUID id;

    private String title;

    private String text;

    private ArticleStatus status;

    private UserReadDTO author;

    private List<PersonReadDTO> people;

    private List<MovieReadDTO> movies;

    private Integer likesCount;

    private Integer dislikesCount;

    private Instant createdAt;

    private Instant updatedAt;
}
