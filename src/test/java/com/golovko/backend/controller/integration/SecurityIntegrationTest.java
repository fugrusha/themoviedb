package com.golovko.backend.controller.integration;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.movie.MovieReadDTO;
import com.golovko.backend.repository.ApplicationUserRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.postgresql.shaded.com.ongres.scram.common.bouncycastle.base64.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@ActiveProfiles({"another-profile"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class SecurityIntegrationTest extends BaseTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testHealthNoSecurity() {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<Void> response = restTemplate.getForEntity("http://localhost:8080/health", Void.class);

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Value("${spring.profiles.active:}")
    private String activeProfiles;

    @Test
    public void testGetActiveProfiles() {

        for (String profileName : activeProfiles.split(",")) {
            System.out.println("Currently active profile - " + profileName);
        }

    }

    @Test
    public void testGetMoviesNoSecurity() {
        RestTemplate restTemplate = new RestTemplate();
        Assertions.assertThatThrownBy(() -> restTemplate.exchange(
                "http://localhost:8080/api/v1/movies", HttpMethod.GET, HttpEntity.EMPTY,
                new ParameterizedTypeReference<PageResult<MovieReadDTO>>() {
                }))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testGetMovies() {
        String email = "test@email.com";
        String password = "pass123";

        ApplicationUser user = testObjectFactory.createUser();
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));
        applicationUserRepository.save(user);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", getBasicAuthorizationHeaderValue(email, password));
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<PageResult<MovieReadDTO>> response = restTemplate.exchange(
                "http://localhost:8080/api/v1/movies", HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<>() {
                });

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void testGetMoviesWrongUser() {
        String email = "test@email.com";
        String password = "pass123";

        ApplicationUser user = testObjectFactory.createUser();
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));
        applicationUserRepository.save(user);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", getBasicAuthorizationHeaderValue("wrong user", password));
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        Assertions.assertThatThrownBy(() -> restTemplate.exchange(
                "http://localhost:8080/api/v1/movies", HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<PageResult<MovieReadDTO>>() {
                }))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testGetMoviesWrongPassword() {
        String email = "test@email.com";
        String password = "pass123";

        ApplicationUser user = testObjectFactory.createUser();
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));
        applicationUserRepository.save(user);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", getBasicAuthorizationHeaderValue(email, "wrong password"));
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        Assertions.assertThatThrownBy(() -> restTemplate.exchange(
                "http://localhost:8080/api/v1/movies", HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<PageResult<MovieReadDTO>>() {
                }))
                .isInstanceOf(HttpClientErrorException.class)
                .extracting("statusCode").isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    public void testGetMoviesNoSession() {
        String email = "test@email.com";
        String password = "pass123";

        ApplicationUser user = testObjectFactory.createUser();
        user.setEmail(email);
        user.setEncodedPassword(passwordEncoder.encode(password));
        applicationUserRepository.save(user);

        RestTemplate restTemplate = new RestTemplate();

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", getBasicAuthorizationHeaderValue(email, password));
        HttpEntity<?> httpEntity = new HttpEntity<>(headers);

        ResponseEntity<PageResult<MovieReadDTO>> response = restTemplate.exchange(
                "http://localhost:8080/api/v1/movies", HttpMethod.GET, httpEntity,
                new ParameterizedTypeReference<>() {
                });

        Assert.assertEquals(HttpStatus.OK, response.getStatusCode());

        Assert.assertNull(response.getHeaders().get("Set_cookie"));
    }

    private String getBasicAuthorizationHeaderValue(String email, String password) {
        return "Basic " + new String(Base64.encode(String.format("%s:%s", email, password).getBytes()));
    }
}
