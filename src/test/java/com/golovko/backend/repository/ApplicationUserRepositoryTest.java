package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ApplicationUserRepositoryTest extends BaseTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

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