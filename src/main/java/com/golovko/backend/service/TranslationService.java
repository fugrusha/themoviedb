package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.genre.*;
import com.golovko.backend.dto.like.LikeCreateDTO;
import com.golovko.backend.dto.like.LikePatchDTO;
import com.golovko.backend.dto.like.LikePutDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.dto.movie.*;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.dto.moviecrew.*;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonPutDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.dto.user.UserCreateDTO;
import com.golovko.backend.dto.user.UserPatchDTO;
import com.golovko.backend.dto.user.UserPutDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Service
public class TranslationService {

    @Autowired
    private RepositoryHelper repoHelper;

    /*
    toRead methods
     */
    public UserReadDTO toRead(ApplicationUser user) {
        UserReadDTO dto = new UserReadDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setIsBlocked(user.getIsBlocked());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    public MovieReadDTO toRead(Movie movie) {
        MovieReadDTO dto = new MovieReadDTO();
        dto.setId(movie.getId());
        dto.setMovieTitle(movie.getMovieTitle());
        dto.setDescription(movie.getDescription());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setIsReleased(movie.getIsReleased());
        dto.setAverageRating(movie.getAverageRating());
        dto.setCreatedAt(movie.getCreatedAt());
        dto.setUpdatedAt(movie.getUpdatedAt());
        return dto;
    }

    public ComplaintReadDTO toRead(Complaint complaint) {
        ComplaintReadDTO dto = new ComplaintReadDTO();
        dto.setId(complaint.getId());
        dto.setComplaintTitle(complaint.getComplaintTitle());
        dto.setComplaintText(complaint.getComplaintText());
        dto.setComplaintType(complaint.getComplaintType());
        dto.setComplaintStatus(complaint.getComplaintStatus());
        dto.setAuthorId(complaint.getAuthor().getId());
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());
        dto.setTargetObjectType(complaint.getTargetObjectType());
        dto.setTargetObjectId(complaint.getTargetObjectId());
        if (complaint.getModerator() != null) {
            dto.setModeratorId(complaint.getModerator().getId());
        }
        return dto;
    }

    public PersonReadDTO toRead(Person person) {
        PersonReadDTO dto = new PersonReadDTO();
        dto.setId(person.getId());
        dto.setFirstName(person.getFirstName());
        dto.setLastName(person.getLastName());
        dto.setGender(person.getGender());
        dto.setCreatedAt(person.getCreatedAt());
        dto.setUpdatedAt(person.getUpdatedAt());
        return dto;
    }

    public MovieCrewReadDTO toRead(MovieCrew movieCrew) {
        MovieCrewReadDTO dto = new MovieCrewReadDTO();
        dto.setId(movieCrew.getId());
        dto.setDescription(movieCrew.getDescription());
        dto.setAverageRating(movieCrew.getAverageRating());
        dto.setMovieCrewType(movieCrew.getMovieCrewType());
        dto.setMovieId(movieCrew.getMovie().getId());
        if (movieCrew.getPerson() != null) {
            dto.setPersonId(movieCrew.getPerson().getId());
        }
        dto.setCreatedAt(movieCrew.getCreatedAt());
        dto.setUpdatedAt(movieCrew.getUpdatedAt());
        return dto;
    }

    public MovieCastReadDTO toRead(MovieCast movieCast) {
        MovieCastReadDTO dto = new MovieCastReadDTO();
        dto.setId(movieCast.getId());
        dto.setDescription(movieCast.getDescription());
        dto.setAverageRating(movieCast.getAverageRating());
        dto.setMovieCrewType(movieCast.getMovieCrewType());
        dto.setMovieId(movieCast.getMovie().getId());
        if (movieCast.getPerson() != null) {
            dto.setPersonId(movieCast.getPerson().getId());
        }
        dto.setCharacter(movieCast.getCharacter());
        dto.setCreatedAt(movieCast.getCreatedAt());
        dto.setUpdatedAt(movieCast.getUpdatedAt());
        return dto;
    }

