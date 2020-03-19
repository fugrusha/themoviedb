package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.UserRole;
import com.golovko.backend.dto.user.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.util.TestObjectFactory;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from user_role", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ApplicationUserServiceTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ApplicationUserService applicationUserService;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Test
    public void testGetUserById() {
        ApplicationUser user = testObjectFactory.createUser();

        UserReadDTO readDTO = applicationUserService.getUser(user.getId());

        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(user);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetUserWrongId() {
        applicationUserService.getUser(UUID.randomUUID());
    }

    @Test
    public void testCreateUser() {
        UserCreateDTO create = new UserCreateDTO();
        create.setUsername("Vitalik");
        create.setPassword("123456");
        create.setEmail("vetal@gmail.com");

        UserReadDTO readDTO = applicationUserService.createUser(create);

        Assertions.assertThat(create).isEqualToIgnoringGivenFields(readDTO, "password");
        Assert.assertNotNull(readDTO.getId());

        ApplicationUser applicationUser = applicationUserRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(applicationUser);
    }

    @Test
    public void testPatchUser() {
        ApplicationUser applicationUser = testObjectFactory.createUser();

        UserPatchDTO patch = new UserPatchDTO();
        patch.setUsername("Volodya");
        patch.setEmail("vovka@mail.ru");
        patch.setPassword("098765");

        UserReadDTO readDTO = applicationUserService.patchUser(applicationUser.getId(), patch);

        Assertions.assertThat(patch).isEqualToIgnoringGivenFields(readDTO, "password");

        applicationUser = applicationUserRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(applicationUser).isEqualToIgnoringGivenFields(readDTO,
                "password", "articles", "likes");
    }

    @Test
    public void testPatchUserEmptyPatch() {
        ApplicationUser applicationUser = testObjectFactory.createUser();

        UserPatchDTO patchDTO = new UserPatchDTO();
        UserReadDTO readDTO = applicationUserService.patchUser(applicationUser.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        ApplicationUser userAfterUpdate = applicationUserRepository.findById(readDTO.getId()).get();

        Assertions.assertThat(userAfterUpdate).hasNoNullFieldsOrProperties();
        Assertions.assertThat(applicationUser).isEqualToIgnoringGivenFields(userAfterUpdate,
                "password", "articles", "likes");
    }

    @Test
    public void testUpdateUser() {
        ApplicationUser user = testObjectFactory.createUser();

        UserPutDTO updateDTO = new UserPutDTO();
        updateDTO.setUsername("new username");
        updateDTO.setPassword("new password");
        updateDTO.setEmail("new_email@gmail.com");

        UserReadDTO readDTO = applicationUserService.updateUser(user.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToIgnoringGivenFields(readDTO, "password");

        user = applicationUserRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(user).isEqualToIgnoringGivenFields(readDTO,
                "password", "articles", "likes");
    }

    @Test
    public void testDeleteUser() {
        ApplicationUser applicationUser = testObjectFactory.createUser();
        applicationUserService.deleteUser(applicationUser.getId());

        Assert.assertFalse(applicationUserRepository.existsById(applicationUser.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteUserNotFound() {
        applicationUserService.deleteUser(UUID.randomUUID());
    }

    @Test
    public void testBanUser() {
        ApplicationUser user = testObjectFactory.createUser();

        UserReadDTO readDTO = applicationUserService.ban(user.getId());

        Assert.assertEquals(true, readDTO.getIsBlocked());

        ApplicationUser bannedUser = applicationUserRepository.findById(user.getId()).get();
        Assert.assertEquals(true, bannedUser.getIsBlocked());
    }

    @Test
    public void testPardonUser() {
        ApplicationUser user = testObjectFactory.createUser();
        user.setIsBlocked(true);
        applicationUserRepository.save(user);

        UserReadDTO readDTO = applicationUserService.pardon(user.getId());

        Assert.assertEquals(false, readDTO.getIsBlocked());

        ApplicationUser unBannedUser = applicationUserRepository.findById(user.getId()).get();
        Assert.assertEquals(false, unBannedUser.getIsBlocked());
    }

    @Test
    public void testChangeTrustLevel() {
        ApplicationUser user = testObjectFactory.createUser();
        user.setTrustLevel(9.0);
        applicationUserRepository.save(user);

        UserTrustLevelDTO trustLevelDTO = new UserTrustLevelDTO();
        trustLevelDTO.setTrustLevel(2.0);

        UserReadDTO actualResult = applicationUserService.changeTrustLevel(user.getId(), trustLevelDTO);

        Assertions.assertThat(actualResult).hasNoNullFieldsOrProperties();

        ApplicationUser updatedUser = applicationUserRepository.findById(user.getId()).get();
        Assert.assertEquals(updatedUser.getTrustLevel(), trustLevelDTO.getTrustLevel());
    }

    @Test
    public void testAddUserRole() {
        ApplicationUser user = testObjectFactory.createUser(Set.of(UserRole.USER));

        UserRoleDTO userRoleDTO = new UserRoleDTO();
        userRoleDTO.setUserRole(UserRole.MODERATOR);

        UserReadDTO actualResult = applicationUserService.addUserRole(user.getId(), userRoleDTO);

        Assertions.assertThat(actualResult).hasNoNullFieldsOrProperties();
        Assertions.assertThat(actualResult.getUserRole()).contains(UserRole.MODERATOR);

        ApplicationUser updatedUser = applicationUserRepository.findById(user.getId()).get();
        Assertions.assertThat(updatedUser.getUserRole()).contains(UserRole.MODERATOR, UserRole.USER);
    }

    @Test
    public void testRemoveUserRole() {
        ApplicationUser user = testObjectFactory.createUser(Set.of(UserRole.USER, UserRole.CONTENT_MANAGER));

        UserRoleDTO userRoleDTO = new UserRoleDTO();
        userRoleDTO.setUserRole(UserRole.CONTENT_MANAGER);

        UserReadDTO actualResult = applicationUserService.removeUserRole(user.getId(), userRoleDTO);

        Assertions.assertThat(actualResult).hasNoNullFieldsOrProperties();
        Assert.assertFalse(actualResult.getUserRole().contains(UserRole.CONTENT_MANAGER));

        ApplicationUser updatedUser = applicationUserRepository.findById(user.getId()).get();
        Assert.assertFalse(updatedUser.getUserRole().contains(UserRole.CONTENT_MANAGER));
    }
}