package com.golovko.backend.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class UserInLeaderBoardDTO {

    private UUID id;

    private String username;

    private Double trustLevel;

    private Long commentsCount;

    private Long ratedMoviesCount;
}
