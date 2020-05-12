package com.golovko.backend.repository;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.UserRole;
import com.golovko.backend.domain.UserRoleType;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.UUID;

import static com.golovko.backend.domain.UserRoleType.*;

public class UserRoleRepositoryTest extends BaseTest {

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Test
    public void testGetUserRoleByType() {
        UUID userRoleId = UUID.fromString("d1931edd-f5bb-4366-b3cf-906988c83eeb"); // USER

        UUID actualId = userRoleRepository.findUserRoleIdByType(USER);

        Assert.assertEquals(userRoleId, actualId);
    }

    @Test
    public void testFindByType() {
        UserRoleType expectedType = USER;

        UserRole userRole = userRoleRepository.findByType(expectedType);

        Assert.assertNotNull(userRole);
        Assert.assertEquals(expectedType, userRole.getType());
    }

    @Test
    public void testFindAllRoles() {
        List<UserRole> userRoles = userRoleRepository.findAllRoles();

        Assert.assertEquals(4, userRoles.size());
        Assertions.assertThat(userRoles).extracting("type")
                .containsExactlyInAnyOrder(USER, ADMIN, CONTENT_MANAGER, MODERATOR);
    }
}
