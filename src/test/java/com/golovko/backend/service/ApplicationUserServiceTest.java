package com.golovko.backend.service;

import com.golovko.backend.BaseTest;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.UserRoleType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.user.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ApplicationUserRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.TransactionSystemException;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class ApplicationUserServiceTest extends BaseTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ApplicationUserService applicationUserService;

    @Test
    public void testGetAllUsers() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        ApplicationUser u3 = testObjectFactory.createUser();

        PageResult<UserReadDTO> actualResult = applicationUserService.getAllUsers(Pageable.unpaged());

        Assertions.assertThat(actualResult.getData()).extracting("id")
                .containsExactlyInAnyOrder(u1.getId(), u2.getId(), u3.getId());
    }

    @Test
    public void testGetUsersLeaderBoardByMovieComments() {
        ApplicationUser u1 = testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        ApplicationUser u3 = testObjectFactory.createUser();

        List<UserInLeaderBoardDTO> actualResult = applicationUserService.getUsersLeaderBoard();

        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(u1.getId(), u2.getId(), u3.getId());
    }

    @Test
    public void testGetAllUsersWithPagingAndSorting() {
        testObjectFactory.createUser();
        ApplicationUser u2 = testObjectFactory.createUser();
        ApplicationUser u3 = testObjectFactory.createUser();


        PageRequest pageRequest = PageRequest.of(0, 2,
                Sort.by(Sort.Direction.DESC, "createdAt"));

        Assertions.assertThat(applicationUserService.getAllUsers(pageRequest).getData())
                .extracting("id")
                .isEqualTo(Arrays.asList(u3.getId(), u2.getId()));
    }

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

        Assertions.assertThat(create).isEqualToIgnoringGivenFields(readDTO,
                "password", "passwordConfirmation");
        Assert.assertNotNull(readDTO.getId());

        ApplicationUser createdUser = applicationUserRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToIgnoringGivenFields(createdUser,
                "userRoles");

        Assertions.assertThat(createdUser.getUserRoles()).extracting("type")
                .containsExactlyInAnyOrder(UserRoleType.USER);
    }

    @Test
    public void testPatchUser() {
        ApplicationUser applicationUser = testObjectFactory.createUser();

        UserPatchDTO patch = new UserPatchDTO();
        patch.setUsername("Volodya");
        patch.setEmail("vovka@mail.ru");
        patch.setPassword("098765");

        UserReadDTO readDTO = applicationUserService.patchUser(applicationUser.getId(), patch);

        Assertions.assertThat(patch).isEqualToIgnoringGivenFields(readDTO,
                "password", "passwordConfirmation");

        applicationUser = applicationUserRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(applicationUser).isEqualToIgnoringGivenFields(readDTO,
                "password", "passwordConfirmation", "articles", "likes", "userRoles");
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
                "password", "passwordConfirmation", "articles", "likes", "userRoles");
    }

    @Test
    public void testUpdateUser() {
        ApplicationUser user = testObjectFactory.createUser();

        UserPutDTO updateDTO = new UserPutDTO();
        updateDTO.setUsername("new username");
        updateDTO.setPassword("new password");
        updateDTO.setEmail("new_email@gmail.com");

        UserReadDTO readDTO = applicationUserService.updateUser(user.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToIgnoringGivenFields(readDTO,
                "password","passwordConfirmation");

        user = applicationUserRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(user).isEqualToIgnoringGivenFields(readDTO,
                "password", "passwordConfirmation", "articles", "likes", "userRoles");
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

    @Test(expected = TransactionSystemException.class)
    public void testSaveUserNotNullValidation() {
        ApplicationUser user = new ApplicationUser();
        applicationUserRepository.save(user);
    }

    @Test(expected = TransactionSystemException.class)
    public void testSaveUserWrongEmailValidation() {
        ApplicationUser user = testObjectFactory.createUser();
        user.setEmail("wrongemail");
        applicationUserRepository.save(user);
    }
}