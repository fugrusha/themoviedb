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
import com.golovko.backend.dto.movie.MovieCreateDTO;
import com.golovko.backend.dto.movie.MoviePatchDTO;
import com.golovko.backend.dto.movie.MoviePutDTO;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.moviecast.*;
import com.golovko.backend.dto.movieparticipation.*;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonPutDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.dto.user.*;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TranslationService {

    @Autowired
    private RepositoryHelper repoHelper;

    /*
        ApplicationUser translations
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

    public UserReadExtendedDTO toReadExtended(ApplicationUser user) {
        UserReadExtendedDTO dto = new UserReadExtendedDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        return dto;
    }

    public ApplicationUser toEntity(UserCreateDTO createDTO) {
        ApplicationUser user = new ApplicationUser();
        user.setUsername(createDTO.getUsername());
        user.setEmail(createDTO.getEmail());
        user.setPassword(createDTO.getPassword());
        return user;
    }

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
        dto.setIsReleased(movie.getIsReleased());
        dto.setAverageRating(movie.getAverageRating());
        dto.setCreatedAt(movie.getCreatedAt());
        dto.setUpdatedAt(movie.getUpdatedAt());
        return dto;
    }

    public Movie toEntity(MovieCreateDTO createDTO) {
        Movie movie = new Movie();
        movie.setMovieTitle(createDTO.getMovieTitle());
        movie.setDescription(createDTO.getDescription());
        movie.setReleaseDate(createDTO.getReleaseDate());
        movie.setIsReleased(createDTO.getIsReleased());
        return movie;
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
        dto.setComplaintStatus(complaint.getComplaintStatus());
        dto.setAuthorId(complaint.getAuthor().getId());
        dto.setCreatedAt(complaint.getCreatedAt());
        dto.setUpdatedAt(complaint.getUpdatedAt());
        dto.setParentType(complaint.getParentType());
        dto.setParentId(complaint.getParentId());
        if (complaint.getModerator() != null) {
            dto.setModeratorId(complaint.getModerator().getId());
        }
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
        dto.setCreatedAt(person.getCreatedAt());
        dto.setUpdatedAt(person.getUpdatedAt());
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
        dto.setPartType(movieParticipation.getPartType());
        dto.setMovieId(movieParticipation.getMovie().getId());
        dto.setPersonId(movieParticipation.getPerson().getId());
        dto.setCreatedAt(movieParticipation.getCreatedAt());
        dto.setUpdatedAt(movieParticipation.getUpdatedAt());
        return dto;
    }

    public MoviePartReadExtendedDTO toReadExtended(MovieParticipation movieParticipation) {
        MoviePartReadExtendedDTO dto = new MoviePartReadExtendedDTO();
        dto.setId(movieParticipation.getId());
        dto.setPartInfo(movieParticipation.getPartInfo());
        dto.setPartType(movieParticipation.getPartType());
        dto.setAverageRating(movieParticipation.getAverageRating());
        dto.setMovie(toRead(movieParticipation.getMovie()));
        dto.setPerson(toRead(movieParticipation.getPerson()));
        dto.setCreatedAt(movieParticipation.getCreatedAt());
        dto.setUpdatedAt(movieParticipation.getUpdatedAt());
        return dto;
    }

    public MovieParticipation toEntity(MoviePartCreateDTO createDTO) {
        MovieParticipation movieParticipation = new MovieParticipation();
        movieParticipation.setPartInfo(createDTO.getPartInfo());
        movieParticipation.setPartType(createDTO.getPartType());
        movieParticipation.setPerson(repoHelper.getReferenceIfExist(Person.class, createDTO.getPersonId()));
        return movieParticipation;
    }

    public void updateEntity(MoviePartPutDTO updateDTO, MovieParticipation movieParticipation) {
        movieParticipation.setPartType(updateDTO.getPartType());
        movieParticipation.setPartInfo(updateDTO.getPartInfo());
        movieParticipation.setPerson(repoHelper.getReferenceIfExist(Person.class, updateDTO.getPersonId()));
    }

    public void patchEntity(MoviePartPatchDTO patchDTO, MovieParticipation movieParticipation) {
        if (patchDTO.getPartType() != null) {
            movieParticipation.setPartType(patchDTO.getPartType());
        }
        if (patchDTO.getPartInfo() != null) {
            movieParticipation.setPartInfo(patchDTO.getPartInfo());
        }
        if (patchDTO.getPersonId() != null) {
            movieParticipation.setPerson(repoHelper.getReferenceIfExist(Person.class, patchDTO.getPersonId()));
        }
    }

    public List<MoviePartReadDTO> toReadListOfMoviePart(List<MovieParticipation> listOfMoviePart) {
        return listOfMoviePart.stream().map(this::toRead).collect(Collectors.toList());
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
        dto.setCreatedAt(movieCast.getCreatedAt());
        dto.setUpdatedAt(movieCast.getUpdatedAt());
        return dto;
    }

    public MovieCast toEntity(MovieCastCreateDTO createDTO) {
        MovieCast movieCast = new MovieCast();
        movieCast.setPartInfo(createDTO.getPartInfo());
        movieCast.setCharacter(createDTO.getCharacter());
        movieCast.setPerson(repoHelper.getReferenceIfExist(Person.class, createDTO.getPersonId()));
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
        dto.setCreatedAt(movieCast.getCreatedAt());
        dto.setUpdatedAt(movieCast.getUpdatedAt());
        return dto;
    }

    public void updateEntity(MovieCastPutDTO updateDTO, MovieCast movieCast) {
        movieCast.setCharacter(updateDTO.getCharacter());
        movieCast.setPartInfo(updateDTO.getPartInfo());
        movieCast.setPerson(repoHelper.getReferenceIfExist(Person.class, updateDTO.getPersonId()));
    }

    public void patchEntity(MovieCastPatchDTO patchDTO, MovieCast movieCast) {
        if (patchDTO.getCharacter() != null) {
            movieCast.setCharacter(patchDTO.getCharacter());
        }
        if (patchDTO.getPartInfo() != null) {
            movieCast.setPartInfo(patchDTO.getPartInfo());
        }
        if (patchDTO.getPersonId() != null) {
            movieCast.setPerson(repoHelper.getReferenceIfExist(Person.class, patchDTO.getPersonId()));
        }
    }

    public List<MovieCastReadDTO> toReadList(List<MovieCast> listOfMovieCast) {
        return listOfMovieCast.stream().map(this::toRead).collect(Collectors.toList());
    }

    /*
        Article translations
    */
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

    public Article toEntity(ArticleCreateDTO createDTO) {
        Article article = new Article();
        article.setTitle(createDTO.getTitle());
        article.setText(createDTO.getText());
        article.setStatus(createDTO.getStatus());
        return article;
    }

    public void updateEntity(Article article, ArticlePutDTO putDTO) {
        article.setTitle(putDTO.getTitle());
        article.setText(putDTO.getText());
        article.setStatus(putDTO.getStatus());
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

    /*
         Comment translations
     */
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
        dto.setParentType(comment.getParentType());
        dto.setParentId(comment.getParentId());
        return dto;
    }

    public Comment toEntity(CommentCreateDTO createDTO) {
        Comment comment = new Comment();
        comment.setMessage(createDTO.getMessage());
        return comment;
    }

    public void updateEntity(CommentPutDTO putDTO, Comment comment) {
        comment.setMessage(putDTO.getMessage());
    }

    public void patchEntity(CommentPatchDTO patchDTO, Comment comment) {
        if (patchDTO.getMessage() != null) {
            comment.setMessage(patchDTO.getMessage());
        }
    }
}
