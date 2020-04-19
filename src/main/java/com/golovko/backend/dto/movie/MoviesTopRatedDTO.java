package com.golovko.backend.dto.movie;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class MoviesTopRatedDTO {

    private UUID id;

    private String movieTitle;

    private Double averageRating;

    private Integer likesCount;

    private Integer dislikesCount;
}
