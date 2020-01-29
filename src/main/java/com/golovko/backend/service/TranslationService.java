package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.movie.MovieCreateDTO;
import com.golovko.backend.dto.movie.MoviePatchDTO;
import com.golovko.backend.dto.movie.MoviePutDTO;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.moviecast.MovieCastCreateDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadExtendedDTO;
import com.golovko.backend.dto.movieparticipation.MoviePartCreateDTO;
import com.golovko.backend.dto.movieparticipation.MoviePartReadDTO;
import com.golovko.backend.dto.movieparticipation.MoviePartReadExtendedDTO;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonPutDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.dto.user.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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

        List<ComplaintReadDTO> asDTO = user.getComplaints()
                .stream().map(this::toRead).collect(Collectors.toList());
        dto.setComplaints(asDTO);
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

    public void updateEntity(UserPutDTO update, ApplicationUser user) {
        user.setUsername(update.getUsername());
        user.setEmail(update.getEmail());
        user.setPassword(update.getPassword());
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
        return movie;
    }

    public void patchEntity(MoviePatchDTO patchDTO, Movie movie) {
        if (patchDTO.getMovieTitle() != null){
            movie.setMovieTitle(patchDTO.getMovieTitle());
        }
        if (patchDTO.getDescription() != null) {
            movie.setDescription(patchDTO.getDescription());
        }
        if (patchDTO.getReleaseDate() != null){
            movie.setReleaseDate(patchDTO.getReleaseDate());
        }
        if (patchDTO.getIsReleased() != null){
            movie.setIsReleased(patchDTO.getIsReleased());
        }
    }

    public void updateEntity(MoviePutDTO updateDTO, Movie movie) {
        movie.setMovieTitle(updateDTO.getMovieTitle());
        movie.setReleaseDate(updateDTO.getReleaseDate());
        movie.setDescription(updateDTO.getDescription());
        movie.setIsReleased(updateDTO.getIsReleased());
        movie.setAverageRating(updateDTO.getAverageRating());
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
        dto.setAuthorId(complaint.getAuthor().getId());
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

    public void updateEntity(ComplaintPutDTO updateDTO, Complaint complaint) {
        complaint.setComplaintTitle(updateDTO.getComplaintTitle());
        complaint.setComplaintText(updateDTO.getComplaintText());
        complaint.setComplaintType(updateDTO.getComplaintType());
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

    public void updateEntity(PersonPutDTO updateDTO, Person person) {
        person.setFirstName(updateDTO.getFirstName());
        person.setLastName(updateDTO.getLastName());
        person.setGender(updateDTO.getGender());
    }

    /*
        MovieParticipation translations
    */
    public MoviePartReadDTO toRead(MovieParticipation movieParticipation) {
        MoviePartReadDTO dto = new MoviePartReadDTO();
        dto.setId(movieParticipation.getId());
        dto.setPartInfo(movieParticipation.getPartInfo());
        dto.setAverageRating(movieParticipation.getAverageRating());
        dto.setPartTypes(movieParticipation.getPartTypes());
        dto.setMovieId(movieParticipation.getMovie().getId());
        dto.setPersonId(movieParticipation.getPerson().getId());
        return dto;
    }

    public MoviePartReadExtendedDTO toReadExtended(MovieParticipation movieParticipation) {
        MoviePartReadExtendedDTO dto = new MoviePartReadExtendedDTO();
        dto.setId(movieParticipation.getId());
        dto.setPartInfo(movieParticipation.getPartInfo());
        dto.setPartTypes(movieParticipation.getPartTypes());
        dto.setAverageRating(movieParticipation.getAverageRating());
        dto.setMovie(toRead(movieParticipation.getMovie()));
        dto.setPerson(toRead(movieParticipation.getPerson()));
        return dto;
    }

    public MovieParticipation toEntity(MoviePartCreateDTO createDTO) {
        MovieParticipation movieParticipation = new MovieParticipation();
        movieParticipation.setPartInfo(createDTO.getPartInfo());
        movieParticipation.setPartTypes(createDTO.getPartTypes());
        return movieParticipation;
    }

    /*
        MovieCast translations
    */
    public MovieCastReadDTO toRead(MovieCast movieCast) {
        MovieCastReadDTO dto = new MovieCastReadDTO();
        dto.setId(movieCast.getId());
        dto.setPartInfo(movieCast.getPartInfo());
        dto.setAverageRating(movieCast.getAverageRating());
        dto.setPartType(movieCast.getPartType());
        dto.setMovieId(movieCast.getMovie().getId());
        dto.setPersonId(movieCast.getPerson().getId());
        dto.setCharacter(movieCast.getCharacter());
        return dto;
    }

    public MovieCast toEntity(MovieCastCreateDTO createDTO) {
        MovieCast movieCast = new MovieCast();
        movieCast.setPartInfo(createDTO.getPartInfo());
        movieCast.setCharacter(createDTO.getCharacter());
        return movieCast;
    }

    public MovieCastReadExtendedDTO toReadExtended(MovieCast movieCast) {
        MovieCastReadExtendedDTO dto = new MovieCastReadExtendedDTO();
        dto.setId(movieCast.getId());
        dto.setPartInfo(movieCast.getPartInfo());
        dto.setPartType(movieCast.getPartType());
        dto.setAverageRating(movieCast.getAverageRating());
        dto.setMovie(toRead(movieCast.getMovie()));
        dto.setPerson(toRead(movieCast.getPerson()));
        return dto;
    }
}
