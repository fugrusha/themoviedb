package com.golovko.backend.util;

import com.golovko.backend.domain.*;
import com.golovko.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
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
    private MovieCastRepository movieCastRepository;

    @Autowired
    private MovieParticipationRepository movieParticipationRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    public Movie createMovie() {
        Movie movie = new Movie();
        movie.setMovieTitle("Title of the Movie");
        movie.setDescription("movie description");
        movie.setIsReleased(false);
        movie.setReleaseDate(LocalDate.parse("1990-05-14"));
        movie.setAverageRating(5.0);
        return movieRepository.save(movie);
    }

    public Person createPerson() {
        Person person = new Person();
        person.setFirstName("Anna");
        person.setLastName("Popova");
        person.setGender(Gender.FEMALE);
        return personRepository.save(person);
    }

    public ApplicationUser createUser() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("Vitalka");
        user.setPassword("123456");
        user.setEmail("vetal@gmail.com");
        user.setIsBlocked(false);
        return applicationUserRepository.save(user);
    }

    public Complaint createComplaint(
            ApplicationUser user,
            ComplaintType complaintType,
            TargetObjectType targetObjectType
    ) {
        Complaint complaint = new Complaint();
        complaint.setComplaintTitle("Some title");
        complaint.setComplaintText("Some report text");
        complaint.setComplaintType(complaintType);
        complaint.setComplaintStatus(ComplaintStatus.INITIATED);
        complaint.setAuthor(user);
        complaint.setTargetObjectType(targetObjectType);
        complaint.setTargetObjectId(UUID.randomUUID());
        return complaintRepository.save(complaint);
    }

    public Complaint createComplaint(
            UUID articleId,
            TargetObjectType targetObjectType,
            ApplicationUser author,
            ApplicationUser moderator
    ) {
        Complaint complaint = new Complaint();
        complaint.setComplaintTitle("Some title");
        complaint.setComplaintText("Some report text");
        complaint.setComplaintType(ComplaintType.MISPRINT);
        complaint.setComplaintStatus(ComplaintStatus.INITIATED);
        complaint.setAuthor(author);
        complaint.setModerator(moderator);
        complaint.setTargetObjectId(articleId);
        complaint.setTargetObjectType(targetObjectType);
        return complaintRepository.save(complaint);
    }

    public MovieCast createMovieCast(Person person, Movie movie) {
        MovieCast movieCast = new MovieCast();
        movieCast.setPartInfo("Some text");
        movieCast.setAverageRating(5.0);
        movieCast.setPerson(person);
        movieCast.setMovie(movie);
        movieCast.setPartType(PartType.CAST);
        movieCast.setCharacter("Leon");
        return movieCastRepository.save(movieCast);
    }

    public MovieParticipation createMovieParticipation(Person person, Movie movie) {
        MovieParticipation movieParticipation = new MovieParticipation();
        movieParticipation.setPartInfo("Some text");
        movieParticipation.setAverageRating(5.0);
        movieParticipation.setPerson(person);
        movieParticipation.setMovie(movie);
        movieParticipation.setPartType(PartType.WRITER);
        return movieParticipationRepository.save(movieParticipation);
    }

    public MovieParticipation createMovieParticipationForFilter(Person person, Movie movie, PartType partType) {
        MovieParticipation movieParticipation = new MovieParticipation();
        movieParticipation.setPartInfo("Some text");
        movieParticipation.setAverageRating(5.0);
        movieParticipation.setPerson(person);
        movieParticipation.setMovie(movie);
        movieParticipation.setPartType(partType);
        return movieParticipationRepository.save(movieParticipation);
    }

    public Article createArticle(ApplicationUser author, ArticleStatus status) {
        Article article = new Article();
        article.setTitle("Some title");
        article.setText("Some text");
        article.setStatus(status);
        article.setDislikesCount(444);
        article.setLikesCount(111);
        article.setAuthor(author);
        return articleRepository.save(article);
    }

    public Comment createComment(
            ApplicationUser author,
            UUID parentId,
            CommentStatus status,
            TargetObjectType targetObjectType) {
        Comment comment = new Comment();
        comment.setMessage("text");
        comment.setStatus(status);
        comment.setLikesCount(45);
        comment.setDislikesCount(78);
        comment.setAuthor(author);
        comment.setTargetObjectType(targetObjectType);
        comment.setTargetObjectId(parentId);
        return commentRepository.save(comment);
    }
}
