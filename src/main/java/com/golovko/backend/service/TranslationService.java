package com.golovko.backend.service;

import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.ArticleCreateDTO;
import com.golovko.backend.dto.article.ArticlePatchDTO;
import com.golovko.backend.dto.article.ArticlePutDTO;
import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.comment.CommentPatchDTO;
import com.golovko.backend.dto.comment.CommentPutDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.genre.GenrePatchDTO;
import com.golovko.backend.dto.genre.GenrePutDTO;
import com.golovko.backend.dto.like.LikePatchDTO;
import com.golovko.backend.dto.like.LikePutDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.dto.movie.MoviePatchDTO;
import com.golovko.backend.dto.movie.MoviePutDTO;
import com.golovko.backend.dto.moviecast.MovieCastCreateDTO;
import com.golovko.backend.dto.moviecast.MovieCastPatchDTO;
import com.golovko.backend.dto.moviecast.MovieCastPutDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewCreateDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewPatchDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewPutDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewReadDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonPutDTO;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingPatchDTO;
import com.golovko.backend.dto.rating.RatingPutDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.dto.user.UserPatchDTO;
import com.golovko.backend.dto.user.UserPutDTO;
import com.golovko.backend.dto.watchlist.WatchlistReadDTO;
import com.golovko.backend.repository.RepositoryHelper;
import lombok.extern.slf4j.Slf4j;
import org.bitbucket.brunneng.ot.Configuration;
import org.bitbucket.brunneng.ot.ObjectTranslator;
import org.bitbucket.brunneng.ot.exceptions.TranslationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class TranslationService {

    @Autowired
    private RepositoryHelper repoHelper;

    private ObjectTranslator objectTranslator;

    public TranslationService() {
        objectTranslator = new ObjectTranslator(createConfig());
    }

    private Configuration createConfig() {
        Configuration c = new Configuration();

        configureForAbstractEntity(c);
        configureApplicationUser(c);
        configureForArticle(c);
        configureForComment(c);
        configureForComplaint(c);
        configureForGenre(c);
        configureForLike(c);
        configureForMisprint(c);
        configureForMovieCast(c);
        configureForMovieCrew(c);
        configureForMovie(c);
        configureForPerson(c);
        configureForRating(c);
        configureForWatchlist(c);

        return c;
    }

    private void configureForAbstractEntity(Configuration c) {
        c.beanOfClass(AbstractEntity.class).setIdentifierProperty("id");
        c.beanOfClass(AbstractEntity.class).setBeanFinder(
                (beanClass, id) -> repoHelper.getReferenceIfExist(beanClass, (UUID) id));
    }

    private void configureApplicationUser(Configuration c) {
        c.beanOfClass(UserPatchDTO.class).translationTo(ApplicationUser.class).mapOnlyNotNullProperties();

        c.beanOfClass(UserPutDTO.class).translationTo(ApplicationUser.class);
    }

    private void configureForArticle(Configuration c) {
        Configuration.Translation t = c.beanOfClass(Article.class).translationTo(ArticleReadDTO.class);
        t.srcProperty("author.id").translatesTo("authorId");

        Configuration.Translation toEntity = c.beanOfClass(ArticleCreateDTO.class).translationTo(Article.class);
        toEntity.srcProperty("authorId").translatesTo("author.id");

        c.beanOfClass(ArticlePatchDTO.class).translationTo(Article.class).mapOnlyNotNullProperties();

        c.beanOfClass(ArticlePutDTO.class).translationTo(Article.class);
    }

    private void configureForComment(Configuration c) {
        Configuration.Translation t = c.beanOfClass(Comment.class).translationTo(CommentReadDTO.class);
        t.srcProperty("author.id").translatesTo("authorId");

        c.beanOfClass(CommentPatchDTO.class).translationTo(Comment.class).mapOnlyNotNullProperties();

        c.beanOfClass(CommentPutDTO.class).translationTo(Comment.class);
    }

    private void configureForComplaint(Configuration c) {
        Configuration.Translation t = c.beanOfClass(Complaint.class).translationTo(ComplaintReadDTO.class);
        t.srcProperty("author.id").translatesTo("authorId");
        t.srcProperty("moderator.id").translatesTo("moderatorId");

        c.beanOfClass(ComplaintPatchDTO.class).translationTo(Complaint.class).mapOnlyNotNullProperties();

        c.beanOfClass(ComplaintPutDTO.class).translationTo(Complaint.class);
    }

    private void configureForGenre(Configuration c) {
        c.beanOfClass(GenrePatchDTO.class).translationTo(Genre.class).mapOnlyNotNullProperties();

        c.beanOfClass(GenrePutDTO.class).translationTo(Genre.class);
    }

    private void configureForLike(Configuration c) {
        Configuration.Translation t = c.beanOfClass(Like.class).translationTo(LikeReadDTO.class);
        t.srcProperty("author.id").translatesTo("authorId");

        c.beanOfClass(LikePatchDTO.class).translationTo(Like.class).mapOnlyNotNullProperties();

        c.beanOfClass(LikePutDTO.class).translationTo(Like.class);
    }

    private void configureForMisprint(Configuration c) {
        Configuration.Translation t = c.beanOfClass(Misprint.class).translationTo(MisprintReadDTO.class);
        t.srcProperty("author.id").translatesTo("authorId");
        t.srcProperty("moderator.id").translatesTo("moderatorId");
    }

    private void configureForMovieCast(Configuration c) {
        Configuration.Translation t = c.beanOfClass(MovieCast.class).translationTo(MovieCastReadDTO.class);
        t.srcProperty("person.id").translatesTo("personId");
        t.srcProperty("movie.id").translatesTo("movieId");

        Configuration.Translation toEntity = c.beanOfClass(MovieCastCreateDTO.class).translationTo(MovieCast.class);
        toEntity.srcProperty("personId").translatesTo("person.id");

        c.beanOfClass(MovieCastPatchDTO.class).translationTo(MovieCast.class).mapOnlyNotNullProperties();

        c.beanOfClass(MovieCastPutDTO.class).translationTo(MovieCast.class);
    }

    private void configureForMovieCrew(Configuration c) {
        Configuration.Translation t = c.beanOfClass(MovieCrew.class).translationTo(MovieCrewReadDTO.class);
        t.srcProperty("person.id").translatesTo("personId");
        t.srcProperty("movie.id").translatesTo("movieId");

        Configuration.Translation toEntity = c.beanOfClass(MovieCrewCreateDTO.class).translationTo(MovieCrew.class);
        toEntity.srcProperty("personId").translatesTo("person.id");

        c.beanOfClass(MovieCrewPatchDTO.class).translationTo(MovieCrew.class).mapOnlyNotNullProperties();

        c.beanOfClass(MovieCrewPutDTO.class).translationTo(MovieCrew.class);
    }

    private void configureForMovie(Configuration c) {
        c.beanOfClass(MoviePatchDTO.class).translationTo(Movie.class).mapOnlyNotNullProperties();

        c.beanOfClass(MoviePutDTO.class).translationTo(Movie.class);
    }

    private void configureForPerson(Configuration c) {
        c.beanOfClass(PersonPatchDTO.class).translationTo(Person.class).mapOnlyNotNullProperties();

        c.beanOfClass(PersonPutDTO.class).translationTo(Person.class);
    }

    private void configureForRating(Configuration c) {
        Configuration.Translation t = c.beanOfClass(Rating.class).translationTo(RatingReadDTO.class);
        t.srcProperty("author.id").translatesTo("authorId");

        Configuration.Translation toEntity = c.beanOfClass(RatingCreateDTO.class).translationTo(Rating.class);
        toEntity.srcProperty("authorId").translatesTo("author.id");

        c.beanOfClass(RatingPatchDTO.class).translationTo(Rating.class).mapOnlyNotNullProperties();

        c.beanOfClass(RatingPutDTO.class).translationTo(Rating.class);
    }

    private void configureForWatchlist(Configuration c) {
        Configuration.Translation t = c.beanOfClass(Watchlist.class).translationTo(WatchlistReadDTO.class);
        t.srcProperty("author.id").translatesTo("authorId");
    }

    public <T> T translate(Object srcObject, Class<T> targetClass) {
        try {
            return objectTranslator.translate(srcObject, targetClass);
        } catch (TranslationException e) {
            log.warn(e.getMessage());
            throw (RuntimeException) e.getCause();
        }
    }

    public <T> void map(Object srcObject, Object destObject) {
        try {
            objectTranslator.mapBean(srcObject, destObject);
        } catch (TranslationException e) {
            log.warn(e.getMessage());
            throw (RuntimeException) e.getCause();
        }
    }

    public <E, T> PageResult<T> toPageResult(Page<E> page, Class<T> targetClass) {
        PageResult<T> result = new PageResult<>();
        result.setPage(page.getNumber());
        result.setPageSize(page.getSize());
        result.setTotalPages(page.getTotalPages());
        result.setTotalElements(page.getTotalElements());
        result.setData(translateList(page.getContent(), targetClass));

        return result;
    }

    public <T> PageResult<T> toPageResult(Page<T> pageOfDTOs) {
        PageResult<T> result = new PageResult<>();
        result.setPage(pageOfDTOs.getNumber());
        result.setPageSize(pageOfDTOs.getSize());
        result.setTotalPages(pageOfDTOs.getTotalPages());
        result.setTotalElements(pageOfDTOs.getTotalElements());
        result.setData(pageOfDTOs.getContent());

        return result;
    }

    public <T> List<T> translateList(List<?> objects, Class<T> targetClass) {
        return objects.stream().map(o -> translate(o, targetClass)).collect(Collectors.toList());
    }
}
