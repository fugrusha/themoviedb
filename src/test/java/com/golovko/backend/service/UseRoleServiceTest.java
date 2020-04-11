package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.UserRole;
import com.golovko.backend.domain.UserRoleType;
import com.golovko.backend.dto.userrole.UserRoleReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.LinkDuplicatedException;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.UserRoleRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UseRoleServiceTest extends BaseTest {

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private UserRoleRepository userRoleRepository;

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Test
    public void testGetUserRolesByUserId() {
        ApplicationUser user = testObjectFactory.createUser();
        UserRole userRole = userRoleRepository.findByType(UserRoleType.CONTENT_MANAGER);
        user.setUserRoles(List.of(userRole));
        applicationUserRepository.save(user);

        List<UserRoleReadDTO> actualResult = userRoleService.getUserRoles(user.getId());

        Assertions.assertThat(actualResult).extracting("id")
                .contains(userRole.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetNotFoundUserRolesByUserId() {
        ApplicationUser user = testObjectFactory.createUser();
        userRoleService.getUserRoles(user.getId());
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetUserRolesByUserIdEmptyCollection() {
        ApplicationUser user = testObjectFactory.createUser();
        user.setUserRoles(new ArrayList<UserRole>());
        applicationUserRepository.save(user);

        userRoleService.getUserRoles(user.getId());
    }

    @Test
    public void testAddRoleToUser() {
        ApplicationUser user = testObjectFactory.createUser();
        UUID userRoleId = userRoleRepository.findUserRoleIdByType(UserRoleType.CONTENT_MANAGER);

        List<UserRoleReadDTO> actualResult = userRoleService.addUserRole(user.getId(), userRoleId);

        UserRoleReadDTO expectedRead = new UserRoleReadDTO();
        expectedRead.setId(userRoleId);
        expectedRead.setType(UserRoleType.CONTENT_MANAGER);

        Assertions.assertThat(actualResult).contains(expectedRead);

        inTransaction(() -> {
            ApplicationUser updatedUser = applicationUserRepository.findById(user.getId()).get();
            Assertions.assertThat(updatedUser.getUserRoles()).extracting("id")
                    .containsExactlyInAnyOrder(userRoleId);
        });
    }

    @Test
    public void testDuplicatedRole() {
        ApplicationUser user = testObjectFactory.createUser();
        UUID userRoleId = userRoleRepository.findUserRoleIdByType(UserRoleType.CONTENT_MANAGER);

        userRoleService.addUserRole(user.getId(), userRoleId);

        Assertions.assertThatThrownBy(() -> userRoleService.addUserRole(user.getId(), userRoleId))
                .isInstanceOf(LinkDuplicatedException.class);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testWrongUserId() {
        UUID wrongUserId = UUID.randomUUID();
        UUID userRoleId = userRoleRepository.findUserRoleIdByType(UserRoleType.CONTENT_MANAGER);

        userRoleService.addUserRole(wrongUserId, userRoleId);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testWrongUserRoleId() {
        ApplicationUser user = testObjectFactory.createUser();
        UUID wrongUserRoleId = UUID.randomUUID();

        userRoleService.addUserRole(user.getId(), wrongUserRoleId);
    }

    @Test
    public void testRemoveRoleFromUser() {
        ApplicationUser user = testObjectFactory.createUser();
        UUID userRoleId = userRoleRepository.findUserRoleIdByType(UserRoleType.CONTENT_MANAGER);
        userRoleService.addUserRole(user.getId(), userRoleId);

        List<UserRoleReadDTO> remainingRoles = userRoleService.removeRoleFromUser(user.getId(), userRoleId);
        Assert.assertTrue(remainingRoles.isEmpty());

        inTransaction(() -> {
            ApplicationUser updatedUser = applicationUserRepository.findById(user.getId()).get();
            Assert.assertTrue(updatedUser.getUserRoles().isEmpty());
        });
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteNotFoundRole() {
        ApplicationUser user = testObjectFactory.createUser();
        UUID userRoleId = userRoleRepository.findUserRoleIdByType(UserRoleType.CONTENT_MANAGER);

        userRoleService.removeRoleFromUser(user.getId(), userRoleId);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteNotExistedRole() {
        ApplicationUser user = testObjectFactory.createUser();
        UUID wrongUserRoleId = UUID.randomUUID();

        userRoleService.removeRoleFromUser(user.getId(), wrongUserRoleId);
    }
}
