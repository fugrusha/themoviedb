package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.Person;
import com.golovko.backend.dto.*;
import org.springframework.stereotype.Service;

@Service
public class TranslationService {

    /*
        ApplicationUser translations
    */
    public UserReadDTO toRead(ApplicationUser user) {
        UserReadDTO dto = new UserReadDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setPassword(user.getPassword());
        dto.setEmail(user.getEmail());
        return dto;
    }

    public UserReadExtendedDTO toReadExtended(ApplicationUser user) {
        UserReadExtendedDTO dto = new UserReadExtendedDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setComplaint(toRead(user.getComplaint()));
        return dto;
    }

    public ApplicationUser toEntity(UserCreateDTO createDTO) {
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setUsername(createDTO.getUsername());
        applicationUser.setEmail(createDTO.getEmail());
        applicationUser.setPassword(createDTO.getPassword());
        return applicationUser;
    }

    public void patchEntity(UserPatchDTO patchDTO, ApplicationUser applicationUser) {
        if (patchDTO.getUsername() != null) {
            applicationUser.setUsername(patchDTO.getUsername()); // username cannot be editable
        }
        if (patchDTO.getEmail() != null){
            applicationUser.setEmail(patchDTO.getEmail());
        }
        if (patchDTO.getPassword() != null){
            applicationUser.setPassword(patchDTO.getPassword());
        }
    }


    /*
        Movie translations
    */
    public MovieReadDTO toRead(Movie movie) {
        MovieReadDTO dto = new MovieReadDTO();
        dto.setId(movie.getId());
        dto.setMovieTitle(movie.getMovieTitle());
        dto.setDescription(movie.getDescription());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setReleased(movie.getIsReleased());
        dto.setAverageRating(movie.getAverageRating());
        return dto;
    }

    public Movie toEntity(MovieCreateDTO createDTO) {
        Movie movie = new Movie();
        movie.setMovieTitle(createDTO.getMovieTitle());
        movie.setDescription(createDTO.getDescription());
        movie.setReleaseDate(createDTO.getReleaseDate());
        movie.setIsReleased(createDTO.isReleased());
        movie.setAverageRating(createDTO.getAverageRating());
        return movie;
    }

    public void patchEntity(MoviePatchDTO patchDTO, Movie movie) {
        if (patchDTO.getMovieTitle() != null){
            movie.setMovieTitle(patchDTO.getMovieTitle());
        }
        if (patchDTO.getDescription() != null) {
            movie.setDescription(patchDTO.getDescription());
        }
        if (patchDTO.getAverageRating() != null){
            movie.setAverageRating(patchDTO.getAverageRating());
        }
        if (patchDTO.getReleaseDate() != null){
            movie.setReleaseDate(patchDTO.getReleaseDate());
        }
        if (patchDTO.getIsReleased() != null){
            movie.setIsReleased(patchDTO.getIsReleased());
        }
    }

    /*
        Complaint translations
    */
    public ComplaintReadDTO toRead(Complaint complaint) {
        ComplaintReadDTO dto = new ComplaintReadDTO();
        dto.setId(complaint.getId());
        dto.setComplaintTitle(complaint.getComplaintTitle());
        dto.setComplaintText(complaint.getComplaintText());
        dto.setComplaintType(complaint.getComplaintType());
        return dto;
    }

    public Complaint toEntity(ComplaintCreateDTO createDTO) {
        Complaint complaint = new Complaint();
        complaint.setComplaintTitle(createDTO.getComplaintTitle());
        complaint.setComplaintText(createDTO.getComplaintText());
        complaint.setComplaintType(createDTO.getComplaintType());
        return complaint;
    }

    public void patchEntity(ComplaintPatchDTO patchDTO, Complaint complaint) {
        if (patchDTO.getComplaintTitle() != null) {
            complaint.setComplaintTitle(patchDTO.getComplaintTitle());
        }
        if (patchDTO.getComplaintText() != null) {
            complaint.setComplaintText(patchDTO.getComplaintText());
        }
        if (patchDTO.getComplaintType() != null) {
            complaint.setComplaintType(patchDTO.getComplaintType());
        }
    }

    /*
        Person translations
    */
    public PersonReadDTO toRead(Person person) {
        PersonReadDTO dto = new PersonReadDTO();
        dto.setId(person.getId());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setGender(person.getGender());
        return dto;
    }

    public Person toEntity(PersonCreateDTO createDTO) {
        Person person = new Person();
        person.setFirstName(createDTO.getFirstName());
        person.setLastName(createDTO.getLastName());
        person.setGender(createDTO.getGender());
        return person;
    }

    public void patchEntity(PersonPatchDTO patchDTO, Person person) {
        if (patchDTO.getFirstName() != null) {
            person.setFirstName(patchDTO.getFirstName());
        }
        if (patchDTO.getLastName() != null) {
            person.setLastName(patchDTO.getLastName());
        }
        if (patchDTO.getGender() != null) {
            person.setGender(patchDTO.getGender());
        }
    }
}
