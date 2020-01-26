package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
@Sql(statements = "delete from application_user", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ApplicationUserRepositoryTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Test
    public void testSave() {
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setUsername("Vitalka");
        applicationUser.setPassword("123456");
        applicationUser.setEmail("vetal@gmail.com");
        applicationUser = applicationUserRepository.save(applicationUser);

        assertNotNull(applicationUser.getId());
        assertTrue(applicationUserRepository.findById(applicationUser.getId()).isPresent());
    }
}