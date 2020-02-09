package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintType;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
@Sql(statements = {"delete from complaint", "delete from application_user"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class ApplicationUserServiceTest {

    @Autowired
    private ApplicationUserRepository applicationUserRepository;

    @Autowired
    private ApplicationUserService applicationUserService;

    @Autowired
    private TestObjectFactory testObjectFactory;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Transactional //FIXME remove this annotation
    @Test
    public void testGetUserExtendedTest() {
        ApplicationUser user = testObjectFactory.createUser();
        Complaint complaint = testObjectFactory.createComplaint(user, ComplaintType.MISPRINT);
        user.setComplaints(List.of(complaint));

        UserReadExtendedDTO readExtendedDTO = applicationUserService.getExtendedUser(user.getId());

        Assertions.assertThat(readExtendedDTO).isEqualToIgnoringGivenFields(user, "complaints");

        Assert.assertEquals(readExtendedDTO.getComplaints().get(0).getAuthorId(), complaint.getAuthor().getId());
        Assertions.assertThat(readExtendedDTO.getComplaints().get(0)).isEqualToIgnoringGivenFields(complaint,
                "authorId");
    }

    @Test
    public void testGetUser() {
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

        Assertions.assertThat(create).isEqualToComparingFieldByField(readDTO);
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

        Assertions.assertThat(patch).isEqualToComparingFieldByField(readDTO);

        applicationUser = applicationUserRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(applicationUser).isEqualToIgnoringGivenFields(readDTO,
                "complaints", "articles");
    }

    @Test
    public void testPatchUserEmptyPatch() {
        ApplicationUser applicationUser = testObjectFactory.createUser();

        UserPatchDTO patchDTO = new UserPatchDTO();
        UserReadDTO readDTO = applicationUserService.patchUser(applicationUser.getId(), patchDTO);

        Assertions.assertThat(readDTO).hasNoNullFieldsOrProperties();

        inTransaction(() -> {
            ApplicationUser userAfterUpdate = applicationUserRepository.findById(readDTO.getId()).get();

            Assertions.assertThat(userAfterUpdate).hasNoNullFieldsOrProperties();
            Assertions.assertThat(applicationUser).isEqualToIgnoringGivenFields(userAfterUpdate,
                    "complaints", "articles");
        });
    }

    @Test
    public void testUpdateUser() {
        ApplicationUser user = testObjectFactory.createUser();

        UserPutDTO updateDTO = new UserPutDTO();
        updateDTO.setUsername("new username");
        updateDTO.setPassword("new password");
        updateDTO.setEmail("new_email@gmail.com");

        UserReadDTO readDTO = applicationUserService.updateUser(user.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        user = applicationUserRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(user)
                .isEqualToIgnoringGivenFields(readDTO, "complaints", "articles");
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

    private void inTransaction(Runnable runnable) {
        transactionTemplate.executeWithoutResult(status -> {
            runnable.run();
        });
    }
}