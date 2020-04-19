package com.golovko.backend.dto.person;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PersonTopRatedDTO {

    private UUID id;

    private String firstName;

    private String lastName;

    private Double averageRatingByRoles;

    private Long rolesCount;
}
