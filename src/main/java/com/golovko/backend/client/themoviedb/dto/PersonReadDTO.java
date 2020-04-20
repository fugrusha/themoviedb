package com.golovko.backend.client.themoviedb.dto;

import lombok.Data;

@Data
public class PersonReadDTO {

    private String id;

    private String name;

    private Integer gender;

    private String birthday;

    private String biography;

    private String placeOfBirth;
}
