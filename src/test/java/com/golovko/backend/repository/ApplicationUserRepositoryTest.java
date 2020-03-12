package com.golovko.backend.repository;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.util.TestObjectFactory;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Instant;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@SpringBootTest
@Sql(statements = {"delete from user_role", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ApplicationUserRepositoryTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Test
    public void testSaveUser() {
        ApplicationUser user = testObjectFactory.createUser();

        assertNotNull(user.getId());
        assertTrue(applicationUserRepository.findById(user.getId()).isPresent());
    }

    @Test
    public void testCreatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();

        Instant createdAtBeforeReload = user.getCreatedAt();
        Assert.assertNotNull(createdAtBeforeReload);

        user = applicationUserRepository.findById(user.getId()).get();

        Instant createdAtAfterReload = user.getCreatedAt();
        Assert.assertNotNull(createdAtAfterReload);
        Assert.assertEquals(createdAtBeforeReload, createdAtAfterReload);
    }

    @Test
    public void testUpdatedAtIsSet() {
        ApplicationUser user = testObjectFactory.createUser();

        Instant modifiedAtBeforeReload = user.getUpdatedAt();
        Assert.assertNotNull(modifiedAtBeforeReload);

        user = applicationUserRepository.findById(user.getId()).get();
        user.setEmail("new.user@email.com");
        user = applicationUserRepository.save(user);
        Instant modifiedAtAfterReload = user.getUpdatedAt();

        Assert.assertNotNull(modifiedAtAfterReload);
        Assert.assertTrue(modifiedAtBeforeReload.isBefore(modifiedAtAfterReload));
    }
}