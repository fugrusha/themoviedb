package com.golovko.backend.util;

import com.golovko.backend.domain.*;
import com.golovko.backend.repository.*;
import org.bitbucket.brunneng.br.Configuration;
import org.bitbucket.brunneng.br.RandomObjectGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
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

    protected RandomObjectGenerator flatGenerator;
    {
        Configuration c = new Configuration();
        c.setFlatMode(true);
        flatGenerator = new RandomObjectGenerator(c);
    }

    protected <T extends AbstractEntity> T generateFlatEntityWithoutId(Class<T> entityClass) {
        T entity = flatGenerator.generateRandomObject(entityClass);
        entity.setId(null);
        return entity;
    }

    public Movie createMovie() {
        Movie movie = generateFlatEntityWithoutId(Movie.class);
        movie.setAverageRating(null);
        return movieRepository.save(movie);
    }

    public Movie createMovie(LocalDate releasedDate) {
        Movie movie = generateFlatEntityWithoutId(Movie.class);
        movie.setReleaseDate(releasedDate);
        movie.setIsReleased(false);
        movie.setAverageRating(5.0);
        return movieRepository.save(movie);
    }

    public Movie createMovie(Double averageRating) {
        Movie movie = generateFlatEntityWithoutId(Movie.class);
        movie.setAverageRating(averageRating);
        return movieRepository.save(movie);
    }

    public Person createPerson() {
        Person person = generateFlatEntityWithoutId(Person.class);
        person.setAverageRatingByRoles(null);
        person.setAverageRatingByMovies(null);
        return personRepository.save(person);
    }

    public Person createPerson(String lastName) {
        Person person = generateFlatEntityWithoutId(Person.class);
        person.setLastName(lastName);
        person.setAverageRatingByRoles(null);
        person.setAverageRatingByMovies(null);
        return personRepository.save(person);
    }

    public ApplicationUser createUser() {
        ApplicationUser user = generateFlatEntityWithoutId(ApplicationUser.class);
        user.setTrustLevel(5.0);
        user.setPassword("123456789");
        user.setEmail("vetal@gmail.com");
        user.setIsBlocked(false);
        return applicationUserRepository.save(user);
    }

    public ApplicationUser createUser(Double trustLevel, Boolean isBlocked) {
        ApplicationUser user = new ApplicationUser();
        user.setUsername("Vitalka");
        user.setPassword("123456789");
        user.setEmail("vetal@gmail.com");
        user.setTrustLevel(5.0);
        user.setIsBlocked(isBlocked);
        user.setTrustLevel(trustLevel);
        return applicationUserRepository.save(user);
    }

    public Complaint createComplaint(
            ApplicationUser author
    ) {
        Complaint complaint = generateFlatEntityWithoutId(Complaint.class);
        complaint.setComplaintStatus(ComplaintStatus.INITIATED);
        complaint.setAuthor(author);
        complaint.setModerator(null);
        return complaintRepository.save(complaint);
    }

    public Complaint createComplaint(
            UUID targetObjectId,
            TargetObjectType targetObjectType,
            ComplaintType complaintType,
            ApplicationUser author
    ) {
        Complaint complaint = generateFlatEntityWithoutId(Complaint.class);
        complaint.setComplaintStatus(ComplaintStatus.INITIATED);
        complaint.setAuthor(author);
        complaint.setModerator(null);
        complaint.setComplaintType(complaintType);
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
        MovieCast movieCast = generateFlatEntityWithoutId(MovieCast.class);
        movieCast.setMovieCrewType(MovieCrewType.CAST);
        movieCast.setAverageRating(null);
        movieCast.setPerson(person);
        movieCast.setMovie(movie);
        return movieCastRepository.save(movieCast);
    }

    public MovieCast createMovieCast(Person person, Movie movie, Double rating) {
        MovieCast movieCast = generateFlatEntityWithoutId(MovieCast.class);
        movieCast.setMovieCrewType(MovieCrewType.CAST);
        movieCast.setAverageRating(rating);
        movieCast.setPerson(person);
        movieCast.setMovie(movie);
        return movieCastRepository.save(movieCast);
    }

    public MovieCrew createMovieCrew(Person person, Movie movie) {
        MovieCrew movieCrew = generateFlatEntityWithoutId(MovieCrew.class);
        movieCrew.setAverageRating(null);
        movieCrew.setPerson(person);
        movieCrew.setMovie(movie);
        return movieCrewRepository.save(movieCrew);
    }

    public MovieCrew createMovieCrew(Person person, Movie movie, MovieCrewType movieCrewType) {
        MovieCrew movieCrew = new MovieCrew();
        movieCrew.setDescription("Some text");
        movieCrew.setAverageRating(5.0);
        movieCrew.setPerson(person);
        movieCrew.setMovie(movie);
        movieCrew.setMovieCrewType(movieCrewType);
        return movieCrewRepository.save(movieCrew);
    }

    public Article createArticle(ApplicationUser author, ArticleStatus status) {
        Article article = generateFlatEntityWithoutId(Article.class);
        article.setStatus(status);
        article.setAuthor(author);
        return articleRepository.save(article);
    }

    public Article createExtendedArticle(
            ApplicationUser author, ArticleStatus status,
            List<Person> people, List<Movie> movies) {
        Article article = generateFlatEntityWithoutId(Article.class);
        article.setPeople(people);
        article.setStatus(status);
        article.setAuthor(author);
        article.setMovies(movies);
        return articleRepository.save(article);
    }

    public Comment createComment(
            ApplicationUser author,
            UUID targetObjectId,
            CommentStatus status,
            TargetObjectType targetObjectType) {
        Comment comment = generateFlatEntityWithoutId(Comment.class);
        comment.setStatus(status);
        comment.setAuthor(author);
        comment.setTargetObjectType(targetObjectType);
        comment.setTargetObjectId(targetObjectId);
        return commentRepository.save(comment);
    }

    public Genre createGenre(String genreName) {
        Genre genre = generateFlatEntityWithoutId(Genre.class);
        genre.setGenreName(genreName);
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
