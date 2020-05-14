package com.golovko.backend.controller.integration;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.ArticleCreateDTO;
import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.article.ArticleReadExtendedDTO;
import com.golovko.backend.dto.comment.CommentCreateDTO;
import com.golovko.backend.dto.comment.CommentModerateDTO;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintModerateDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.genre.GenreCreateDTO;
import com.golovko.backend.dto.genre.GenreReadDTO;
import com.golovko.backend.dto.like.LikeCreateDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.dto.misprint.MisprintConfirmDTO;
import com.golovko.backend.dto.misprint.MisprintCreateDTO;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.dto.movie.MovieCreateDTO;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.movie.MovieReadExtendedDTO;
import com.golovko.backend.dto.moviecast.MovieCastCreateDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewCreateDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewReadDTO;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.dto.person.PersonReadExtendedDTO;
import com.golovko.backend.dto.rating.RatingCreateDTO;
import com.golovko.backend.dto.rating.RatingReadDTO;
import com.golovko.backend.dto.user.UserCreateDTO;
import com.golovko.backend.dto.user.UserPatchDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.dto.user.UserTrustLevelDTO;
import com.golovko.backend.dto.userrole.UserRoleReadDTO;
import com.golovko.backend.job.UpdateAverageRatingOfMovieCastsJob;
import com.golovko.backend.job.UpdateAverageRatingOfMoviesJob;
import com.golovko.backend.job.UpdateAverageRatingOfPersonMoviesJob;
import com.golovko.backend.job.UpdateAverageRatingOfPersonRolesJob;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.UserRoleRepository;
import com.golovko.backend.util.MyParameterizedTypeImpl;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import static com.golovko.backend.domain.TargetObjectType.*;
import static org.awaitility.Awaitility.await;

