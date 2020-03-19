package com.golovko.backend.util;

import com.golovko.backend.domain.*;
import com.golovko.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
    private MovieCastRepository movieCastRepository;

    @Autowired
    private MovieCrewRepository movieCrewRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private RatingRepository ratingRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private MisprintRepository misprintRepository;

    public Movie createMovie() {
        Movie movie = new Movie();
        movie.setMovieTitle("Title of the Movie");
        movie.setDescription("movie description");
        movie.setIsReleased(false);
        movie.setReleaseDate(LocalDate.parse("1990-05-14"));
        movie.setAverageRating(null);
        return movieRepository.save(movie);
    }

    public Person createPerson() {
        Person person = new Person();
        person.setFirstName("Anna");
        person.setLastName("Popova");
        person.setBio("some text");
        person.setAverageRatingByRoles(null);
        person.setGender(Gender.FEMALE);
        return personRepository.save(person);
    }

    public ApplicationUser createUser() {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("Vitalka");
        user.setPassword("123456");
        user.setEmail("vetal@gmail.com");
        user.setIsBlocked(false);
        user.setTrustLevel(7.0);
        user.setUserRole(Set.of(UserRole.USER));
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
            UUID targetObjectId,
            TargetObjectType targetObjectType,
            ApplicationUser author
    ) {
        Complaint complaint = new Complaint();
        complaint.setComplaintTitle("Some title");
        complaint.setComplaintText("Some report text");
        complaint.setComplaintType(ComplaintType.MISPRINT);
        complaint.setComplaintStatus(ComplaintStatus.INITIATED);
        complaint.setAuthor(author);
        complaint.setModerator(null);
        complaint.setTargetObjectId(targetObjectId);
        complaint.setTargetObjectType(targetObjectType);
        return complaintRepository.save(complaint);
    }

    public Misprint createMisprint(
            UUID targetObjectId,
            TargetObjectType targetObjectType,
            ApplicationUser author,
            String misprintText
    ) {
        Misprint misprint = new Misprint();
        misprint.setMisprintText(misprintText);
        misprint.setReplaceTo("Some report text");
        misprint.setStatus(ComplaintStatus.INITIATED);
        misprint.setAuthor(author);
        misprint.setModerator(null);
        misprint.setTargetObjectId(targetObjectId);
        misprint.setTargetObjectType(targetObjectType);
        return misprintRepository.save(misprint);
    }

    public MovieCast createMovieCast(Person person, Movie movie) {
        MovieCast movieCast = new MovieCast();
        movieCast.setDescription("Some text");
        movieCast.setAverageRating(null);
        movieCast.setPerson(person);
        movieCast.setMovie(movie);
        movieCast.setMovieCrewType(MovieCrewType.CAST);
        movieCast.setCharacter("Leon");
        return movieCastRepository.save(movieCast);
    }

    public MovieCast createMovieCast(Person person, Movie movie, Double rating) {
        MovieCast movieCast = new MovieCast();
        movieCast.setDescription("Some text");
        movieCast.setAverageRating(rating);
        movieCast.setPerson(person);
        movieCast.setMovie(movie);
        movieCast.setMovieCrewType(MovieCrewType.CAST);
        movieCast.setCharacter("Leon");
        return movieCastRepository.save(movieCast);
    }

    public MovieCrew createMovieCrew(Person person, Movie movie) {
        MovieCrew movieCrew = new MovieCrew();
        movieCrew.setDescription("Some text");
        movieCrew.setAverageRating(null);
        movieCrew.setPerson(person);
        movieCrew.setMovie(movie);
        movieCrew.setMovieCrewType(MovieCrewType.WRITER);
        return movieCrewRepository.save(movieCrew);
    }

    public MovieCrew createMovieCrewForFilter(
            Person person,
            Movie movie,
            MovieCrewType movieCrewType
    ) {
        MovieCrew movieCrew = new MovieCrew();
        movieCrew.setDescription("Some text");
        movieCrew.setAverageRating(5.0);
        movieCrew.setPerson(person);
        movieCrew.setMovie(movie);
        movieCrew.setMovieCrewType(movieCrewType);
        return movieCrewRepository.save(movieCrew);
    }

    public Article createArticle(ApplicationUser author, ArticleStatus status) {
        Article article = new Article();
        article.setTitle("Some title");
        article.setText("Some long long long text");
        article.setStatus(status);
        article.setDislikesCount(444);
        article.setLikesCount(111);
        article.setAuthor(author);
        return articleRepository.save(article);
    }

    public Comment createComment(
            ApplicationUser author,
            UUID targetObjectId,
            CommentStatus status,
            TargetObjectType targetObjectType) {
        Comment comment = new Comment();
        comment.setMessage("text");
        comment.setStatus(status);
        comment.setLikesCount(45);
        comment.setDislikesCount(78);
        comment.setAuthor(author);
        comment.setTargetObjectType(targetObjectType);
        comment.setTargetObjectId(targetObjectId);
        return commentRepository.save(comment);
    }

    public Genre createGenre(String genreName) {
        Genre genre = new Genre();
        genre.setGenreName(genreName);
        genre.setDescription("Scary movies");
        return genreRepository.save(genre);
    }

    public Rating createRating(
            Integer starRating,
            ApplicationUser author,
            UUID targetObjectId,
            TargetObjectType targetObjectType
    ) {
        Rating rating = new Rating();
        rating.setRating(starRating);
        rating.setRatedObjectId(targetObjectId);
        rating.setRatedObjectType(targetObjectType);
        rating.setAuthor(author);
        return ratingRepository.save(rating);
    }

    public Like createLike(
            Boolean meLiked,
            ApplicationUser author,
            UUID likedObjectId
    ) {
        Like like = new Like();
        like.setMeLiked(meLiked);
        like.setAuthor(author);
        like.setLikedObjectType(TargetObjectType.ARTICLE);
        like.setLikedObjectId(likedObjectId);
        return likeRepository.save(like);
    }

    public Like createLike(
            Boolean meLiked,
            ApplicationUser author,
            UUID likedObjectId,
            TargetObjectType targetType
    ) {
        Like like = new Like();
        like.setMeLiked(meLiked);
        like.setAuthor(author);
        like.setLikedObjectType(targetType);
        like.setLikedObjectId(likedObjectId);
        return likeRepository.save(like);
    }
}
