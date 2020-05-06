package com.golovko.backend.controller.integration;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.UserRole;
import com.golovko.backend.domain.UserRoleType;
import com.golovko.backend.dto.user.UserCreateDTO;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

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

    private UUID moderatorRoleId;
    private UUID contentManagerRoleId;
    private UUID userRoleId;
    private final String API_URL = "http://localhost:8080/api/v1";
    private final String ADMIN_EMAIL = "admin@mail.com";
    private final String ADMIN_PASSWORD = "admin_password";
    private final String M1_EMAIL = "moderator1@mail.com";
    private final String M1_PASSWORD = "moderator1_password";
    private final String C1_EMAIL = "content_manager1@mail.com";
    private final String C1_PASSWORD = "content_manager1_password";

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

        ResponseEntity<UserReadDTO> m1ReadDTO = new RestTemplate()
                .exchange(API_URL + "/users", HttpMethod.POST,
                        new HttpEntity<>(m1CreateDTO),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, m1ReadDTO.getStatusCode());
        Assertions.assertThat(m1ReadDTO.getBody()).hasNoNullFieldsOrProperties();
        Assertions.assertThat(m1CreateDTO).isEqualToComparingOnlyGivenFields(m1ReadDTO.getBody(),
                "username", "email");
        UUID m1UserId = m1ReadDTO.getBody().getId();

        // FINAL_2 a1 gives m1 the role of moderator
        ResponseEntity<List<UserRoleReadDTO>> m1UserRoles = new RestTemplate()
                .exchange(API_URL + "/users/" + m1UserId + "/roles/" + moderatorRoleId,
                        HttpMethod.POST,
                        new HttpEntity<>(getAdminAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, m1UserRoles.getStatusCode());
        Assert.assertEquals(2, m1UserRoles.getBody().size());
        Assertions.assertThat(m1UserRoles.getBody()).extracting("id")
                .containsExactlyInAnyOrder(userRoleId, moderatorRoleId);

        // FINAL_3 register user c1
        UserCreateDTO c1CreateDTO = createUserC1();

        ResponseEntity<UserReadDTO> c1ReadDTO = new RestTemplate()
                .exchange(API_URL + "/users", HttpMethod.POST,
                        new HttpEntity<>(c1CreateDTO),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, c1ReadDTO.getStatusCode());
        Assertions.assertThat(c1ReadDTO.getBody()).hasNoNullFieldsOrProperties();
        Assertions.assertThat(c1CreateDTO).isEqualToComparingOnlyGivenFields(c1ReadDTO.getBody(),
                "username", "email");
        UUID c1UserId = c1ReadDTO.getBody().getId();

        // FINAL_4 a1 gives c1 the role of content manager
        ResponseEntity<List<UserRoleReadDTO>> c1UserRoles = new RestTemplate()
                .exchange(API_URL + "/users/" + c1UserId + "/roles/" + contentManagerRoleId,
                        HttpMethod.POST,
                        new HttpEntity<>(getAdminAuthHeaders()),
                        new ParameterizedTypeReference<>() {});

        Assert.assertEquals(HttpStatus.OK, c1UserRoles.getStatusCode());
        Assert.assertEquals(2, c1UserRoles.getBody().size());
        Assertions.assertThat(c1UserRoles.getBody()).extracting("id")
                .containsExactlyInAnyOrder(userRoleId, contentManagerRoleId);
    }

    private HttpHeaders getAdminAuthHeaders() {
        HttpHeaders adminAuthHeaders = new HttpHeaders();
        adminAuthHeaders.add("Authorization", getBasicAuthorizationHeaderValue(ADMIN_EMAIL, ADMIN_PASSWORD));
        return adminAuthHeaders;
    }

    private HttpHeaders getM1AuthHeaders() {
        HttpHeaders m1AuthHeaders = new HttpHeaders();
        m1AuthHeaders.add("Authorization", getBasicAuthorizationHeaderValue(M1_EMAIL, M1_PASSWORD));
        return m1AuthHeaders;
    }

    private HttpHeaders getC1AuthHeaders() {
        HttpHeaders c1AuthHeaders = new HttpHeaders();
        c1AuthHeaders.add("Authorization", getBasicAuthorizationHeaderValue(C1_EMAIL, C1_PASSWORD));
        return c1AuthHeaders;
    }

    private UserCreateDTO createUserM1() {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("moderator_david");
        createDTO.setPassword(M1_PASSWORD);
        createDTO.setPasswordConfirmation(M1_PASSWORD);
        createDTO.setEmail(M1_EMAIL);
        return createDTO;
    }

    private UserCreateDTO createUserC1() {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("content_manager1");
        createDTO.setPassword(C1_PASSWORD);
        createDTO.setPasswordConfirmation(C1_PASSWORD);
        createDTO.setEmail(C1_EMAIL);
        return createDTO;
    }

    private String getBasicAuthorizationHeaderValue(String email, String password) {
        return "Basic " + new String(Base64.getEncoder()
                .encode(String.format("%s:%s", email, password).getBytes()));
    }
}