@ActiveProfiles({"test", "working-scenario-profile"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WorkingScenarioIntegrationTest extends BaseTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @SpyBean
    private UpdateAverageRatingOfMoviesJob updateAverageRatingOfMoviesJob;

    @SpyBean
    private UpdateAverageRatingOfMovieCastsJob updateAverageRatingOfMovieCastsJob;

    @SpyBean
    private UpdateAverageRatingOfPersonMoviesJob updateAverageRatingOfPersonMoviesJob;

    @SpyBean
    private UpdateAverageRatingOfPersonRolesJob updateAverageRatingOfPersonRolesJob;

    private final String API_URL = "http://localhost:8080/api/v1";

    @Test
    public void testWorkingScenario() throws InterruptedException {
        final String ADMIN_EMAIL = "admin@mail.com";
        final String ADMIN_PASSWORD = "admin_password";
        final String M1_EMAIL = "moderator1@mail.com";
        final String M1_PASSWORD = "moderator1_password";
        final String C1_EMAIL = "content_manager1@mail.com";
        final String C1_PASSWORD = "content_manager1_password";
        final String U1_PASSWORD = "user1_password";
        final String U1_EMAIL = "user1@mail.com";
        final String U2_PASSWORD = "user2_password";
        final String U2_EMAIL = "user2@mail.com";
        final String U3_PASSWORD = "user3_password";
        final String U3_EMAIL = "user3@mail.com";

        // create admin
        ApplicationUser a1 = new ApplicationUser();
        a1.setUsername("super_admin");
        a1.setTrustLevel(10.0);
        a1.setIsBlocked(false);
        a1.setEmail(ADMIN_EMAIL);
        a1.setEncodedPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        UserRole userRole = userRoleRepository.findByType(UserRoleType.ADMIN);
        a1.getUserRoles().add(userRole);
        applicationUserRepository.save(a1);

        // check if repo contains only 1 record
        Assert.assertEquals(1, applicationUserRepository.count());
        // check if user has ADMIN authority
        ApplicationUser admin = applicationUserRepository.findByEmail(ADMIN_EMAIL);
        Assert.assertEquals(UserRoleType.ADMIN, admin.getUserRoles().get(0).getType());

        // FINAL_1 register user m1
        UserCreateDTO m1CreateDTO = createUserDTO(M1_EMAIL, M1_PASSWORD, Gender.FEMALE);

        UserReadDTO m1ReadDTO = performRequest(
                "/users",
                HttpMethod.POST,
                m1CreateDTO,
                null,
                UserReadDTO.class);

        Assertions.assertThat(m1ReadDTO).hasNoNullFieldsOrProperties();
        Assertions.assertThat(m1CreateDTO).isEqualToComparingOnlyGivenFields(m1ReadDTO,
                "username", "email");
        UUID m1UserId = m1ReadDTO.getId();

        // FINAL_2 a1 gives m1 the role of moderator
        List<UserRoleReadDTO> allUserRoles = performListRequest(
                "/user-roles/",
                HttpMethod.GET,
                null,
                getAuthHeaders(ADMIN_EMAIL, ADMIN_PASSWORD),
                UserRoleReadDTO.class);

        UUID moderatorRoleId = allUserRoles.stream()
                .filter(role -> UserRoleType.MODERATOR.equals(role.getType()))
                .findAny()
                .map(UserRoleReadDTO::getId)
                .orElse(null);

        List<UserRoleReadDTO> m1UserRoles = performListRequest(
                "/users/" + m1UserId + "/roles/" + moderatorRoleId,
                HttpMethod.POST,
                null,
                getAuthHeaders(ADMIN_EMAIL, ADMIN_PASSWORD),
                UserRoleReadDTO.class);

        Assert.assertEquals(2, m1UserRoles.size());
        Assertions.assertThat(m1UserRoles).extracting("type")
                .containsExactlyInAnyOrder(UserRoleType.USER, UserRoleType.MODERATOR);

        // FINAL_3 register user c1
        UserCreateDTO c1CreateDTO = createUserDTO(C1_EMAIL, C1_PASSWORD, Gender.MALE);

        UserReadDTO c1ReadDTO = performRequest(
                "/users",
                HttpMethod.POST,
                c1CreateDTO,
                null,
                UserReadDTO.class);

        Assertions.assertThat(c1ReadDTO).hasNoNullFieldsOrProperties();
        Assertions.assertThat(c1CreateDTO).isEqualToComparingOnlyGivenFields(c1ReadDTO,
                "username", "email");
        UUID c1UserId = c1ReadDTO.getId();

        // FINAL_4 a1 gives c1 the role of content manager
        UUID contentManagerRoleId = allUserRoles.stream()
                .filter(role -> UserRoleType.CONTENT_MANAGER.equals(role.getType()))
                .findAny()
                .map(UserRoleReadDTO::getId)
                .orElse(null);

        List<UserRoleReadDTO> c1UserRoles = performListRequest(
                "/users/" + c1UserId + "/roles/" + contentManagerRoleId,
                HttpMethod.POST,
                null,
                getAuthHeaders(ADMIN_EMAIL, ADMIN_PASSWORD),
                UserRoleReadDTO.class);

        Assert.assertEquals(2, c1UserRoles.size());
        Assertions.assertThat(c1UserRoles).extracting("type")
                .containsExactlyInAnyOrder(UserRoleType.USER, UserRoleType.CONTENT_MANAGER);

        // FINAL_5 Three regular users are registered. Men: u1, u2 and woman: u3
        // register u1
        UserCreateDTO u1CreateDTO = createUserDTO(U1_EMAIL, U1_PASSWORD, Gender.MALE);

        UserReadDTO u1ReadDTO = performRequest(
                "/users",
                HttpMethod.POST,
                u1CreateDTO,
                null,
                UserReadDTO.class);

        Assertions.assertThat(u1ReadDTO).hasNoNullFieldsOrProperties();
        Assertions.assertThat(u1CreateDTO).isEqualToComparingOnlyGivenFields(u1ReadDTO,
                "username", "email", "gender");
        UUID u1UserId = u1ReadDTO.getId();

        // register u2
        UserCreateDTO u2CreateDTO = createUserDTO(U2_EMAIL, U2_PASSWORD, Gender.MALE);

        UserReadDTO u2ReadDTO = performRequest(
                "/users",
                HttpMethod.POST,
                u2CreateDTO,
                null,
                UserReadDTO.class);

        Assertions.assertThat(u2ReadDTO).hasNoNullFieldsOrProperties();
        Assertions.assertThat(u2CreateDTO).isEqualToComparingOnlyGivenFields(u2ReadDTO,
                "username", "email", "gender");
        UUID u2UserId = u2ReadDTO.getId();

        // register u3
        UserCreateDTO u3CreateDTO = createUserDTO(U3_EMAIL, U3_PASSWORD, Gender.FEMALE);

        UserReadDTO u3ReadDTO = performRequest(
                "/users",
                HttpMethod.POST,
                u3CreateDTO,
                null,
                UserReadDTO.class);

        Assertions.assertThat(u3ReadDTO).hasNoNullFieldsOrProperties();
        Assertions.assertThat(u3CreateDTO).isEqualToComparingOnlyGivenFields(u3ReadDTO,
                "username", "email", "gender");
        UUID u3UserId = u3ReadDTO.getId();

        // FINAL_6 u2 changes the name in his profile.
        UserPatchDTO u2PatchDTO = new UserPatchDTO();
        u2PatchDTO.setUsername("new_username");

        ResponseEntity<UserReadDTO> u2UpdatedReadDTOResponse = new RestTemplate(new HttpComponentsClientHttpRequestFactory())
                .exchange(API_URL + "/users/" + u2UserId, HttpMethod.PATCH,
                        new HttpEntity<>(u2PatchDTO, getAuthHeaders(U2_EMAIL, U2_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, u2UpdatedReadDTOResponse.getStatusCode());
        Assert.assertEquals(u2PatchDTO.getUsername(), u2UpdatedReadDTOResponse.getBody().getUsername());

        // FINAL_7 c1 creates a film, characters, actors, crew and others
        // create movie
        MovieCreateDTO movieCreateDTO = createMovieDTO();

        MovieReadDTO movieReadDTO = performRequest(
                "/movies/",
                HttpMethod.POST,
                movieCreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieReadDTO.class);

        Assertions.assertThat(movieCreateDTO).isEqualToIgnoringNullFields(movieReadDTO);
        UUID movieId = movieReadDTO.getId();

        // create genres
        GenreCreateDTO biographyGenre = createGenreDTO("Biography");
        GenreCreateDTO comedyGenre = createGenreDTO("Comedy");
        GenreCreateDTO dramaGenre = createGenreDTO("Drama");

        GenreReadDTO g1ReadDTO = performRequest(
                "/genres/",
                HttpMethod.POST,
                biographyGenre,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                GenreReadDTO.class);

        UUID biographyGenreId = g1ReadDTO.getId();

        GenreReadDTO g2ReadDTO = performRequest(
                "/genres/",
                HttpMethod.POST,
                comedyGenre,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                GenreReadDTO.class);

        UUID comedyGenreId = g2ReadDTO.getId();

        GenreReadDTO g3ReadDTO = performRequest(
                "/genres/",
                HttpMethod.POST,
                dramaGenre,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                GenreReadDTO.class);

        UUID dramaGenreId = g3ReadDTO.getId();

        // add genres to movie

        performListRequest("/movies/" + movieId + "/genres/" + biographyGenreId,
                HttpMethod.POST,
                null,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                GenreReadDTO.class);

        performListRequest("/movies/" + movieId + "/genres/" + comedyGenreId,
                HttpMethod.POST,
                null,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                GenreReadDTO.class);

        List<GenreReadDTO> movieGenres = performListRequest(
                "/movies/" + movieId + "/genres/" + dramaGenreId,
                HttpMethod.POST,
                null,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                GenreReadDTO.class);

        Assert.assertEquals(3, movieGenres.size());
        Assertions.assertThat(movieGenres).extracting("id")
                .containsExactlyInAnyOrder(biographyGenreId, comedyGenreId, dramaGenreId);

        // create person director
        PersonCreateDTO p1CreateDTO = createP1();

        PersonReadDTO p1ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p1CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p1CreateDTO).isEqualToIgnoringNullFields(p1ReadDTO);
        UUID directorId = p1ReadDTO.getId();

        // create crew director
        MovieCrewCreateDTO crew1CreateDTO = createCrewDTO(directorId, MovieCrewType.DIRECTOR);

        MovieCrewReadDTO crew1ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-crews/",
                HttpMethod.POST,
                crew1CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCrewReadDTO.class);

        Assertions.assertThat(crew1CreateDTO).isEqualToIgnoringNullFields(crew1ReadDTO);

        // create person writer1
        PersonCreateDTO p2CreateDTO = createP2();

        PersonReadDTO p2ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p2CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p2CreateDTO).isEqualToIgnoringNullFields(p2ReadDTO);
        UUID writer1Id = p2ReadDTO.getId();

        // create crew writer1
        MovieCrewCreateDTO crew2CreateDTO = createCrewDTO(writer1Id, MovieCrewType.WRITER);

        MovieCrewReadDTO crew2ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-crews/",
                HttpMethod.POST,
                crew2CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCrewReadDTO.class);

        Assertions.assertThat(crew2CreateDTO).isEqualToIgnoringNullFields(crew2ReadDTO);

        // create person writer
        PersonCreateDTO p3CreateDTO = createP3();

        PersonReadDTO p3ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p3CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p3CreateDTO).isEqualToIgnoringNullFields(p3ReadDTO);
        UUID writer2Id = p3ReadDTO.getId();

        // create crew writer2
        MovieCrewCreateDTO crew3CreateDTO = createCrewDTO(writer2Id, MovieCrewType.WRITER);

        MovieCrewReadDTO crew3ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-crews/",
                HttpMethod.POST,
                crew3CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCrewReadDTO.class);

        Assertions.assertThat(crew3CreateDTO).isEqualToIgnoringNullFields(crew3ReadDTO);

        // create actor
        PersonCreateDTO p4CreateDTO = createP4();

        PersonReadDTO p4ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p4CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p4CreateDTO).isEqualToIgnoringNullFields(p4ReadDTO);
        UUID actor1Id = p4ReadDTO.getId();

        // create cast
        MovieCastCreateDTO cast1CreateDTO = createCast1DTO(actor1Id);

        MovieCastReadDTO cast1ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-casts/",
                HttpMethod.POST,
                cast1CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCastReadDTO.class);

        Assertions.assertThat(cast1CreateDTO).isEqualToIgnoringNullFields(cast1ReadDTO);

        // create person actor
        PersonCreateDTO p5CreateDTO = createP5();

        PersonReadDTO p5ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p5CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p5CreateDTO).isEqualToIgnoringNullFields(p5ReadDTO);
        UUID actor2Id = p5ReadDTO.getId();

        // create cast
        MovieCastCreateDTO cast2CreateDTO = createCast2DTO(actor2Id);

        MovieCastReadDTO cast2ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-casts/",
                HttpMethod.POST,
                cast2CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCastReadDTO.class);

        UUID movieCastId = cast2ReadDTO.getId();
        Assertions.assertThat(cast2CreateDTO).isEqualToIgnoringNullFields(cast2ReadDTO);

        // create person actor
        PersonCreateDTO p6CreateDTO = createP6();

        PersonReadDTO p6ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p6CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p6CreateDTO).isEqualToIgnoringNullFields(p6ReadDTO);
        UUID actor3Id = p6ReadDTO.getId();

        // create cast
        MovieCastCreateDTO cast3CreateDTO = createCast3DTO(actor3Id);

        MovieCastReadDTO cast3ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-casts/",
                HttpMethod.POST,
                cast3CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCastReadDTO.class);

        Assertions.assertThat(cast3CreateDTO).isEqualToIgnoringNullFields(cast3ReadDTO);

        // create person producer
        PersonCreateDTO p7CreateDTO = createP7();

        PersonReadDTO p7ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p7CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p7CreateDTO).isEqualToIgnoringNullFields(p7ReadDTO);
        UUID producerId = p7ReadDTO.getId();

        // create crew producer
        MovieCrewCreateDTO crew4CreateDTO = createCrewDTO(producerId, MovieCrewType.PRODUCER);

        MovieCrewReadDTO crew4ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-crews/",
                HttpMethod.POST,
                crew4CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCrewReadDTO.class);

        Assertions.assertThat(crew4CreateDTO).isEqualToIgnoringNullFields(crew4ReadDTO);

        // create person composer
        PersonCreateDTO p8CreateDTO = createP8();

        PersonReadDTO p8ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p8CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p8CreateDTO).isEqualToIgnoringNullFields(p8ReadDTO);
        UUID composerId = p8ReadDTO.getId();

        // create crew composer
        MovieCrewCreateDTO crew5CreateDTO = createCrewDTO(composerId, MovieCrewType.SOUND);

        MovieCrewReadDTO crew5ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-crews/",
                HttpMethod.POST,
                crew5CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCrewReadDTO.class);

        Assertions.assertThat(crew5CreateDTO).isEqualToIgnoringNullFields(crew5ReadDTO);

        // create person costume designer
        PersonCreateDTO p9CreateDTO = createP9();

        PersonReadDTO p9ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p9CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p9CreateDTO).isEqualToIgnoringNullFields(p9ReadDTO);
        UUID costumeDesignerId = p9ReadDTO.getId();

        // create crew costume designer
        MovieCrewCreateDTO crew6CreateDTO = createCrewDTO(costumeDesignerId, MovieCrewType.COSTUME_DESIGNER);

        MovieCrewReadDTO crew6ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-crews/",
                HttpMethod.POST,
                crew6CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCrewReadDTO.class);

        Assertions.assertThat(crew6CreateDTO).isEqualToIgnoringNullFields(crew6ReadDTO);

        // create visual effects designer
        PersonCreateDTO p10CreateDTO = createP10();

        PersonReadDTO p10ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p10CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p10CreateDTO).isEqualToIgnoringNullFields(p10ReadDTO);
        UUID visualEffectsDesignerId = p10ReadDTO.getId();

        // create crew visual effects designer
        MovieCrewCreateDTO crew7CreateDTO = createCrewDTO(visualEffectsDesignerId, MovieCrewType.VISUAL_EFFECTS);

        MovieCrewReadDTO crew7ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-crews/",
                HttpMethod.POST,
                crew7CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCrewReadDTO.class);

        Assertions.assertThat(crew7CreateDTO).isEqualToIgnoringNullFields(crew7ReadDTO);

        // create person editor
        PersonCreateDTO p11CreateDTO = createP11();

        PersonReadDTO p11ReadDTO = performRequest(
                "/people/",
                HttpMethod.POST,
                p11CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assertions.assertThat(p11CreateDTO).isEqualToIgnoringNullFields(p11ReadDTO);
        UUID editorId = p11ReadDTO.getId();

        // create crew editor
        MovieCrewCreateDTO crew8CreateDTO = createCrewDTO(editorId, MovieCrewType.VISUAL_EFFECTS);

        MovieCrewReadDTO crew8ReadDTO = performRequest(
                "/movies/" + movieId + "/movie-crews/",
                HttpMethod.POST,
                crew8CreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieCrewReadDTO.class);

        Assertions.assertThat(crew8CreateDTO).isEqualToIgnoringNullFields(crew8ReadDTO);

        // FINAL_8 c1 add article about movie
        ArticleCreateDTO articleCreateDTO = createArticleDTO(c1UserId);

        ArticleReadDTO articleReadDTO = performRequest(
                "/articles/",
                HttpMethod.POST,
                articleCreateDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                ArticleReadDTO.class);

        UUID articleId = articleReadDTO.getId();

        // add related movie to article
        List<MovieReadDTO> relatedMovies = performListRequest(
                "/articles/" + articleId + "/movies/" + movieId,
                HttpMethod.POST,
                null,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieReadDTO.class);

        Assert.assertEquals(1, relatedMovies.size());
        Assertions.assertThat(relatedMovies).extracting("id").contains(movieId);

        // add related people to article
        performListRequest("/articles/" + articleId + "/people/" + actor1Id,
                HttpMethod.POST,
                null,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        performListRequest("/articles/" + articleId + "/people/" + actor2Id,
                HttpMethod.POST,
                null,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        List<PersonReadDTO> relatedPeople = performListRequest(
                "/articles/" + articleId + "/people/" + actor3Id,
                HttpMethod.POST,
                null,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                PersonReadDTO.class);

        Assert.assertEquals(3, relatedPeople.size());
        Assertions.assertThat(relatedPeople).extracting("id")
                .containsExactlyInAnyOrder(actor1Id, actor2Id, actor3Id);

        // FINAL_9 u1 opens article
        ArticleReadExtendedDTO articleReadExtendedDTO = performRequest(
                "/articles/" + articleId + "/extended/",
                HttpMethod.GET,
                null,
                getAuthHeaders(U1_EMAIL, U1_PASSWORD),
                ArticleReadExtendedDTO.class);

        Assert.assertEquals(articleCreateDTO.getTitle(), articleReadExtendedDTO.getTitle());
        Assert.assertEquals(1, articleReadExtendedDTO.getMovies().size());
        Assert.assertEquals(3, articleReadExtendedDTO.getPeople().size());

        // FINAL_10 u1 and u2 add likes to article
        LikeCreateDTO likeCreateDTO = createLikeDTO(articleId, TargetObjectType.ARTICLE, true);

        performRequest("/users/" + u1UserId + "/likes/",
                HttpMethod.POST,
                likeCreateDTO,
                getAuthHeaders(U1_EMAIL, U1_PASSWORD),
                LikeReadDTO.class);

        performRequest("/users/" + u2UserId + "/likes/",
                HttpMethod.POST,
                likeCreateDTO,
                getAuthHeaders(U2_EMAIL, U2_PASSWORD),
                LikeReadDTO.class);

        ArticleReadDTO updatedArticleReadDTO = performRequest(
                "/articles/" + articleId, HttpMethod.GET,
                null, null, ArticleReadDTO.class);

        Assert.assertEquals((Integer) 2, updatedArticleReadDTO.getLikesCount());

        // FINAL_11 User u3 - mistakenly adds like to article, and then cancels it
        // u3 adds like
        LikeReadDTO u3LikeReadDTO = performRequest(
                "/users/" + u3UserId + "/likes/",
                HttpMethod.POST,
                likeCreateDTO,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                LikeReadDTO.class);

        ArticleReadDTO updated1ArticleReadDTO = performRequest(
                "/articles/" + articleId, HttpMethod.GET,
                null, null, ArticleReadDTO.class);

         Assert.assertEquals((Integer) 3, updated1ArticleReadDTO.getLikesCount());

        // u3 deletes her like
        performRequest("/users/" + u3UserId + "/likes/" + u3LikeReadDTO.getId(),
                HttpMethod.DELETE,
                null,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                Void.class);

        ArticleReadDTO updated2ArticleReadDTO = performRequest(
                "/articles/" + articleId, HttpMethod.GET,
                null, null, ArticleReadDTO.class);

         Assert.assertEquals((Integer) 2, updated2ArticleReadDTO.getLikesCount());

        // FINAL_12 user u3 adds dislike to article
        LikeCreateDTO dislikeCreateDTO = createLikeDTO(articleId, TargetObjectType.ARTICLE, false);

        performRequest("/users/" + u3UserId + "/likes/",
                HttpMethod.POST,
                dislikeCreateDTO,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                LikeReadDTO.class);

        ArticleReadDTO updated3ArticleReadDTO = performRequest(
                "/articles/" + articleId, HttpMethod.GET,
                null, null, ArticleReadDTO.class);

         Assert.assertEquals((Integer) 1, updated3ArticleReadDTO.getDislikesCount());

        // FINAL_13 u3 creates misprint for article without replaceTo text
        MisprintCreateDTO u3MisprintCreateDTO = createMisprintDTO(articleId, TargetObjectType.ARTICLE);

        MisprintReadDTO u3MisprintReadDTO = performRequest(
                "/users/" + u3UserId + "/misprints/",
                HttpMethod.POST,
                u3MisprintCreateDTO,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                MisprintReadDTO.class);

        UUID u3MisprintId = u3MisprintReadDTO.getId();

        // FINAL_14 c1 opens list of misprint complaints with one complaint
        PageResult<MisprintReadDTO> allMisprints1 = performPageRequest(
                "/misprints/",
                HttpMethod.GET,
                null,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MisprintReadDTO.class);

        Assert.assertEquals(1, allMisprints1.getData().size());
        Assertions.assertThat(allMisprints1.getData()).extracting("id").contains(u3MisprintId);
        Assertions.assertThat(allMisprints1.getData()).extracting("status")
                .contains(ComplaintStatus.INITIATED);

        // FINAL_15 c1 moderates complaint and fixes mistake in the article
        MisprintConfirmDTO c1ConfirmDTO = new MisprintConfirmDTO();
        c1ConfirmDTO.setModeratorId(c1UserId);
        c1ConfirmDTO.setStartIndex(28);
        c1ConfirmDTO.setEndIndex(36);
        c1ConfirmDTO.setReplaceTo("musician");

        MisprintReadDTO c1MisprintReadDTO = performRequest(
                "/misprints/" + u3MisprintId + "/confirm",
                HttpMethod.POST,
                c1ConfirmDTO,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MisprintReadDTO.class);

        Assert.assertEquals(c1UserId, c1MisprintReadDTO.getModeratorId());
        Assert.assertEquals(ComplaintStatus.CLOSED, c1MisprintReadDTO.getStatus());

        // check if mistake in article was fixed
        ArticleReadDTO updated4ArticleReadDTO = performRequest(
                "/articles/" + articleId, HttpMethod.GET, null, null, ArticleReadDTO.class);
        Assert.assertTrue(updated4ArticleReadDTO.getText().contains("musician"));

        // FINAL_16 c1 opens list of misprint complaints with zero complaint
        PageResult<MisprintReadDTO> allMisprints2 = performPageRequest(
                "/misprints/?statuses=INITIATED",
                HttpMethod.GET,
                null,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MisprintReadDTO.class);

        Assert.assertEquals(0, allMisprints2.getData().size());

        // FINAL_17 unregistered user opens article and sees 2 likes and 1 dislike
        ArticleReadExtendedDTO articleReadExtendedDTO2 = performRequest(
                "/articles/" + articleId + "/extended/",
                HttpMethod.GET,
                null,
                null,
                ArticleReadExtendedDTO.class);

        Assert.assertEquals((Integer) 1, articleReadExtendedDTO2.getDislikesCount());
        Assert.assertEquals((Integer) 2, articleReadExtendedDTO2.getLikesCount());

        // FINAL_18 u1 opens movie
        MovieReadExtendedDTO u1MovieReadExtendedDTO = performRequest(
                "/movies/" + movieId + "/extended/",
                HttpMethod.GET,
                null,
                getAuthHeaders(U1_EMAIL, U1_PASSWORD),
                MovieReadExtendedDTO.class);

        Assert.assertEquals(movieCreateDTO.getMovieTitle(), u1MovieReadExtendedDTO.getMovieTitle());

        // FINAL_19 u1 writes a review on the film with a spoiler and adds high rating
        // u1 adds comment
        CommentCreateDTO commentCreateDTO = new CommentCreateDTO();
        commentCreateDTO.setMessage("message text with spoiler");
        commentCreateDTO.setSpoiler("spoiler");
        commentCreateDTO.setAuthorId(u1UserId);
        commentCreateDTO.setTargetObjectType(MOVIE);

        CommentReadDTO u1CommentReadDTO = performRequest(
                "/movies/" + movieId + "/comments/",
                HttpMethod.POST,
                commentCreateDTO,
                getAuthHeaders(U1_EMAIL, U1_PASSWORD),
                CommentReadDTO.class);

        UUID u1CommentId = u1CommentReadDTO.getId();

        Assertions.assertThat(commentCreateDTO).isEqualToComparingFieldByField(u1CommentReadDTO);
        Assert.assertEquals(movieId, u1CommentReadDTO.getTargetObjectId());
        Assert.assertEquals(CommentStatus.PENDING, u1CommentReadDTO.getStatus());

        // u1 adds rating
        RatingCreateDTO u1RatingCreateDTO = createRatingDTO(10, u1UserId, MOVIE);

        RatingReadDTO u1RatingReadDTO = performRequest(
                "/movies/" + movieId + "/ratings/",
                HttpMethod.POST,
               u1RatingCreateDTO,
               getAuthHeaders(U1_EMAIL, U1_PASSWORD),
               RatingReadDTO.class);

        Assertions.assertThat(u1RatingCreateDTO).isEqualToComparingFieldByField(u1RatingReadDTO);
        Assert.assertEquals(movieId, u1RatingReadDTO.getRatedObjectId());

        // FINAL_20 u2 gets movie and don't see comment cause it is not passed moderation yet
        PageResult<CommentReadDTO> u2MovieCommentsReadDTO = performPageRequest(
                "/movies/" + movieId + "/comments/",
                HttpMethod.GET,
                null,
                getAuthHeaders(U2_EMAIL, U2_PASSWORD),
                CommentReadDTO.class);

        Assert.assertEquals(0, u2MovieCommentsReadDTO.getData().size());

        // FINAL_21 u2 adds rating to movie
        RatingCreateDTO u2RatingCreateDTO = createRatingDTO(8, u2UserId, MOVIE);

        RatingReadDTO u2RatingReadDTO = performRequest(
                "/movies/" + movieId + "/ratings/",
                HttpMethod.POST,
                u2RatingCreateDTO,
                getAuthHeaders(U2_EMAIL, U2_PASSWORD),
                RatingReadDTO.class);

        Assertions.assertThat(u2RatingCreateDTO).isEqualToComparingFieldByField(u2RatingReadDTO);
        Assert.assertEquals(movieId, u2RatingReadDTO.getRatedObjectId());

        // FINAL_22 m1 opens movie and list of pending comments and notices 1 pending comment
        MovieReadExtendedDTO u2MovieReadExtendedDTO = performRequest(
                "/movies/" + movieId + "/extended/",
                HttpMethod.GET,
                null,
                getAuthHeaders(U2_EMAIL, U2_PASSWORD),
                MovieReadExtendedDTO.class);

        Assert.assertEquals(movieCreateDTO.getMovieTitle(), u2MovieReadExtendedDTO.getMovieTitle());

        // u2 opens list of pending comments and notices 1 pending comment
        PageResult<CommentReadDTO> m1PendingCommentReadDTO = performPageRequest(
                "/comments/?statuses=PENDING",
                HttpMethod.GET,
                null,
                getAuthHeaders(M1_EMAIL, M1_PASSWORD),
                CommentReadDTO.class);

        Assert.assertEquals(1, m1PendingCommentReadDTO.getData().size());
        UUID u1PendingCommentId = m1PendingCommentReadDTO.getData().get(0).getId();

        // FINAL_23
        // m1 moderates pending comment and approves it
        CommentModerateDTO commentModerateDTO = new CommentModerateDTO();
        commentModerateDTO.setNewStatus(CommentStatus.APPROVED);

        CommentReadDTO m1ModeratesComment = performRequest(
                "/comments/" + u1PendingCommentId + "/moderate/",
                HttpMethod.POST,
                commentModerateDTO,
                getAuthHeaders(M1_EMAIL, M1_PASSWORD),
                CommentReadDTO.class);

        Assert.assertEquals(CommentStatus.APPROVED, m1ModeratesComment.getStatus());

        // m1 sets u1 trust level higher than 5, so u1 comments don't need to moderate anymore
        UserTrustLevelDTO trustLevelDTO = new UserTrustLevelDTO();
        trustLevelDTO.setTrustLevel(6.5);

        UserReadDTO m1ChangedU1TrustLevel = performRequest(
                "/users/" + u1UserId + "/set-trust-level/",
                HttpMethod.POST,
                trustLevelDTO,
                getAuthHeaders(M1_EMAIL, M1_PASSWORD),
                UserReadDTO.class);

        Assert.assertEquals(trustLevelDTO.getTrustLevel(), m1ChangedU1TrustLevel.getTrustLevel());

        // FINAL_24 u3 opens movie and adds rating to it. Sees one approved comment
        MovieReadExtendedDTO u3MovieReadExtendedDTO = performRequest(
                "/movies/" + movieId + "/extended/",
                HttpMethod.GET,
                null,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                MovieReadExtendedDTO.class);

        Assert.assertEquals(movieCreateDTO.getMovieTitle(), u3MovieReadExtendedDTO.getMovieTitle());

        // u3 adds rating
        RatingCreateDTO u3RatingCreateDTO = createRatingDTO(6, u3UserId, MOVIE);

        RatingReadDTO u3RatingReadDTO = performRequest(
                "/movies/" + movieId + "/ratings/",
                HttpMethod.POST,
                u3RatingCreateDTO,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                RatingReadDTO.class);

        Assertions.assertThat(u3RatingCreateDTO).isEqualToComparingFieldByField(u3RatingReadDTO);
        Assert.assertEquals(movieId, u3RatingReadDTO.getRatedObjectId());

        //u3 sees approved 1 comment
        PageResult<CommentReadDTO> u3MovieCommentsReadDTO = performPageRequest(
                "/movies/" + movieId + "/comments/",
                HttpMethod.GET,
                null,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                CommentReadDTO.class);

        Assert.assertEquals(1, u3MovieCommentsReadDTO.getData().size());

        // FINAL_25 u3 adds like to comment
        LikeCreateDTO u3LikeCreateDTO = createLikeDTO(u1CommentId, COMMENT, true);

        LikeReadDTO u3CommentLikeReadDTO = performRequest(
                "/users/" + u3UserId + "/likes/",
                HttpMethod.POST,
                u3LikeCreateDTO,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                LikeReadDTO.class);

        Assert.assertEquals(u1CommentId, u3CommentLikeReadDTO.getLikedObjectId());

        CommentReadDTO u3CommentReadDTO = performRequest(
                "/movies/" + movieId + "/comments/" + u1CommentId,
                HttpMethod.GET,
                null,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                CommentReadDTO.class);

        Assert.assertEquals((Integer) 1, u3CommentReadDTO.getLikesCount());

        // FINAL_26 unregistered user opens movie sees average rating
        // wait and check if updateAverageRating() was invoked
        Thread.sleep(10000);
        await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() ->
                        Mockito.verify(updateAverageRatingOfMoviesJob, Mockito.atLeast(2))
                                .updateAverageRating());

        MovieReadExtendedDTO unregisteredUserMovieReadDTO = performRequest(
                "/movies/" + movieId + "/extended/",
                HttpMethod.GET,
                null,
                null,
                MovieReadExtendedDTO.class);

        Assert.assertNotNull(unregisteredUserMovieReadDTO.getAverageRating());
        Assert.assertEquals(8.0, unregisteredUserMovieReadDTO.getAverageRating(), Double.MIN_NORMAL);

        // FINAL_27 c1 imports movie from TheMovieDB with common actor
        // The Curious Case of Benjamin Button
        String externalMovieId = "4922";

        MovieReadExtendedDTO c1ImportedMovieReadDTO = performRequest(
                "/movies/import-movie/" + externalMovieId,
                HttpMethod.POST,
                null,
                getAuthHeaders(C1_EMAIL, C1_PASSWORD),
                MovieReadExtendedDTO.class);

        UUID importedMovieId = c1ImportedMovieReadDTO.getId();

        Assert.assertEquals("The Curious Case of Benjamin Button", c1ImportedMovieReadDTO.getMovieTitle());
        Assert.assertNotNull(c1ImportedMovieReadDTO.getGenres());
        Assert.assertNotNull(c1ImportedMovieReadDTO.getMovieCrews());
        Assert.assertNotNull(c1ImportedMovieReadDTO.getMovieCasts());

        // common person is Mahershala Ali
        UUID commonPersonId = c1ImportedMovieReadDTO.getMovieCasts().stream()
                .filter(cast -> actor2Id.equals(cast.getPersonId()))
                .findAny()
                .map(MovieCastReadDTO::getPersonId)
                .orElse(null);

        Assert.assertEquals(actor2Id, commonPersonId);

        // get imported movie cast id for task #33
        UUID importedMovieCastId = c1ImportedMovieReadDTO.getMovieCasts().stream()
                .filter(cast -> actor2Id.equals(cast.getPersonId()))
                .findAny()
                .map(MovieCastReadDTO::getId)
                .orElse(null);

        // FINAL_28 u1 adds low rating and writes bad comment
        // u1 add rating
        RatingCreateDTO u1ImportedMovieRatingCreateDTO = createRatingDTO(2, u1UserId, MOVIE);

        performRequest(
                "/movies/" + importedMovieId + "/ratings",
                HttpMethod.POST,
                u1ImportedMovieRatingCreateDTO,
                getAuthHeaders(U1_EMAIL, U1_PASSWORD),
                RatingReadDTO.class);

        // u1 adds comment
        CommentCreateDTO u1BadCommentCreateDTO = new CommentCreateDTO();
        u1BadCommentCreateDTO.setMessage("message that contains ");
        u1BadCommentCreateDTO.setAuthorId(u1UserId);
        u1BadCommentCreateDTO.setTargetObjectType(MOVIE);

        CommentReadDTO u1BadCommentReadDTO = performRequest(
                "/movies/" + importedMovieId + "/comments",
                HttpMethod.POST,
                u1BadCommentCreateDTO,
                getAuthHeaders(U1_EMAIL, U1_PASSWORD),
                CommentReadDTO.class);

        UUID u1BadCommentId = u1BadCommentReadDTO.getId();

        Assertions.assertThat(u1BadCommentCreateDTO).isEqualToComparingFieldByField(u1BadCommentReadDTO);
        Assert.assertEquals(importedMovieId, u1BadCommentReadDTO.getTargetObjectId());
        Assert.assertEquals(CommentStatus.APPROVED, u1BadCommentReadDTO.getStatus());

        // FINAL_29 u2 and u3 add ratings to importedMovie, see u1BadComment and send two complaints
        // u2 adds rating
        RatingCreateDTO u2ImportedMovieRatingCreateDTO = createRatingDTO(6, u2UserId, MOVIE);

        performRequest(
                "/movies/" + importedMovieId + "/ratings",
                HttpMethod.POST,
                u2ImportedMovieRatingCreateDTO,
                getAuthHeaders(U2_EMAIL, U2_PASSWORD),
                RatingReadDTO.class);

        // u3 adds rating
        RatingCreateDTO u3ImportedMovieRatingCreateDTO = createRatingDTO(10, u3UserId, MOVIE);

        performRequest(
                "/movies/" + importedMovieId + "/ratings",
                HttpMethod.POST,
                u3ImportedMovieRatingCreateDTO,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                RatingReadDTO.class);

        // u2 can see u1BadComment
        PageResult<CommentReadDTO> u2ImportedMovieComments = performPageRequest(
                "/movies/" + importedMovieId + "/comments",
                HttpMethod.GET,
                null,
                getAuthHeaders(U2_EMAIL, U2_PASSWORD),
                CommentReadDTO.class);

        Assert.assertEquals(u1BadCommentId, u2ImportedMovieComments.getData().get(0).getId());

        // u3 can see u1BadComment
        PageResult<CommentReadDTO> u3ImportedMovieComments = performPageRequest(
                "/movies/" + importedMovieId + "/comments",
                HttpMethod.GET,
                null,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                CommentReadDTO.class);

        Assert.assertEquals(u1BadCommentId, u3ImportedMovieComments.getData().get(0).getId());

        // u2 creates complaint on u1BadComment cause it contains adult language(profanity)
        ComplaintCreateDTO u2ComplaintCreateDTO = new ComplaintCreateDTO();
        u2ComplaintCreateDTO.setComplaintTitle("Comment contains bad words");
        u2ComplaintCreateDTO.setComplaintText("Moderator, you should ban comment's author");
        u2ComplaintCreateDTO.setComplaintType(ComplaintType.PROFANITY);
        u2ComplaintCreateDTO.setTargetObjectId(u1BadCommentId);
        u2ComplaintCreateDTO.setTargetObjectType(COMMENT);

        ComplaintReadDTO u2ComplaintReadDTO = performRequest(
                "/users/" + u2UserId + "/complaints",
                HttpMethod.POST,
                u2ComplaintCreateDTO,
                getAuthHeaders(U2_EMAIL, U2_PASSWORD),
                ComplaintReadDTO.class);

        UUID u2ComplaintId = u2ComplaintReadDTO.getId();
        Assertions.assertThat(u2ComplaintCreateDTO).isEqualToComparingFieldByField(u2ComplaintReadDTO);

        // u3 creates complaint on u1BadComment cause it contains adult language(profanity)
        ComplaintCreateDTO u3ComplaintCreateDTO = new ComplaintCreateDTO();
        u3ComplaintCreateDTO.setComplaintTitle("Author uses profanity");
        u3ComplaintCreateDTO.setComplaintText("Dear moderator, this comment contains bad words");
        u3ComplaintCreateDTO.setComplaintType(ComplaintType.PROFANITY);
        u3ComplaintCreateDTO.setTargetObjectId(u1BadCommentId);
        u3ComplaintCreateDTO.setTargetObjectType(COMMENT);

        ComplaintReadDTO u3ComplaintReadDTO = performRequest(
                "/users/" + u2UserId + "/complaints",
                HttpMethod.POST,
                u3ComplaintCreateDTO,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                ComplaintReadDTO.class);

        UUID u3ComplaintId = u3ComplaintReadDTO.getId();
        Assertions.assertThat(u3ComplaintCreateDTO).isEqualToComparingFieldByField(u3ComplaintReadDTO);

        // FINAL_30 m1 opens pending complaints and sees two new complaints
        PageResult<ComplaintReadDTO> m1AllComplaints = performPageRequest(
                "/complaints?statuses=INITIATED",
                HttpMethod.GET,
                null,
                getAuthHeaders(M1_EMAIL, M1_PASSWORD),
                ComplaintReadDTO.class);

        Assert.assertEquals(2, m1AllComplaints.getData().size());
        Assertions.assertThat(m1AllComplaints.getData()).extracting("id")
                .containsExactlyInAnyOrder(u2ComplaintId, u3ComplaintId);

        UUID firstComplaintId = m1AllComplaints.getData().get(0).getId();

        // FINAL_31 m1 moderates one of the pending complaints, deletes comment and bans u1
        ComplaintModerateDTO complaintModerateDTO = new ComplaintModerateDTO();
        complaintModerateDTO.setComplaintStatus(ComplaintStatus.CLOSED);
        complaintModerateDTO.setDeleteComment(true);
        complaintModerateDTO.setBlockCommentAuthor(true);
        complaintModerateDTO.setModeratorId(m1UserId);

        ComplaintReadDTO m1ModeratesComplaintReadDTO = performRequest(
                "/complaints/" + firstComplaintId + "/moderate",
                HttpMethod.POST,
                complaintModerateDTO,
                getAuthHeaders(M1_EMAIL, M1_PASSWORD),
                ComplaintReadDTO.class);

        Assert.assertEquals(m1UserId, m1ModeratesComplaintReadDTO.getModeratorId());
        Assert.assertEquals(ComplaintStatus.CLOSED, m1ModeratesComplaintReadDTO.getComplaintStatus());

        // check if comment is not displayed
        PageResult<CommentReadDTO> u3ImportedMovieComments2 = performPageRequest(
                "/movies/" + importedMovieId + "/comments",
                HttpMethod.GET,
                null,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                CommentReadDTO.class);

        Assert.assertEquals(0, u3ImportedMovieComments2.getData().size());

        // check if u1 is blocked
        UserReadDTO u1BlockedUserReadDTO = performRequest(
                "/users/" + u1UserId,
                HttpMethod.GET,
                null,
                getAuthHeaders(ADMIN_EMAIL, ADMIN_PASSWORD),
                UserReadDTO.class);

        Assert.assertTrue(u1BlockedUserReadDTO.getIsBlocked());

        // FINAL_32 m1 opens pending complaints and sees no new complaints
        PageResult<ComplaintReadDTO> m1AllComplaints2 = performPageRequest(
                "/complaints?statuses=INITIATED",
                HttpMethod.GET,
                null,
                getAuthHeaders(M1_EMAIL, M1_PASSWORD),
                ComplaintReadDTO.class);

        Assert.assertEquals(0, m1AllComplaints2.getData().size());

        // FINAL_33 u2 and u3 add ratings to casts of actor2Id in two movies
        // u2 adds rating to cast in movieId
        RatingCreateDTO u2MovieCastRatingCreateDTO = createRatingDTO(8, u2UserId, MOVIE_CAST);

        performRequest(
                "/movies/" + movieId + "/movie-casts/" + movieCastId + "/ratings/",
                HttpMethod.POST,
                u2MovieCastRatingCreateDTO,
                getAuthHeaders(U2_EMAIL, U2_PASSWORD),
                RatingReadDTO.class);

        // u2 adds rating to cast in imported movie
        RatingCreateDTO u2ImportedMovieCastRatingCreateDTO = createRatingDTO(10, u2UserId, MOVIE_CAST);

        performRequest(
                "/movies/" + importedMovieId + "/movie-casts/" + importedMovieCastId + "/ratings/",
                HttpMethod.POST,
                u2ImportedMovieCastRatingCreateDTO,
                getAuthHeaders(U2_EMAIL, U2_PASSWORD),
                RatingReadDTO.class);

        // u3 adds rating to cast in movieId
        RatingCreateDTO u3MovieCastRatingCreateDTO = createRatingDTO(4, u3UserId, MOVIE_CAST);

        performRequest(
                "/movies/" + movieId + "/movie-casts/" + movieCastId + "/ratings/",
                HttpMethod.POST,
                u3MovieCastRatingCreateDTO,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                RatingReadDTO.class);

        // u3 adds rating to cast in imported movie
        RatingCreateDTO u3ImportedMovieCastRatingCreateDTO = createRatingDTO(6, u3UserId, MOVIE_CAST);

        performRequest(
                "/movies/" + importedMovieId + "/movie-casts/" + importedMovieCastId + "/ratings/",
                HttpMethod.POST,
                u3ImportedMovieCastRatingCreateDTO,
                getAuthHeaders(U3_EMAIL, U3_PASSWORD),
                RatingReadDTO.class);

        // FINAL_34 u1 tries to add rating to cast but receives 403 error
        RatingCreateDTO u1ImportedMovieCastRatingCreateDTO = createRatingDTO(8, u1UserId, MOVIE_CAST);

        Assertions.assertThatThrownBy(() -> new RestTemplate().exchange(
                API_URL + "/movies/" + importedMovieId + "/movie-casts/" + importedMovieCastId + "/ratings/",
                HttpMethod.POST,
                new HttpEntity<>(u1ImportedMovieCastRatingCreateDTO, getAuthHeaders(U1_EMAIL, U1_PASSWORD)),
                new ParameterizedTypeReference<RatingReadDTO>() {}))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.UNAUTHORIZED);

        // FINAL_35 unregistered user opens person page and sees average ratings
        // wait and check if updateAverageRating() was invoked
        Thread.sleep(10000);
        await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() ->
                        Mockito.verify(updateAverageRatingOfMovieCastsJob, Mockito.atLeast(3))
                                .updateAverageRatingOfMovieCast());

        Thread.sleep(5000);
        await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() ->
                        Mockito.verify(updateAverageRatingOfPersonMoviesJob, Mockito.atLeast(3))
                                .updateAverageRating());

        Thread.sleep(5000);
        await().atMost(Duration.ofSeconds(20))
                .untilAsserted(() ->
                        Mockito.verify(updateAverageRatingOfPersonRolesJob, Mockito.atLeast(3))
                                .updateAverageRating());

        PersonReadExtendedDTO actorReadDTO = performRequest(
                "/people/" + actor2Id + "/extended/",
                HttpMethod.GET,
                null,
                null,
                PersonReadExtendedDTO.class);

        Assert.assertEquals(2, actorReadDTO.getMovieCasts().size());
        Assert.assertEquals(7.0, actorReadDTO.getAverageRatingByMovies(), Double.MIN_NORMAL);
        Assert.assertEquals(7.0, actorReadDTO.getAverageRatingByRoles(), Double.MIN_NORMAL);
    }

    private <T> T performRequest(
            String url, HttpMethod httpMethod, Object body, HttpHeaders headers, Class<T> responseClass
    ) {
        ResponseEntity<T> response = new RestTemplate().exchange(
                API_URL + url,
                httpMethod,
                new HttpEntity<>(body, headers),
                responseClass);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody();
    }

    private <T> PageResult<T> performPageRequest(
            String url, HttpMethod httpMethod, Object body, HttpHeaders headers, Class<T> responseClass
    ) {
        ResponseEntity<PageResult<T>> response =  new RestTemplate()
                .exchange(API_URL + url,
                        httpMethod,
                        new HttpEntity<>(body, headers),
                        new ParameterizedTypeReference<PageResult<T>>() {
                            public Type getType() {
                                return new MyParameterizedTypeImpl((ParameterizedType) super.getType(),
                                        new Type[] {responseClass});
                            }
                        });

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody();
    }

    private <T> List<T> performListRequest(
            String url, HttpMethod httpMethod, Object body, HttpHeaders headers, Class<T> responseClass
    ) {
        ResponseEntity<List<T>> response = new RestTemplate().exchange(
                API_URL + url,
                httpMethod,
                new HttpEntity<>(body, headers),
                new ParameterizedTypeReference<List<T>>() {
                    public Type getType() {
                        return new MyParameterizedTypeImpl((ParameterizedType) super.getType(),
                                new Type[] {responseClass});
                    }
                });

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        return response.getBody();
    }

    private HttpHeaders getAuthHeaders(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", getBasicAuthorizationHeaderValue(email, password));
        return headers;
    }

    private String getBasicAuthorizationHeaderValue(String email, String password) {
        return "Basic " + new String(Base64.getEncoder()
                .encode(String.format("%s:%s", email, password).getBytes()));
    }

    private RatingCreateDTO createRatingDTO(int rating, UUID authorId, TargetObjectType objectType) {
        RatingCreateDTO dto = new RatingCreateDTO();
        dto.setRating(rating);
        dto.setAuthorId(authorId);
        dto.setRatedObjectType(objectType);
        return dto;
    }

    private MisprintCreateDTO createMisprintDTO(UUID targetObjectId, TargetObjectType objectType) {
        MisprintCreateDTO dto = new MisprintCreateDTO();
        dto.setMisprintText("musicant");
        dto.setReplaceTo(null);
        dto.setTargetObjectType(objectType);
        dto.setTargetObjectId(targetObjectId);
        return dto;
    }

    private LikeCreateDTO createLikeDTO(UUID likedObjectId, TargetObjectType objectType, Boolean meLiked) {
        LikeCreateDTO dto = new LikeCreateDTO();
        dto.setLikedObjectId(likedObjectId);
        dto.setLikedObjectType(objectType);
        dto.setMeLiked(meLiked);
        return dto;
    }

    private ArticleCreateDTO createArticleDTO(UUID c1UserId) {
        ArticleCreateDTO dto = new ArticleCreateDTO();
        dto.setTitle("Green Book review – a bumpy ride through the deep south");
        dto.setText("Mahershala Ali plays a jazz musicant who confronts the racism of his driver,"
                + " played by Viggo Mortensen, in a warm but tentative real-life story");
        dto.setStatus(ArticleStatus.PUBLISHED);
        dto.setAuthorId(c1UserId);
        return dto;
    }

    private GenreCreateDTO createGenreDTO(String name) {
        GenreCreateDTO dto = new GenreCreateDTO();
        dto.setGenreName(name);
        dto.setDescription("Some description");
        return dto;
    }

    private MovieCastCreateDTO createCast1DTO(UUID personId) {
        MovieCastCreateDTO dto = new MovieCastCreateDTO();
        dto.setPersonId(personId);
        dto.setDescription("String and rude bodyguard");
        dto.setCharacter("Tony Lip");
        dto.setGender(Gender.MALE);
        dto.setOrderNumber(1);
        return dto;
    }

    private MovieCastCreateDTO createCast2DTO(UUID personId) {
        MovieCastCreateDTO dto = new MovieCastCreateDTO();
        dto.setPersonId(personId);
        dto.setDescription("Talented famous musician");
        dto.setCharacter("Dr. Donald Shirley");
        dto.setGender(Gender.MALE);
        dto.setOrderNumber(2);
        return dto;
    }

    private MovieCastCreateDTO createCast3DTO(UUID personId) {
        MovieCastCreateDTO dto = new MovieCastCreateDTO();
        dto.setPersonId(personId);
        dto.setDescription("Tony Lip's wife");
        dto.setCharacter("Dolores");
        dto.setGender(Gender.FEMALE);
        dto.setOrderNumber(3);
        return dto;
    }

    private MovieCrewCreateDTO createCrewDTO(UUID personId, MovieCrewType crewType) {
        MovieCrewCreateDTO dto = new MovieCrewCreateDTO();
        dto.setDescription(crewType.toString());
        dto.setPersonId(personId);
        dto.setMovieCrewType(crewType);
        return dto;
    }

    private UserCreateDTO createUserDTO(String email, String password, Gender gender) {
        UserCreateDTO dto = generateObject(UserCreateDTO.class);
        dto.setPassword(password);
        dto.setPasswordConfirmation(password);
        dto.setEmail(email);
        dto.setGender(gender);
        return dto;
    }

    private MovieCreateDTO createMovieDTO() {
        MovieCreateDTO dto = new MovieCreateDTO();
        dto.setMovieTitle("Green Book");
        dto.setDescription("A working-class Italian-American bouncer becomes the driver of an African-American"
                + " classical pianist on a tour of venues through the 1960s American South.");
        dto.setReleaseDate(LocalDate.of(2018, 10, 16));
        dto.setIsReleased(true);
        dto.setRuntime(130);
        dto.setRevenue(329700000);
        return dto;
    }

    private PersonCreateDTO createP1() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Peter");
        dto.setLastName("Farrelly");
        dto.setBio("Peter John Farrelly is a producer and writer, known for Green Book (2018).");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1956, 12, 17));
        dto.setPlaceOfBirth("Phoenixville, Pennsylvania, USA");
        return dto;
    }

    private PersonCreateDTO createP2() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Nick");
        dto.setLastName("Vallelonga");
        dto.setBio("Nick Vallelonga won two Academy Awards and two Golden Globes.");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1959, 9, 13));
        dto.setPlaceOfBirth("Bronx, New York, USA");
        return dto;
    }

    private PersonCreateDTO createP3() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Brian");
        dto.setLastName("Hayes Currie");
        dto.setBio("He is an actor and writer, known for Green Book, Armageddon and Con Air.");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1961, 1, 1));
        dto.setPlaceOfBirth("Unknown");
        return dto;
    }

    private PersonCreateDTO createP4() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Viggo");
        dto.setLastName("Mortensen");
        dto.setBio("Since his screen debut as a young Amish farmer in Peter Weir's Witness (1985), "
                + "Viggo Mortensen's career has been marked by a steady string of well-rounded performances.");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1958, 10, 20));
        dto.setPlaceOfBirth("Manhattan, New York City, New York, USA");
        return dto;
    }

    private PersonCreateDTO createP5() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Mahershala");
        dto.setLastName("Ali");
        dto.setBio("Mahershala Ali is one of the most in-demand faces in Hollywood"
                + " with his extraordinarily diverse skill set and wide-ranging background in film, and theater.");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1974, 2, 16));
        dto.setPlaceOfBirth("Oakland, California, USA");
        return dto;
    }

    private PersonCreateDTO createP6() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Linda");
        dto.setLastName("Cardellini");
        dto.setBio("She is of Italian (from her grandfather), Irish (from her mother), German descent.");
        dto.setGender(Gender.FEMALE);
        dto.setBirthday(LocalDate.of(1975, 7, 25));
        dto.setPlaceOfBirth("Redwood City, California, USA");
        return dto;
    }

    private PersonCreateDTO createP7() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Jim");
        dto.setLastName("Burke");
        dto.setBio("Jim Burke is a producer and actor, known for Green Book (2018)");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1961, 11, 29));
        dto.setPlaceOfBirth("Sacramento, California, USA");
        return dto;
    }

    private PersonCreateDTO createP8() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Kris");
        dto.setLastName("Bowers");
        dto.setBio("Kris Bowers is known for his work on When They See Us (2019), Green Book (2018)");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1989, 11, 29));
        dto.setPlaceOfBirth("Los Angeles, California, USA");
        return dto;
    }

    private PersonCreateDTO createP9() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Betsy");
        dto.setLastName("Heimann");
        dto.setBio("She is known for her work on Almost Famous, Pulp Fiction and Vanilla Sky.");
        dto.setGender(Gender.FEMALE);
        dto.setBirthday(LocalDate.of(1975, 6, 20));
        dto.setPlaceOfBirth("Chicago, Illinois, USA");
        return dto;
    }

    private PersonCreateDTO createP10() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Brock");
        dto.setLastName("Jolet");
        dto.setBio("Brock Jolet is known for his work on Green Book, The Man with the Iron Fists");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1970, 10, 5));
        dto.setPlaceOfBirth("Los Angeles, California, USA");
        return dto;
    }

    private PersonCreateDTO createP11() {
        PersonCreateDTO dto = new PersonCreateDTO();
        dto.setFirstName("Patrick");
        dto.setLastName("J. Don Vito");
        dto.setBio("Patrick J. Don Vito is known for his work on Green Book (2018), National Security (2003)");
        dto.setGender(Gender.MALE);
        dto.setBirthday(LocalDate.of(1980, 4, 16));
        dto.setPlaceOfBirth("Berlin, Germany");
        return dto;
    }
}