    public ArticleReadDTO toRead(Article article) {
        ArticleReadDTO dto = new ArticleReadDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setText(article.getText());
        dto.setStatus(article.getStatus());
        dto.setDislikesCount(article.getDislikesCount());
        dto.setLikesCount(article.getLikesCount());
        dto.setAuthorId(article.getAuthor().getId());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        return dto;
    }

    public CommentReadDTO toRead(Comment comment) {
        CommentReadDTO dto = new CommentReadDTO();
        dto.setId(comment.getId());
        dto.setMessage(comment.getMessage());
        dto.setDislikesCount(comment.getDislikesCount());
        dto.setLikesCount(comment.getLikesCount());
        dto.setStatus(comment.getStatus());
        dto.setAuthorId(comment.getAuthor().getId());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setTargetObjectType(comment.getTargetObjectType());
        dto.setTargetObjectId(comment.getTargetObjectId());
        return dto;
    }

    public GenreReadDTO toRead(Genre genre) {
        GenreReadDTO dto = new GenreReadDTO();
        dto.setId(genre.getId());
        dto.setGenreName(genre.getGenreName());
        dto.setDescription(genre.getDescription());
        dto.setCreatedAt(genre.getCreatedAt());
        dto.setUpdatedAt(genre.getUpdatedAt());
        return dto;
    }

    public RatingReadDTO toRead(Rating rating) {
        RatingReadDTO dto = new RatingReadDTO();
        dto.setId(rating.getId());
        dto.setRating(rating.getRating());
        dto.setAuthorId(rating.getAuthor().getId());
        dto.setCreatedAt(rating.getCreatedAt());
        dto.setUpdatedAt(rating.getUpdatedAt());
        dto.setRatedObjectId(rating.getRatedObjectId());
        dto.setRatedObjectType(rating.getRatedObjectType());
        return dto;
    }

    public LikeReadDTO toRead(Like like) {
        LikeReadDTO dto = new LikeReadDTO();
        dto.setId(like.getId());
        dto.setMeLiked(like.getMeLiked());
        dto.setAuthorId(like.getAuthor().getId());
        dto.setCreatedAt(like.getCreatedAt());
        dto.setUpdatedAt(like.getUpdatedAt());
        dto.setLikedObjectId(like.getLikedObjectId());
        dto.setLikedObjectType(like.getLikedObjectType());
        return dto;
    }

    /*
    toReadExtended methods
     */

    public MovieReadExtendedDTO toReadExtended(Movie movie) {
        MovieReadExtendedDTO dto = new MovieReadExtendedDTO();
        dto.setId(movie.getId());
        dto.setMovieTitle(movie.getMovieTitle());
        dto.setDescription(movie.getDescription());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setIsReleased(movie.getIsReleased());
        dto.setAverageRating(movie.getAverageRating());
        dto.setCreatedAt(movie.getCreatedAt());
        dto.setUpdatedAt(movie.getUpdatedAt());
        dto.setMovieCasts(movie.getMovieCasts().stream().map(this::toRead).collect(Collectors.toSet()));
        dto.setMovieCrews(movie.getMovieCrews().stream().map(this::toRead).collect(Collectors.toSet()));
        dto.setGenres(movie.getGenres().stream().map(this::toRead).collect(Collectors.toSet()));
        return dto;
    }

    public MovieCrewReadExtendedDTO toReadExtended(MovieCrew movieCrew) {
        MovieCrewReadExtendedDTO dto = new MovieCrewReadExtendedDTO();
        dto.setId(movieCrew.getId());
        dto.setDescription(movieCrew.getDescription());
        dto.setMovieCrewType(movieCrew.getMovieCrewType());
        dto.setAverageRating(movieCrew.getAverageRating());
        dto.setMovie(toRead(movieCrew.getMovie()));
        dto.setPerson(toRead(movieCrew.getPerson()));
        dto.setCreatedAt(movieCrew.getCreatedAt());
        dto.setUpdatedAt(movieCrew.getUpdatedAt());
        return dto;
    }

