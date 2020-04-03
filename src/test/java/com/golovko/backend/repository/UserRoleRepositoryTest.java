package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.UserRole;
import com.golovko.backend.domain.UserRoleType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

public class UserRoleRepositoryTest extends BaseTest {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    public void testGetUserRoleByType() {
        UUID userRoleId = UUID.fromString("d1931edd-f5bb-4366-b3cf-906988c83eeb"); // USER

        UUID actualId = userRoleRepository.findUserRoleIdByType(UserRoleType.USER);

        Assert.assertEquals(userRoleId, actualId);
    }

    @Test
    public void testFindByType() {
        UserRoleType expectedType = UserRoleType.USER;

        UserRole userRole = userRoleRepository.findByType(expectedType);

        Assert.assertNotNull(userRole);
        Assert.assertEquals(expectedType, userRole.getType());
    }
}
