package com.golovko.backend.dto.article;

import com.golovko.backend.domain.ArticleStatus;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ArticleCreateDTO {

    private String title;

    private String text;

    private ArticleStatus status;

    private List<UUID> relatedPeopleId;

    private List<UUID> relatedMoviesId;
}