    public MovieCastReadExtendedDTO toReadExtended(MovieCast movieCast) {
        MovieCastReadExtendedDTO dto = new MovieCastReadExtendedDTO();
        dto.setId(movieCast.getId());
        dto.setDescription(movieCast.getDescription());
        dto.setMovieCrewType(movieCast.getMovieCrewType());
        dto.setAverageRating(movieCast.getAverageRating());
        dto.setMovie(toRead(movieCast.getMovie()));
        dto.setPerson(toRead(movieCast.getPerson()));
        dto.setCreatedAt(movieCast.getCreatedAt());
        dto.setUpdatedAt(movieCast.getUpdatedAt());
        return dto;
    }

    public ArticleReadExtendedDTO toReadExtended(Article article) {
        ArticleReadExtendedDTO dto = new ArticleReadExtendedDTO();
        dto.setId(article.getId());
        dto.setTitle(article.getTitle());
        dto.setText(article.getText());
        dto.setStatus(article.getStatus());
        dto.setDislikesCount(article.getDislikesCount());
        dto.setLikesCount(article.getLikesCount());
        dto.setAuthor(toRead(article.getAuthor()));
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        return dto;
    }

    public GenreReadExtendedDTO toReadExtended(Genre genre) {
        GenreReadExtendedDTO dto = new GenreReadExtendedDTO();
        dto.setId(genre.getId());
        dto.setGenreName(genre.getGenreName());
        dto.setDescription(genre.getDescription());
        dto.setCreatedAt(genre.getCreatedAt());
        dto.setUpdatedAt(genre.getUpdatedAt());
        dto.setMovies(genre.getMovies().stream().map(this::toRead).collect(Collectors.toList()));
        return dto;
    }

    /*
    toEntity methods
     */
    public ApplicationUser toEntity(UserCreateDTO createDTO) {
        ApplicationUser user = new ApplicationUser();
        user.setUsername(createDTO.getUsername());
        user.setEmail(createDTO.getEmail());
        user.setPassword(createDTO.getPassword());
        return user;
    }

    public Movie toEntity(MovieCreateDTO createDTO) {
        Movie movie = new Movie();
        movie.setMovieTitle(createDTO.getMovieTitle());
        movie.setDescription(createDTO.getDescription());
        movie.setReleaseDate(createDTO.getReleaseDate());
        movie.setIsReleased(createDTO.getIsReleased());
        return movie;
    }

    public Complaint toEntity(ComplaintCreateDTO createDTO) {
        Complaint complaint = new Complaint();
        complaint.setComplaintTitle(createDTO.getComplaintTitle());
        complaint.setComplaintText(createDTO.getComplaintText());
        complaint.setComplaintType(createDTO.getComplaintType());
        complaint.setTargetObjectType(createDTO.getTargetObjectType());
        complaint.setTargetObjectId(createDTO.getTargetObjectId());
        return complaint;
    }

    public Person toEntity(PersonCreateDTO createDTO) {
        Person person = new Person();
        person.setFirstName(createDTO.getFirstName());
        person.setLastName(createDTO.getLastName());
        person.setGender(createDTO.getGender());
        return person;
    }

    public MovieCrew toEntity(MovieCrewCreateDTO createDTO) {
        MovieCrew movieCrew = new MovieCrew();
        movieCrew.setDescription(createDTO.getDescription());
        movieCrew.setMovieCrewType(createDTO.getMovieCrewType());
        if (createDTO.getPersonId() != null) {
            movieCrew.setPerson(repoHelper.getReferenceIfExist(Person.class, createDTO.getPersonId()));
        }
        return movieCrew;
    }

