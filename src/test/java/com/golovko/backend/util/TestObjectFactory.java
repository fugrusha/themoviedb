package com.golovko.backend.util;

import com.golovko.backend.domain.*;
import com.golovko.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;

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
    private MovieCastRepository movieCastRepository;

    @Autowired
    private MovieParticipationRepository movieParticipationRepository;

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

    public Complaint createComplaint(ApplicationUser user, ComplaintType complaintType) {
        Complaint complaint = new Complaint();
        complaint.setComplaintTitle("Some title");
        complaint.setComplaintText("Some report text");
        complaint.setComplaintType(complaintType);
        complaint.setAuthor(user);
        complaint = complaintRepository.save(complaint);
        return complaint;
    }

    public MovieCast createMovieCast(Person person, Movie movie) {
        MovieCast movieCast = new MovieCast();
        movieCast.setPartInfo("Some text");
        movieCast.setAverageRating(5.0);
        movieCast.setPerson(person);
        movieCast.setMovie(movie);
        movieCast.setPartType(PartType.CAST);
        movieCast.setCharacter("Leon");

        movieCast = movieCastRepository.save(movieCast);
        return movieCast;
    }

    public MovieParticipation createMovieParticipation(Person person, Movie movie) {
        MovieParticipation movieParticipation = new MovieParticipation();
        movieParticipation.setPartInfo("Some text");
        movieParticipation.setAverageRating(5.0);
        movieParticipation.setPerson(person);
        movieParticipation.setMovie(movie);
        movieParticipation.setPartType(PartType.WRITER);

        movieParticipation = movieParticipationRepository.save(movieParticipation);
        return movieParticipation;
    }

    public MovieParticipation createMovieParticipationForFilter(Person person, Movie movie, PartType partType) {
        MovieParticipation movieParticipation = new MovieParticipation();
        movieParticipation.setPartInfo("Some text");
        movieParticipation.setAverageRating(5.0);
        movieParticipation.setPerson(person);
        movieParticipation.setMovie(movie);
        movieParticipation.setPartType(partType);

        movieParticipation = movieParticipationRepository.save(movieParticipation);
        return movieParticipation;
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
