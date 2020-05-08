package com.golovko.backend.controller.integration;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.article.ArticleCreateDTO;
import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.article.ArticleReadExtendedDTO;
import com.golovko.backend.dto.genre.GenreCreateDTO;
import com.golovko.backend.dto.genre.GenreReadDTO;
import com.golovko.backend.dto.like.LikeCreateDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.dto.movie.MovieCreateDTO;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.dto.moviecast.MovieCastCreateDTO;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewCreateDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewReadDTO;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.dto.user.UserCreateDTO;
import com.golovko.backend.dto.user.UserPatchDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.dto.userrole.UserRoleReadDTO;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.UserRoleRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@ActiveProfiles({"another-profile"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class WorkingScenarioIntegrationTest extends BaseTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static UUID moderatorRoleId;
    private static UUID contentManagerRoleId;
    private static UUID userRoleId;
    private final String API_URL = "http://localhost:8080/api/v1";
    private final String ADMIN_EMAIL = "admin@mail.com";
    private final String ADMIN_PASSWORD = "admin_password";
    private final String M1_EMAIL = "moderator1@mail.com";
    private final String M1_PASSWORD = "moderator1_password";
    private final String C1_EMAIL = "content_manager1@mail.com";
    private final String C1_PASSWORD = "content_manager1_password";
    private final String U1_PASSWORD = "user1_password";
    private final String U1_EMAIL = "user1@mail.com";
    private final String U2_PASSWORD = "user2_password";
    private final String U2_EMAIL = "user2@mail.com";
    private final String U3_PASSWORD = "user3_password";
    private final String U3_EMAIL = "user3@mail.com";

    @Before
    public void setup() {
        ApplicationUser a1 = new ApplicationUser();
        a1.setUsername("super_admin");
        a1.setTrustLevel(10.0);
        a1.setIsBlocked(false);
        a1.setEmail(ADMIN_EMAIL);
        a1.setEncodedPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        UserRole userRole = userRoleRepository.findByType(UserRoleType.ADMIN);
        a1.getUserRoles().add(userRole);
        applicationUserRepository.save(a1);

        moderatorRoleId = userRoleRepository.findByType(UserRoleType.MODERATOR).getId();
        contentManagerRoleId = userRoleRepository.findByType(UserRoleType.CONTENT_MANAGER).getId();
        userRoleId = userRoleRepository.findByType(UserRoleType.USER).getId();
    }

    @Test
    public void testWorkingScenario() {
        // check if repo contains only 1 record
        Assert.assertEquals(1, applicationUserRepository.count());
        // check if user has ADMIN authority
        ApplicationUser a1 = applicationUserRepository.findByEmail(ADMIN_EMAIL);
        Assert.assertEquals(UserRoleType.ADMIN, a1.getUserRoles().get(0).getType());

        // FINAL_1 register user m1
        UserCreateDTO m1CreateDTO = createUserM1();

        ResponseEntity<UserReadDTO> m1ReadDTOResponse = new RestTemplate()
                .exchange(API_URL + "/users", HttpMethod.POST,
                        new HttpEntity<>(m1CreateDTO),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, m1ReadDTOResponse.getStatusCode());
        Assertions.assertThat(m1ReadDTOResponse.getBody()).hasNoNullFieldsOrProperties();
        Assertions.assertThat(m1CreateDTO).isEqualToComparingOnlyGivenFields(m1ReadDTOResponse.getBody(),
                "username", "email");
        UUID m1UserId = m1ReadDTOResponse.getBody().getId();

        // FINAL_2 a1 gives m1 the role of moderator
        ResponseEntity<List<UserRoleReadDTO>> m1UserRolesResponse = new RestTemplate()
                .exchange(API_URL + "/users/" + m1UserId + "/roles/" + moderatorRoleId,
                        HttpMethod.POST,
                        new HttpEntity<>(getAuthHeaders(ADMIN_EMAIL, ADMIN_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, m1UserRolesResponse.getStatusCode());
        Assert.assertEquals(2, m1UserRolesResponse.getBody().size());
        Assertions.assertThat(m1UserRolesResponse.getBody()).extracting("id")
                .containsExactlyInAnyOrder(userRoleId, moderatorRoleId);

        // FINAL_3 register user c1
        UserCreateDTO c1CreateDTO = createUserC1();

        ResponseEntity<UserReadDTO> c1ReadDTOResponse = new RestTemplate()
                .exchange(API_URL + "/users", HttpMethod.POST,
                        new HttpEntity<>(c1CreateDTO),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, c1ReadDTOResponse.getStatusCode());
        Assertions.assertThat(c1ReadDTOResponse.getBody()).hasNoNullFieldsOrProperties();
        Assertions.assertThat(c1CreateDTO).isEqualToComparingOnlyGivenFields(c1ReadDTOResponse.getBody(),
                "username", "email");
        UUID c1UserId = c1ReadDTOResponse.getBody().getId();

        // FINAL_4 a1 gives c1 the role of content manager
        ResponseEntity<List<UserRoleReadDTO>> c1UserRolesResponse = new RestTemplate()
                .exchange(API_URL + "/users/" + c1UserId + "/roles/" + contentManagerRoleId,
                        HttpMethod.POST,
                        new HttpEntity<>(getAuthHeaders(ADMIN_EMAIL, ADMIN_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, c1UserRolesResponse.getStatusCode());
        Assert.assertEquals(2, c1UserRolesResponse.getBody().size());
        Assertions.assertThat(c1UserRolesResponse.getBody()).extracting("id")
                .containsExactlyInAnyOrder(userRoleId, contentManagerRoleId);

        // FINAL_5 Three regular users are registered. Men: u1, u2 and woman: u3
        // register u1
        UserCreateDTO u1CreateDTO = createUserU1();

        ResponseEntity<UserReadDTO> u1ReadDTOResponse = new RestTemplate()
                .exchange(API_URL + "/users", HttpMethod.POST,
                        new HttpEntity<>(u1CreateDTO),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, u1ReadDTOResponse.getStatusCode());
        Assertions.assertThat(u1ReadDTOResponse.getBody()).hasNoNullFieldsOrProperties();
        Assertions.assertThat(u1CreateDTO).isEqualToComparingOnlyGivenFields(u1ReadDTOResponse.getBody(),
                "username", "email", "gender");
        UUID u1UserId = u1ReadDTOResponse.getBody().getId();

        // register u2
        UserCreateDTO u2CreateDTO = createUserU2();

        ResponseEntity<UserReadDTO> u2ReadDTOResponse = new RestTemplate()
                .exchange(API_URL + "/users", HttpMethod.POST,
                        new HttpEntity<>(u2CreateDTO),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, u2ReadDTOResponse.getStatusCode());
        Assertions.assertThat(u2ReadDTOResponse.getBody()).hasNoNullFieldsOrProperties();
        Assertions.assertThat(u2CreateDTO).isEqualToComparingOnlyGivenFields(u2ReadDTOResponse.getBody(),
                "username", "email", "gender");
        UUID u2UserId = u2ReadDTOResponse.getBody().getId();

        // register u3
        UserCreateDTO u3CreateDTO = createUserU3();

        ResponseEntity<UserReadDTO> u3ReadDTOResponse = new RestTemplate()
                .exchange(API_URL + "/users", HttpMethod.POST,
                        new HttpEntity<>(u3CreateDTO),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, u3ReadDTOResponse.getStatusCode());
        Assertions.assertThat(u3ReadDTOResponse.getBody()).hasNoNullFieldsOrProperties();
        Assertions.assertThat(u3CreateDTO).isEqualToComparingOnlyGivenFields(u3ReadDTOResponse.getBody(),
                "username", "email", "gender");
        UUID u3UserId = u3ReadDTOResponse.getBody().getId();

        // FINAL_6 u2 changes the name in his profile.
        UserPatchDTO u2PatchDTO = new UserPatchDTO();
        u2PatchDTO.setUsername("new_username");
        u2PatchDTO.setPassword(U2_PASSWORD);
        u2PatchDTO.setPasswordConfirmation(U2_PASSWORD);

        ResponseEntity<UserReadDTO> u2UpdatedReadDTOResponse = new RestTemplate(new HttpComponentsClientHttpRequestFactory())
                .exchange(API_URL + "/users/" + u2UserId, HttpMethod.PATCH,
                        new HttpEntity<>(u2PatchDTO, getAuthHeaders(U2_EMAIL, U2_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, u2UpdatedReadDTOResponse.getStatusCode());
        Assert.assertEquals(u2PatchDTO.getUsername(), u2UpdatedReadDTOResponse.getBody().getUsername());

        // FINAL_7 c1 creates a film, characters, actors, crew and others

        // create movie
        MovieCreateDTO movieCreateDTO = createMovieDTO();

        ResponseEntity<MovieReadDTO> movieReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/",
                        new HttpEntity<>(movieCreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, movieReadDTOResponse.getStatusCode());
        Assertions.assertThat(movieCreateDTO).isEqualToIgnoringNullFields(movieReadDTOResponse.getBody());
        UUID movieId = movieReadDTOResponse.getBody().getId();

        // create genres
        GenreCreateDTO biographyGenre = createGenreDTO("Biography");
        GenreCreateDTO comedyGenre = createGenreDTO("Comedy");
        GenreCreateDTO dramaGenre = createGenreDTO("Drama");

        ResponseEntity<GenreReadDTO> g1ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/genres/",
                        new HttpEntity<>(biographyGenre, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        GenreReadDTO.class);
        UUID biographyGenreId = g1ReadDTOResponse.getBody().getId();

        ResponseEntity<GenreReadDTO> g2ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/genres/",
                        new HttpEntity<>(comedyGenre, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        GenreReadDTO.class);
        UUID comedyGenreId = g2ReadDTOResponse.getBody().getId();

        ResponseEntity<GenreReadDTO> g3ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/genres/",
                        new HttpEntity<>(dramaGenre, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        GenreReadDTO.class);
        UUID dramaGenreId = g3ReadDTOResponse.getBody().getId();

        // add genres to movie
        new RestTemplate()
                .exchange(API_URL + "/movies/" + movieId + "/genres/" + biographyGenreId,
                        HttpMethod.POST,
                        new HttpEntity<>(getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        new RestTemplate()
                .exchange(API_URL + "/movies/" + movieId + "/genres/" + comedyGenreId,
                        HttpMethod.POST,
                        new HttpEntity<>(getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        ResponseEntity<List<GenreReadDTO>> movieGenreResponse = new RestTemplate()
                .exchange(API_URL + "/movies/" + movieId + "/genres/" + dramaGenreId,
                        HttpMethod.POST,
                        new HttpEntity<>(getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(3, movieGenreResponse.getBody().size());
        Assertions.assertThat(movieGenreResponse.getBody()).extracting("id")
                .containsExactlyInAnyOrder(biographyGenreId, comedyGenreId, dramaGenreId);

        // create person director
        PersonCreateDTO p1CreateDTO = createP1();

        ResponseEntity<PersonReadDTO> p1ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p1CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p1ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p1ReadDTOResponse.getBody()).isEqualToIgnoringNullFields(p1ReadDTOResponse.getBody());
        UUID directorId = p1ReadDTOResponse.getBody().getId();

        // create crew director
        MovieCrewCreateDTO crew1CreateDTO = createCrewDTO(directorId, MovieCrewType.DIRECTOR);

        ResponseEntity<MovieCrewReadDTO> crew1ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-crews/",
                        new HttpEntity<>(crew1CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCrewReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, crew1ReadDTOResponse.getStatusCode());
        Assertions.assertThat(crew1CreateDTO).isEqualToIgnoringNullFields(crew1ReadDTOResponse.getBody());
        UUID crew1Id = crew1ReadDTOResponse.getBody().getId();

        // create person writer1
        PersonCreateDTO p2CreateDTO = createP2();

        ResponseEntity<PersonReadDTO> p2ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p2CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p2ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p2CreateDTO).isEqualToIgnoringNullFields(p2ReadDTOResponse.getBody());
        UUID writer1Id = p2ReadDTOResponse.getBody().getId();

        // create crew writer1
        MovieCrewCreateDTO crew2CreateDTO = createCrewDTO(writer1Id, MovieCrewType.WRITER);

        ResponseEntity<MovieCrewReadDTO> crew2ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-crews/",
                        new HttpEntity<>(crew2CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCrewReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, crew2ReadDTOResponse.getStatusCode());
        Assertions.assertThat(crew2CreateDTO).isEqualToIgnoringNullFields(crew2ReadDTOResponse.getBody());
        UUID crew2Id = crew2ReadDTOResponse.getBody().getId();

        // create person writer
        PersonCreateDTO p3CreateDTO = createP3();

        ResponseEntity<PersonReadDTO> p3ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p3CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p3ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p3CreateDTO).isEqualToIgnoringNullFields(p3ReadDTOResponse.getBody());
        UUID writer2Id = p3ReadDTOResponse.getBody().getId();

        // create crew writer2
        MovieCrewCreateDTO crew3CreateDTO = createCrewDTO(writer2Id, MovieCrewType.WRITER);

        ResponseEntity<MovieCrewReadDTO> crew3ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-crews/",
                        new HttpEntity<>(crew3CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCrewReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, crew3ReadDTOResponse.getStatusCode());
        Assertions.assertThat(crew3CreateDTO).isEqualToIgnoringNullFields(crew3ReadDTOResponse.getBody());
        UUID crew3Id = crew3ReadDTOResponse.getBody().getId();

        // create actor
        PersonCreateDTO p4CreateDTO = createP4();

        ResponseEntity<PersonReadDTO> p4ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p4CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p4ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p4CreateDTO).isEqualToIgnoringNullFields(p4ReadDTOResponse.getBody());
        UUID actor1Id = p4ReadDTOResponse.getBody().getId();

        // create cast
        MovieCastCreateDTO cast1CreateDTO = createCast1DTO(actor1Id);

        ResponseEntity<MovieCastReadDTO> cast1ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-casts/",
                        new HttpEntity<>(cast1CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCastReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, cast1ReadDTOResponse.getStatusCode());
        Assertions.assertThat(cast1CreateDTO).isEqualToIgnoringNullFields(cast1ReadDTOResponse.getBody());
        UUID cast1Id = cast1ReadDTOResponse.getBody().getId();

        // create person actor
        PersonCreateDTO p5CreateDTO = createP5();

        ResponseEntity<PersonReadDTO> p5ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p5CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p5ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p5CreateDTO).isEqualToIgnoringNullFields(p5ReadDTOResponse.getBody());
        UUID actor2Id = p5ReadDTOResponse.getBody().getId();

        // create cast
        MovieCastCreateDTO cast2CreateDTO = createCast2DTO(actor2Id);

        ResponseEntity<MovieCastReadDTO> cast2ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-casts/",
                        new HttpEntity<>(cast2CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCastReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, cast2ReadDTOResponse.getStatusCode());
        Assertions.assertThat(cast2CreateDTO).isEqualToIgnoringNullFields(cast2ReadDTOResponse.getBody());
        UUID cast2Id = cast2ReadDTOResponse.getBody().getId();

        // create person actor
        PersonCreateDTO p6CreateDTO = createP6();

        ResponseEntity<PersonReadDTO> p6ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p6CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p6ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p6CreateDTO).isEqualToIgnoringNullFields(p6ReadDTOResponse.getBody());
        UUID actor3Id = p6ReadDTOResponse.getBody().getId();

        // create cast
        MovieCastCreateDTO cast3CreateDTO = createCast3DTO(actor3Id);

        ResponseEntity<MovieCastReadDTO> cast3ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-casts/",
                        new HttpEntity<>(cast3CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCastReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, cast3ReadDTOResponse.getStatusCode());
        Assertions.assertThat(cast3CreateDTO).isEqualToIgnoringNullFields(cast3ReadDTOResponse.getBody());
        UUID cast3Id = cast3ReadDTOResponse.getBody().getId();

        // create person producer
        PersonCreateDTO p7CreateDTO = createP7();

        ResponseEntity<PersonReadDTO> p7ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p7CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p7ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p7CreateDTO).isEqualToIgnoringNullFields(p7ReadDTOResponse.getBody());
        UUID producerId = p7ReadDTOResponse.getBody().getId();

        // create crew producer
        MovieCrewCreateDTO crew4CreateDTO = createCrewDTO(producerId, MovieCrewType.PRODUCER);

        ResponseEntity<MovieCrewReadDTO> crew4ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-crews/",
                        new HttpEntity<>(crew4CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCrewReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, crew4ReadDTOResponse.getStatusCode());
        Assertions.assertThat(crew4CreateDTO).isEqualToIgnoringNullFields(crew4ReadDTOResponse.getBody());
        UUID crew4Id = crew4ReadDTOResponse.getBody().getId();

        // create person composer
        PersonCreateDTO p8CreateDTO = createP8();

        ResponseEntity<PersonReadDTO> p8ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p8CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p8ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p8CreateDTO).isEqualToIgnoringNullFields(p8ReadDTOResponse.getBody());
        UUID composerId = p8ReadDTOResponse.getBody().getId();

        // create crew composer
        MovieCrewCreateDTO crew5CreateDTO = createCrewDTO(composerId, MovieCrewType.SOUND);

        ResponseEntity<MovieCrewReadDTO> crew5ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-crews/",
                        new HttpEntity<>(crew5CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCrewReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, crew5ReadDTOResponse.getStatusCode());
        Assertions.assertThat(crew5CreateDTO).isEqualToIgnoringNullFields(crew5ReadDTOResponse.getBody());
        UUID crew5Id = crew5ReadDTOResponse.getBody().getId();

        // create person costume designer
        PersonCreateDTO p9CreateDTO = createP9();

        ResponseEntity<PersonReadDTO> p9ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p9CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p9ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p9CreateDTO).isEqualToIgnoringNullFields(p9ReadDTOResponse.getBody());
        UUID costumeDesignerId = p9ReadDTOResponse.getBody().getId();

        // create crew costume designer
        MovieCrewCreateDTO crew6CreateDTO = createCrewDTO(costumeDesignerId, MovieCrewType.COSTUME_DESIGNER);

        ResponseEntity<MovieCrewReadDTO> crew6ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-crews/",
                        new HttpEntity<>(crew6CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCrewReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, crew6ReadDTOResponse.getStatusCode());
        Assertions.assertThat(crew6CreateDTO).isEqualToIgnoringNullFields(crew6ReadDTOResponse.getBody());
        UUID crew6Id = crew6ReadDTOResponse.getBody().getId();

        // create visual effects designer
        PersonCreateDTO p10CreateDTO = createP10();

        ResponseEntity<PersonReadDTO> p10ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p10CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p10ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p10CreateDTO).isEqualToIgnoringNullFields(p10ReadDTOResponse.getBody());
        UUID visualEffectsDesignerId = p10ReadDTOResponse.getBody().getId();

        // create crew visual effects designer
        MovieCrewCreateDTO crew7CreateDTO = createCrewDTO(visualEffectsDesignerId, MovieCrewType.VISUAL_EFFECTS);

        ResponseEntity<MovieCrewReadDTO> crew7ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-crews/",
                        new HttpEntity<>(crew7CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCrewReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, crew7ReadDTOResponse.getStatusCode());
        Assertions.assertThat(crew7CreateDTO).isEqualToIgnoringNullFields(crew7ReadDTOResponse.getBody());
        UUID crew7Id = crew7ReadDTOResponse.getBody().getId();

        // create person editor
        PersonCreateDTO p11CreateDTO = createP11();

        ResponseEntity<PersonReadDTO> p11ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/people/",
                        new HttpEntity<>(p11CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        PersonReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, p11ReadDTOResponse.getStatusCode());
        Assertions.assertThat(p11CreateDTO).isEqualToIgnoringNullFields(p11ReadDTOResponse.getBody());
        UUID editorId = p11ReadDTOResponse.getBody().getId();

        // create crew editor
        MovieCrewCreateDTO crew8CreateDTO = createCrewDTO(editorId, MovieCrewType.VISUAL_EFFECTS);

        ResponseEntity<MovieCrewReadDTO> crew8ReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/movies/" + movieId + "/movie-crews/",
                        new HttpEntity<>(crew8CreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        MovieCrewReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, crew8ReadDTOResponse.getStatusCode());
        Assertions.assertThat(crew8CreateDTO).isEqualToIgnoringNullFields(crew8ReadDTOResponse.getBody());
        UUID crew8Id = crew8ReadDTOResponse.getBody().getId();

        // FINAL_8 c1 add news about movie
        ArticleCreateDTO articleCreateDTO = createArticleDTO(c1UserId);

        ResponseEntity<ArticleReadDTO> articleReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/articles/",
                        new HttpEntity<>(articleCreateDTO, getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        ArticleReadDTO.class);

        Assert.assertEquals(HttpStatus.OK, articleReadDTOResponse.getStatusCode());
        UUID articleId = articleReadDTOResponse.getBody().getId();

        // add related movie to article
        ResponseEntity<List<MovieReadDTO>> relatedMoviesResponse = new RestTemplate()
                .exchange(API_URL + "/articles/" + articleId + "/movies/" + movieId,
                        HttpMethod.POST,
                        new HttpEntity<>(getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(1, relatedMoviesResponse.getBody().size());
        Assertions.assertThat(relatedMoviesResponse.getBody()).extracting("id").contains(movieId);

        // add related people to article
        new RestTemplate()
                .exchange(API_URL + "/articles/" + articleId + "/people/" + actor1Id,
                        HttpMethod.POST,
                        new HttpEntity<>(getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        new RestTemplate()
                .exchange(API_URL + "/articles/" + articleId + "/people/" + actor2Id,
                        HttpMethod.POST,
                        new HttpEntity<>(getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        ResponseEntity<List<PersonReadDTO>> relatedPeopleResponse = new RestTemplate()
                .exchange(API_URL + "/articles/" + articleId + "/people/" + actor3Id,
                        HttpMethod.POST,
                        new HttpEntity<>(getAuthHeaders(C1_EMAIL, C1_PASSWORD)),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(3, relatedPeopleResponse.getBody().size());
        Assertions.assertThat(relatedPeopleResponse.getBody()).extracting("id")
                .containsExactlyInAnyOrder(actor1Id, actor2Id, actor3Id);

        // FINAL_9 u1 opens article
        ResponseEntity<ArticleReadExtendedDTO> articleReadExtendedDTOResponse = new RestTemplate()
                .exchange(API_URL + "/articles/" + articleId + "/extended/",
                        HttpMethod.GET,
                        new HttpEntity<>(getAuthHeaders(U1_EMAIL, U1_PASSWORD)),
                        ArticleReadExtendedDTO.class);

        Assert.assertEquals(articleCreateDTO.getTitle(), articleReadExtendedDTOResponse.getBody().getTitle());
        Assert.assertEquals(1, articleReadExtendedDTOResponse.getBody().getMovies().size());
        Assert.assertEquals(3, articleReadExtendedDTOResponse.getBody().getPeople().size());

        // FINAL_10 u1 and u2 add likes to article
        LikeCreateDTO likeCreateDTO = createLikeDTO(articleId, TargetObjectType.ARTICLE, true);

        new RestTemplate().postForEntity(API_URL + "/users/" + u1UserId + "/likes/",
                new HttpEntity<>(likeCreateDTO, getAuthHeaders(U1_EMAIL, U1_PASSWORD)),
                LikeReadDTO.class);

        new RestTemplate().postForEntity(API_URL + "/users/" + u2UserId + "/likes/",
                new HttpEntity<>(likeCreateDTO, getAuthHeaders(U2_EMAIL, U2_PASSWORD)),
                LikeReadDTO.class);

        ResponseEntity<ArticleReadDTO> updatedArticleReadDTOResponse = new RestTemplate()
                .getForEntity(API_URL + "/articles/" + articleId, ArticleReadDTO.class);

        // TODO check countOfLikes for article
        Assert.assertEquals((Integer) 2, updatedArticleReadDTOResponse.getBody().getLikesCount());

        // FINAL_11 User u3 - mistakenly adds like to article, and then cancels it
        // u3 add like
        ResponseEntity<LikeReadDTO> u3LikeReadDTOResponse = new RestTemplate()
                .postForEntity(API_URL + "/users/" + u3UserId + "/likes/",
                        new HttpEntity<>(likeCreateDTO, getAuthHeaders(U3_EMAIL, U3_PASSWORD)),
                        LikeReadDTO.class);

        ResponseEntity<ArticleReadDTO> updated1ArticleReadDTOResponse = new RestTemplate()
                .getForEntity(API_URL + "/articles/" + articleId, ArticleReadDTO.class);
         Assert.assertEquals((Integer) 3, updated1ArticleReadDTOResponse.getBody().getLikesCount());

        // u3 delete her like
        ResponseEntity<Void> deleteLikeResponse = new RestTemplate()
                .exchange(API_URL + "/users/" + u3UserId + "/likes/" + u3LikeReadDTOResponse.getBody().getId(),
                        HttpMethod.DELETE,
                        new HttpEntity<>(getAuthHeaders(U3_EMAIL, U3_PASSWORD)),
                        Void.class);

        Assert.assertEquals(HttpStatus.OK, deleteLikeResponse.getStatusCode());

        ResponseEntity<ArticleReadDTO> updated2ArticleReadDTOResponse = new RestTemplate()
                .getForEntity(API_URL + "/articles/" + articleId, ArticleReadDTO.class);
         Assert.assertEquals((Integer) 2, updated2ArticleReadDTOResponse.getBody().getLikesCount());

        // FINAL_12 user u3 add dislike to article
        LikeCreateDTO dislikeCreateDTO = createLikeDTO(articleId, TargetObjectType.ARTICLE, false);

        new RestTemplate().postForEntity(API_URL + "/users/" + u3UserId + "/likes/",
                new HttpEntity<>(dislikeCreateDTO, getAuthHeaders(U3_EMAIL, U3_PASSWORD)),
                LikeReadDTO.class);

        ResponseEntity<ArticleReadDTO> updated3ArticleReadDTOResponse = new RestTemplate()
                .getForEntity(API_URL + "/articles/" + articleId, ArticleReadDTO.class);
         Assert.assertEquals((Integer) 1, updated3ArticleReadDTOResponse.getBody().getDislikesCount());
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
        dto.setText("Mahershala Ali plays a jazz musician who confronts the racism of his driver,"
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

    private HttpHeaders getAuthHeaders(String email, String password) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", getBasicAuthorizationHeaderValue(email, password));
        return headers;
    }

    private String getBasicAuthorizationHeaderValue(String email, String password) {
        return "Basic " + new String(Base64.getEncoder()
                .encode(String.format("%s:%s", email, password).getBytes()));
    }

    private UserCreateDTO createUserM1() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("moderator_david");
        dto.setPassword(M1_PASSWORD);
        dto.setPasswordConfirmation(M1_PASSWORD);
        dto.setEmail(M1_EMAIL);
        dto.setGender(Gender.FEMALE);
        return dto;
    }

    private UserCreateDTO createUserC1() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("content_manager1");
        dto.setPassword(C1_PASSWORD);
        dto.setPasswordConfirmation(C1_PASSWORD);
        dto.setEmail(C1_EMAIL);
        dto.setGender(Gender.FEMALE);
        return dto;
    }

    private UserCreateDTO createUserU1() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("user1");
        dto.setPassword(U1_PASSWORD);
        dto.setPasswordConfirmation(U1_PASSWORD);
        dto.setEmail(U1_EMAIL);
        dto.setGender(Gender.MALE);
        return dto;
    }

    private UserCreateDTO createUserU2() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("user2");
        dto.setPassword(U2_PASSWORD);
        dto.setPasswordConfirmation(U2_PASSWORD);
        dto.setEmail(U2_EMAIL);
        dto.setGender(Gender.MALE);
        return dto;
    }

    private UserCreateDTO createUserU3() {
        UserCreateDTO dto = new UserCreateDTO();
        dto.setUsername("user3");
        dto.setPassword(U3_PASSWORD);
        dto.setPasswordConfirmation(U3_PASSWORD);
        dto.setEmail(U3_EMAIL);
        dto.setGender(Gender.FEMALE);
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
        dto.setFirstName("Ali");
        dto.setLastName("Mahershala");
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