    public MovieCast toEntity(MovieCastCreateDTO createDTO) {
        MovieCast movieCast = new MovieCast();
        movieCast.setDescription(createDTO.getDescription());
        movieCast.setCharacter(createDTO.getCharacter());
        if (createDTO.getPersonId() != null) {
            movieCast.setPerson(repoHelper.getReferenceIfExist(Person.class, createDTO.getPersonId()));
        }
        return movieCast;
    }

    public Article toEntity(ArticleCreateDTO createDTO) {
        Article article = new Article();
        article.setTitle(createDTO.getTitle());
        article.setText(createDTO.getText());
        article.setStatus(createDTO.getStatus());
        article.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, createDTO.getAuthorId()));
        return article;
    }

    public Comment toEntity(CommentCreateDTO createDTO) {
        Comment comment = new Comment();
        comment.setMessage(createDTO.getMessage());
        comment.setTargetObjectType(createDTO.getTargetObjectType());
        comment.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, createDTO.getAuthorId()));
        return comment;
    }

    public Genre toEntity(GenreCreateDTO createDTO) {
        Genre genre = new Genre();
        genre.setGenreName(createDTO.getGenreName());
        genre.setDescription(createDTO.getDescription());
        return genre;
    }

    public Rating toEntity(RatingCreateDTO createDTO) {
        Rating rating = new Rating();
        rating.setRating(createDTO.getRating());
        rating.setAuthor(repoHelper.getReferenceIfExist(ApplicationUser.class, createDTO.getAuthorId()));
        rating.setRatedObjectType(createDTO.getRatedObjectType());
        return rating;
    }

    public Like toEntity(LikeCreateDTO createDTO) {
        Like like = new Like();
        like.setMeLiked(createDTO.getMeLiked());
        like.setLikedObjectType(createDTO.getLikedObjectType());
        like.setLikedObjectId(createDTO.getLikedObjectId());
        return like;
    }

    /*
    patchEntity methods
     */
    public void patchEntity(UserPatchDTO patchDTO, ApplicationUser user) {
        if (patchDTO.getUsername() != null) {
            user.setUsername(patchDTO.getUsername()); // username cannot be editable
        }
        if (patchDTO.getEmail() != null) {
            user.setEmail(patchDTO.getEmail());
        }
        if (patchDTO.getPassword() != null) {
            user.setPassword(patchDTO.getPassword());
        }
    }

    public void patchEntity(MoviePatchDTO patchDTO, Movie movie) {
        if (patchDTO.getMovieTitle() != null) {
            movie.setMovieTitle(patchDTO.getMovieTitle());
        }
        if (patchDTO.getDescription() != null) {
            movie.setDescription(patchDTO.getDescription());
        }
        if (patchDTO.getReleaseDate() != null) {
            movie.setReleaseDate(patchDTO.getReleaseDate());
        }
        if (patchDTO.getIsReleased() != null) {
            movie.setIsReleased(patchDTO.getIsReleased());
        }
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

    public void patchEntity(MovieCrewPatchDTO patchDTO, MovieCrew movieCrew) {
        if (patchDTO.getMovieCrewType() != null) {
            movieCrew.setMovieCrewType(patchDTO.getMovieCrewType());
        }
        if (patchDTO.getDescription() != null) {
            movieCrew.setDescription(patchDTO.getDescription());
        }
        if (patchDTO.getPersonId() != null) {
            movieCrew.setPerson(repoHelper.getReferenceIfExist(Person.class, patchDTO.getPersonId()));
        }
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

    public void patchEntity(MovieCastPatchDTO patchDTO, MovieCast movieCast) {
        if (patchDTO.getCharacter() != null) {
            movieCast.setCharacter(patchDTO.getCharacter());
        }
        if (patchDTO.getDescription() != null) {
            movieCast.setDescription(patchDTO.getDescription());
        }
        if (patchDTO.getPersonId() != null) {
            movieCast.setPerson(repoHelper.getReferenceIfExist(Person.class, patchDTO.getPersonId()));
        }
    }

    public void patchEntity(Article article, ArticlePatchDTO patchDTO) {
        if (patchDTO.getTitle() != null) {
            article.setTitle(patchDTO.getTitle());
        }
        if (patchDTO.getText() != null) {
            article.setText(patchDTO.getText());
        }
        if (patchDTO.getStatus() != null) {
            article.setStatus(patchDTO.getStatus());
        }
    }

    public void patchEntity(CommentPatchDTO patchDTO, Comment comment) {
        if (patchDTO.getMessage() != null) {
            comment.setMessage(patchDTO.getMessage());
        }
    }

    public void patchEntity(GenrePatchDTO patchDTO, Genre genre) {
        if (patchDTO.getGenreName() != null) {
            genre.setGenreName(patchDTO.getGenreName());
        }
        if (patchDTO.getDescription() != null) {
            genre.setDescription(patchDTO.getDescription());
        }
    }

    public void patchEntity(RatingPatchDTO patchDTO, Rating rating) {
        if (patchDTO.getRating() != null) {
            rating.setRating(patchDTO.getRating());
        }
    }

    public void patchEntity(LikePatchDTO patchDTO, Like like) {
        if (patchDTO.getMeLiked() != null) {
            like.setMeLiked(patchDTO.getMeLiked());
        }
    }

    /*
    updateEntity methods
     */
    public void updateEntity(UserPutDTO update, ApplicationUser user) {
        user.setUsername(update.getUsername());
        user.setEmail(update.getEmail());
        user.setPassword(update.getPassword());
    }

    public void updateEntity(MoviePutDTO updateDTO, Movie movie) {
        movie.setMovieTitle(updateDTO.getMovieTitle());
        movie.setReleaseDate(updateDTO.getReleaseDate());
        movie.setDescription(updateDTO.getDescription());
        movie.setIsReleased(updateDTO.getIsReleased());
        movie.setAverageRating(updateDTO.getAverageRating());
    }

    public void updateEntity(ComplaintPutDTO updateDTO, Complaint complaint) {
        complaint.setComplaintTitle(updateDTO.getComplaintTitle());
        complaint.setComplaintText(updateDTO.getComplaintText());
        complaint.setComplaintType(updateDTO.getComplaintType());
    }

    public void updateEntity(PersonPutDTO updateDTO, Person person) {
        person.setFirstName(updateDTO.getFirstName());
        person.setLastName(updateDTO.getLastName());
        person.setGender(updateDTO.getGender());
    }

    public void updateEntity(MovieCrewPutDTO updateDTO, MovieCrew movieCrew) {
        movieCrew.setMovieCrewType(updateDTO.getMovieCrewType());
        movieCrew.setDescription(updateDTO.getDescription());
        movieCrew.setPerson(repoHelper.getReferenceIfExist(Person.class, updateDTO.getPersonId()));
    }

    public void updateEntity(MovieCastPutDTO updateDTO, MovieCast movieCast) {
        movieCast.setCharacter(updateDTO.getCharacter());
        movieCast.setDescription(updateDTO.getDescription());
        movieCast.setPerson(repoHelper.getReferenceIfExist(Person.class, updateDTO.getPersonId()));
    }

    public void updateEntity(Article article, ArticlePutDTO putDTO) {
        article.setTitle(putDTO.getTitle());
        article.setText(putDTO.getText());
        article.setStatus(putDTO.getStatus());
    }

    public void updateEntity(CommentPutDTO putDTO, Comment comment) {
        comment.setMessage(putDTO.getMessage());
    }

    public void updateEntity(GenrePutDTO putDTO, Genre genre) {
        genre.setGenreName(putDTO.getGenreName());
        genre.setDescription(putDTO.getDescription());
    }

    public void updateEntity(RatingPutDTO putDTO, Rating rating) {
        rating.setRating(putDTO.getRating());
    }

    public void updateEntity(LikePutDTO updateDTO, Like like) {
        like.setMeLiked(updateDTO.getMeLiked());
    }
}
