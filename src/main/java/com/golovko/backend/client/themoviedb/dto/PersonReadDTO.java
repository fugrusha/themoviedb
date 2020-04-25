package com.golovko.backend.client.themoviedb.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateDeserializer;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PersonReadDTO {

    private String id;

    private String name;

    private Integer gender;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate birthday;

    private String biography;

    private String placeOfBirth;
}
