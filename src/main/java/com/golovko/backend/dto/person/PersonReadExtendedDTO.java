package com.golovko.backend.dto.person;

import com.golovko.backend.domain.Gender;
import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewReadDTO;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class PersonReadExtendedDTO {

    private UUID id;

    private String firstName;

    private String lastName;

    private String bio;

    private LocalDate birthday;

    private String placeOfBirth;

    private String imageUrl;

    private Gender gender;

    private Double averageRatingByRoles;

    private Double averageRatingByMovies;

    private List<MovieCrewReadDTO> movieCrews;

    private List<MovieCastReadDTO> movieCasts;

    private List<ArticleReadDTO> articles;

    private Instant createdAt;

    private Instant updatedAt;
}
