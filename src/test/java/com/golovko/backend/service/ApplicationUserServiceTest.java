package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.dto.user.UserCreateDTO;
import com.golovko.backend.dto.user.UserPatchDTO;
import com.golovko.backend.dto.user.UserPutDTO;
import com.golovko.backend.dto.user.UserReadDTO;
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

import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from application_user"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
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
                "password", "articles");
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
                "password", "articles");
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
                "password", "articles");
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
}