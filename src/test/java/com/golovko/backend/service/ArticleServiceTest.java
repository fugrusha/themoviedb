package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.LinkDuplicatedException;
import com.golovko.backend.repository.*;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionSystemException;

import java.util.*;

import static com.golovko.backend.domain.ArticleStatus.*;
import static com.golovko.backend.domain.TargetObjectType.ARTICLE;

public class ArticleServiceTest extends BaseTest {

    @Autowired
    private ArticleService articleService;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private MovieRepository movieRepository;

    @Test
    public void testGetArticleById() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);

        ArticleReadDTO readDTO = articleService.getArticle(article.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(article, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), article.getAuthor().getId());
    }

    @Test
    public void testGetArticleExtended() {
        ApplicationUser user = testObjectFactory.createUser();
        Person person = testObjectFactory.createPerson();
        Movie movie = testObjectFactory.createMovie();
        Article article = testObjectFactory.createExtendedArticle(user, PUBLISHED, List.of(person), List.of(movie));

        ArticleReadExtendedDTO readDTO = articleService.getArticleExtended(article.getId());

        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(article,
                "author", "movies", "people");
        Assertions.assertThat(readDTO.getAuthor()).isEqualToComparingFieldByField(article.getAuthor());
        Assertions.assertThat(readDTO.getPeople()).extracting("id").contains(person.getId());
        Assertions.assertThat(readDTO.getMovies()).extracting("id").contains(movie.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetArticleWrongId() {
        articleService.getArticle(UUID.randomUUID());
    }

    @Test
    public void testGetAllArticles() {
        ApplicationUser user1 = testObjectFactory.createUser();
        ApplicationUser user2 = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(user1, PUBLISHED);
        Article a2 = testObjectFactory.createArticle(user2, PUBLISHED);
        testObjectFactory.createArticle(user1, DRAFT);
        testObjectFactory.createArticle(user2, NEED_MODERATION);
        testObjectFactory.createArticle(user2, DRAFT);

        PageResult<ArticleReadDTO> articles = articleService.getAllPublishedArticles(Pageable.unpaged());

        Assertions.assertThat(articles.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId(), a2.getId());
    }

    @Test
    public void testGetArticlesWithPagingAndSorting() {
        ApplicationUser user1 = testObjectFactory.createUser();
        testObjectFactory.createArticle(user1, PUBLISHED);
        testObjectFactory.createArticle(user1, PUBLISHED);
        Article a3 = testObjectFactory.createArticle(user1, PUBLISHED);
        Article a4 = testObjectFactory.createArticle(user1, PUBLISHED);

        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Assertions.assertThat(articleService.getAllPublishedArticles(pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(a4.getId(), a3.getId()));
    }

    @Test
    public void testCreateArticle() {
        ApplicationUser author = testObjectFactory.createUser();

        ArticleCreateDTO createDTO = new ArticleCreateDTO();
        createDTO.setTitle("Text title");
        createDTO.setText("Some text");
        createDTO.setStatus(DRAFT);
        createDTO.setAuthorId(author.getId());

        ArticleReadDTO readDTO = articleService.createArticle(createDTO);

        Assertions.assertThat(createDTO).isEqualToIgnoringGivenFields(readDTO, "authorId");
        Assert.assertNotNull(readDTO.getId());

        Article article = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(article, "authorId");
        Assert.assertEquals(readDTO.getAuthorId(), article.getAuthor().getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testCreateArticleWrongAuthor() {
        ArticleCreateDTO createDTO = new ArticleCreateDTO();
        createDTO.setTitle("Text title");
        createDTO.setText("Some text");
        createDTO.setStatus(DRAFT);
        createDTO.setAuthorId(UUID.randomUUID());

        articleService.createArticle(createDTO);
    }

    @Test
    public void testUpdateArticle() {
        ArticlePutDTO updateDTO = new ArticlePutDTO();
        updateDTO.setTitle("Title");
        updateDTO.setText("Some text");
        updateDTO.setStatus(PUBLISHED);

        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, DRAFT);

        ArticleReadDTO readDTO = articleService.updateArticle(article.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        article = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(article).isEqualToIgnoringGivenFields(readDTO,
                "author", "people", "movies");
        Assert.assertEquals(article.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testPatchArticle() {
        ArticlePatchDTO patchDTO = new ArticlePatchDTO();
        patchDTO.setTitle("Article title");
        patchDTO.setText("Article text");
        patchDTO.setStatus(NEED_MODERATION);

        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, DRAFT);

        ArticleReadDTO readDTO = articleService.patchArticle(article.getId(), patchDTO);

        Assertions.assertThat(patchDTO).isEqualToComparingFieldByField(readDTO);

        Article articleAfterUpdate = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(articleAfterUpdate).isEqualToIgnoringGivenFields(readDTO,
                "author", "people", "movies");
        Assert.assertEquals(articleAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testPatchArticleEmptyPatch() {
        ArticlePatchDTO patchDTO = new ArticlePatchDTO();

        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, DRAFT);

        ArticleReadDTO readDTO = articleService.patchArticle(article.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        Article articleAfterUpdate = articleRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(articleAfterUpdate).hasNoNullFieldsOrPropertiesExcept("people", "movies");
        Assertions.assertThat(articleAfterUpdate).isEqualToIgnoringGivenFields(article,
                "author", "people", "movies");
        Assert.assertEquals(articleAfterUpdate.getAuthor().getId(), readDTO.getAuthorId());
    }

    @Test
    public void testDeleteArticle() {
        ApplicationUser author = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, DRAFT);
        articleService.deleteArticle(article.getId());

        Assert.assertFalse(articleRepository.existsById(article.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteArticleNotFound() {
        articleService.deleteArticle(UUID.randomUUID());
    }

    @Test
    public void testDeleteArticleWithCompositeItems() {
        ApplicationUser author = testObjectFactory.createUser();
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(author, PUBLISHED);

        Comment c1 = testObjectFactory.createComment(user, article.getId(), CommentStatus.APPROVED, ARTICLE);
        Comment c2 = testObjectFactory.createComment(user, article.getId(), CommentStatus.APPROVED, ARTICLE);

        Like like1 = testObjectFactory.createLike(true, user, article.getId(), ARTICLE);
        Like like2 = testObjectFactory.createLike(true, author, article.getId(), ARTICLE);

        articleService.deleteArticle(article.getId());

        Assert.assertFalse(articleRepository.existsById(article.getId()));

        Assert.assertFalse(commentRepository.existsById(c1.getId()));
        Assert.assertFalse(commentRepository.existsById(c2.getId()));

        Assert.assertFalse(likeRepository.existsById(like1.getId()));
        Assert.assertFalse(likeRepository.existsById(like2.getId()));
    }

    @Test
    public void testGetArticlesByEmptyFilter() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, PUBLISHED);
        Article a2 = testObjectFactory.createArticle(author, DRAFT);
        Article a3 = testObjectFactory.createArticle(author, BLOCKED);

        ArticleManagerFilter filter = new ArticleManagerFilter();

        PageResult<ArticleReadDTO> actualResult = articleService.getArticlesByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId(), a2.getId(), a3.getId());
    }

    @Test
    public void testGetArticlesWithEmptySetOfFilter() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, PUBLISHED);
        Article a2 = testObjectFactory.createArticle(author, DRAFT);
        Article a3 = testObjectFactory.createArticle(author, BLOCKED);

        ArticleManagerFilter filter = new ArticleManagerFilter();
        filter.setStatuses(new HashSet<ArticleStatus>());

        PageResult<ArticleReadDTO> actualResult = articleService.getArticlesByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId(), a2.getId(), a3.getId());
    }

    @Test
    public void testGetArticlesByAuthor() {
        ApplicationUser author1 = testObjectFactory.createUser();
        ApplicationUser author2 = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author1, PUBLISHED);
        Article a2 = testObjectFactory.createArticle(author1, DRAFT);
        testObjectFactory.createArticle(author2, BLOCKED);
        testObjectFactory.createArticle(author2, PUBLISHED);

        ArticleManagerFilter filter = new ArticleManagerFilter();
        filter.setAuthorId(author1.getId());

        PageResult<ArticleReadDTO> actualResult = articleService.getArticlesByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId(), a2.getId());
    }

    @Test
    public void testGetArticlesByStatus() {
        ApplicationUser author = testObjectFactory.createUser();
        Article a1 = testObjectFactory.createArticle(author, DRAFT);
        testObjectFactory.createArticle(author, PUBLISHED);
        testObjectFactory.createArticle(author, BLOCKED);

        ArticleManagerFilter filter = new ArticleManagerFilter();
        filter.setStatuses(Set.of(DRAFT));

        PageResult<ArticleReadDTO> actualResult = articleService.getArticlesByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId());
    }

    @Test
    public void testGetArticlesByAllFilters() {
        ApplicationUser author1 = testObjectFactory.createUser();
        ApplicationUser author2 = testObjectFactory.createUser();

        Article a1 = testObjectFactory.createArticle(author1, DRAFT);
        testObjectFactory.createArticle(author1, PUBLISHED);
        testObjectFactory.createArticle(author1, BLOCKED);
        testObjectFactory.createArticle(author2, DRAFT);

        ArticleManagerFilter filter = new ArticleManagerFilter();
        filter.setAuthorId(author1.getId());
        filter.setStatuses(Set.of(DRAFT));

        PageResult<ArticleReadDTO> actualResult = articleService.getArticlesByFilter(filter, Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(a1.getId());
    }

    @Test
    public void testGetArticlesWithEmptyFilterWithPagingAndSorting() {
        ApplicationUser author = testObjectFactory.createUser();

        Article a1 = testObjectFactory.createArticle(author, PUBLISHED);
        a1.setTitle("Begin ....");
        Article a2 = testObjectFactory.createArticle(author, PUBLISHED);
        a2.setTitle("Mandatory ....");
        Article a3 = testObjectFactory.createArticle(author, PUBLISHED);
        a3.setTitle("From ....");
        articleRepository.saveAll(List.of(a1, a2, a3));

        ArticleManagerFilter filter = new ArticleManagerFilter();
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "title"));
        Assertions.assertThat(articleService.getArticlesByFilter(filter, pageRequest).getData())
                .extracting("id").isEqualTo(Arrays.asList(a2.getId(), a3.getId()));
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveArticleNotNullValidation() {
        Article article = new Article();
        articleRepository.save(article);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveArticleMaxSizeValidation() {
        ApplicationUser author = testObjectFactory.createUser();

        Article article = new Article();
        article.setTitle("article title".repeat(100));
        article.setText("long long text".repeat(1000));
        article.setStatus(DRAFT);
        article.setAuthor(author);
        articleRepository.save(article);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveArticleMinSizeValidation() {
        ApplicationUser author = testObjectFactory.createUser();

        Article article = new Article();
        article.setTitle("");
        article.setText("");
        article.setStatus(DRAFT);
        article.setAuthor(author);
        articleRepository.save(article);
    }

    @Test
    public void testGetPeopleByArticleId() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        Person person = testObjectFactory.createPerson();

        article.setPeople(List.of(person));
        articleRepository.save(article);

        List<PersonReadDTO> actualResult = articleService.getArticlePeople(article.getId());

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(person.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetNotFoundPersonByArticleId() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);

        articleService.getArticlePeople(article.getId());
    }

    @Test
    public void testAddPersonToArticle() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        Person person = testObjectFactory.createPerson();

        List<PersonReadDTO> actualResult = articleService.addPersonToArticle(article.getId(), person.getId());

        Assert.assertNotNull(actualResult);
        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(person.getId());

        inTransaction(() -> {
            Article savedArticle = articleRepository.findById(article.getId()).get();
            Assertions.assertThat(savedArticle.getPeople()).extracting("id")
                    .containsExactlyInAnyOrder(person.getId());

            Person savedPerson = personRepository.findById(person.getId()).get();
            Assertions.assertThat(savedPerson.getArticles()).extracting("id")
                    .containsExactlyInAnyOrder(article.getId());
        });
    }

    @Test
    public void testDuplicatedPerson() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        Person person = testObjectFactory.createPerson();

        articleService.addPersonToArticle(article.getId(), person.getId());

        Assertions.assertThatThrownBy(() -> articleService.addPersonToArticle(article.getId(), person.getId()))
                .isInstanceOf(LinkDuplicatedException.class);
    }

    @Test(expected = EntityNotFoundException.class)
    public void tesAddPersonToArticleWrongArticleId() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        UUID wrongPersonId = UUID.randomUUID();

        articleService.addPersonToArticle(article.getId(), wrongPersonId);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAddPersonToArticleWrongPersonId() {
        UUID wrongArticleId = UUID.randomUUID();
        Person person = testObjectFactory.createPerson();

        articleService.addPersonToArticle(wrongArticleId, person.getId());
    }

    @Test
    public void testRemovePersonFromArticle() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        Person person = testObjectFactory.createPerson();

        articleService.addPersonToArticle(article.getId(), person.getId());

        List<PersonReadDTO> remainingPeople = articleService.removePersonFromArticle(article.getId(), person.getId());
        Assert.assertTrue(remainingPeople.isEmpty());

        inTransaction(() -> {
            Article updatedArticle = articleRepository.findById(article.getId()).get();
            Assert.assertTrue(updatedArticle.getPeople().isEmpty());
        });
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteNotFoundPerson() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        Person person = testObjectFactory.createPerson();

        articleService.removePersonFromArticle(article.getId(), person.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteNotExistedPerson() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        UUID wrongPersonId = UUID.randomUUID();

        articleService.removePersonFromArticle(article.getId(), wrongPersonId);
    }

    @Test
    public void testGetMoviesByArticleId() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        article.setMovies(List.of(movie));
        articleRepository.save(article);

        List<MovieReadDTO> actualResult = articleService.getArticleMovies(article.getId());

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(movie.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetNotFoundMovieByArticleId() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);

        articleService.getArticleMovies(article.getId());
    }

    @Test
    public void testAddMovieToArticle() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        List<MovieReadDTO> actualResult = articleService.addMovieToArticle(article.getId(), movie.getId());

        Assert.assertNotNull(actualResult);
        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(movie.getId());

        inTransaction(() -> {
            Article savedArticle = articleRepository.findById(article.getId()).get();
            Assertions.assertThat(savedArticle.getMovies()).extracting("id")
                    .containsExactlyInAnyOrder(movie.getId());

            Movie savedMovie = movieRepository.findById(movie.getId()).get();
            Assertions.assertThat(savedMovie.getArticles()).extracting("id")
                    .containsExactlyInAnyOrder(article.getId());
        });
    }

    @Test
    public void testDuplicatedMovie() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        articleService.addMovieToArticle(article.getId(), movie.getId());

        Assertions.assertThatThrownBy(() -> articleService.addMovieToArticle(article.getId(), movie.getId()))
                .isInstanceOf(LinkDuplicatedException.class);
    }

    @Test(expected = EntityNotFoundException.class)
    public void tesAddMovieToArticleWrongArticleId() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        UUID wrongMovieId = UUID.randomUUID();

        articleService.addMovieToArticle(article.getId(), wrongMovieId);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAddMovieToArticleWrongMovieId() {
        UUID wrongArticleId = UUID.randomUUID();
        Movie movie = testObjectFactory.createMovie();

        articleService.addMovieToArticle(wrongArticleId, movie.getId());
    }

    @Test
    public void testRemoveMovieFromArticle() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        articleService.addMovieToArticle(article.getId(), movie.getId());

        List<MovieReadDTO> remainingMovies = articleService.removeMovieFromArticle(article.getId(), movie.getId());
        Assert.assertTrue(remainingMovies.isEmpty());

        inTransaction(() -> {
            Article updatedArticle = articleRepository.findById(article.getId()).get();
            Assert.assertTrue(updatedArticle.getPeople().isEmpty());
        });
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteNotFoundMovie() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        Movie movie = testObjectFactory.createMovie();

        articleService.removeMovieFromArticle(article.getId(), movie.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteNotExistedMovie() {
        ApplicationUser user = testObjectFactory.createUser();
        Article article = testObjectFactory.createArticle(user, PUBLISHED);
        UUID wrongMovieId = UUID.randomUUID();

        articleService.removeMovieFromArticle(article.getId(), wrongMovieId);
    }
}
