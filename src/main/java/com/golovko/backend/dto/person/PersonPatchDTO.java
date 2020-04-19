package com.golovko.backend.dto.person;

import com.golovko.backend.domain.Gender;
import lombok.Data;

import javax.validation.constraints.Past;
import javax.validation.constraints.Size;
import java.time.LocalDate;

@Data
public class PersonPatchDTO {

    @Size(min = 1, max = 128)
    private String firstName;

    @Size(min = 1, max = 128)
    private String lastName;

    @Size(min = 1, max = 1000)
    private String bio;

    private Gender gender;

    @Past
    private LocalDate birthday;

    @Size(min = 1, max = 100)
    private String placeOfBirth;

    @Size(min = 1, max = 100)
    private String imageUrl;
}
