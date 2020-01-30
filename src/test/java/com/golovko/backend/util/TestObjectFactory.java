package com.golovko.backend.util;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.movieparticipation.MoviePartReadDTO;
import com.golovko.backend.dto.movieparticipation.MoviePartReadExtendedDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Component
public class TestObjectFactory {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @Autowired
    private ArticleRepository articleRepository;

    public Movie createMovie() {
        Movie movie = new Movie();
        movie.setMovieTitle("Title of the Movie");
        movie.setDescription("movie description");
        movie.setIsReleased(false);
        movie.setReleaseDate(LocalDate.parse("1990-05-14"));
        movie.setAverageRating(5.0);
        movie = movieRepository.save(movie);
        return movie;
    }

    public Person createPerson() {
        Person person = new Person();
        person.setFirstName("Anna");
        person.setLastName("Popova");
        person.setGender(Gender.FEMALE);
        person = personRepository.save(person);
        return person;
    }

    public ApplicationUser createUser() {
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setUsername("Vitalka");
        applicationUser.setPassword("123456");
        applicationUser.setEmail("vetal@gmail.com");
        applicationUser = applicationUserRepository.save(applicationUser);
        return applicationUser;
    }

    public Complaint createComplaint(ApplicationUser user) {
        Complaint complaint = new Complaint();
        complaint.setComplaintTitle("Some title");
        complaint.setComplaintText("Some report text");
        complaint.setComplaintType(ComplaintType.SPOILER);
        complaint.setAuthor(user);
        complaint = complaintRepository.save(complaint);
        return complaint;
    }

    public PersonReadDTO createPersonReadDTO() {
        PersonReadDTO dto = new PersonReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setFirstName("Max");
        dto.setLastName("Popov");
        dto.setGender(Gender.MALE);
        return dto;
    }

    public MovieReadDTO createMovieReadDTO() {
        MovieReadDTO readDTO = new MovieReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setMovieTitle("Guess Who");
        readDTO.setDescription("12345");
        readDTO.setReleaseDate(LocalDate.parse("1990-12-05"));
        readDTO.setReleased(false);
        readDTO.setAverageRating(8.3);
        return readDTO;
    }

    public MoviePartReadDTO createMoviePartReadDTO() {
        MoviePartReadDTO dto = new MoviePartReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setPartInfo("Some text");
        dto.setAverageRating(9.2);
        dto.setPersonId(UUID.randomUUID());
        dto.setMovieId(UUID.randomUUID());
        dto.setPartTypes(Set.of(PartType.WRITER, PartType.COSTUME_DESIGNER));
        return dto;
    }

    public MoviePartReadExtendedDTO createMoviePartReadExtendedDTO(PersonReadDTO personDTO, MovieReadDTO movieDTO) {
        MoviePartReadExtendedDTO dto = new MoviePartReadExtendedDTO();
        dto.setId(UUID.randomUUID());
        dto.setPartInfo("Some text");
        dto.setAverageRating(9.2);
        dto.setPerson(personDTO);
        dto.setMovie(movieDTO);
        dto.setPartTypes(Set.of(PartType.WRITER, PartType.COSTUME_DESIGNER));
        return dto;
    }

    public Article createArticle(ApplicationUser author, Instant time) {
        Article article = new Article();
        article.setTitle("Some title");
        article.setText("Some text");
        article.setDislikesCount(444);
        article.setLikesCount(111);
        article.setAuthor(author);
        article.setPublishedDate(time);
        return articleRepository.save(article);
    }
}
