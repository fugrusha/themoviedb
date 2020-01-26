package com.golovko.backend.service;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.Complaint;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.dto.user.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.ApplicationUserRepository;
import com.golovko.backend.repository.ComplaintRepository;
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

import java.util.ArrayList;
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
    private ComplaintRepository complaintRepository;

    @Autowired
    private ApplicationUserService applicationUserService;

    private Complaint createComplaint(ApplicationUser user) {
        Complaint complaint = new Complaint();
        complaint.setComplaintText("Complaint text");
        complaint.setComplaintTitle("Complaint Title");
        complaint.setComplaintType(ComplaintType.CHILD_ABUSE);
        complaint.setAuthor(user);
        complaint = complaintRepository.save(complaint);
        return complaint;
    }

    private ApplicationUser createUser() {
        ApplicationUser applicationUser = new ApplicationUser();
        applicationUser.setUsername("Vitalka");
        applicationUser.setPassword("123456");
        applicationUser.setEmail("vetal@gmail.com");
        applicationUser = applicationUserRepository.save(applicationUser);
        return applicationUser;
    }

    @Transactional
    @Test
    public void testGetUserExtendedTest() {
        ApplicationUser user = createUser();
        Complaint complaint = createComplaint(user);

        List<Complaint> complaints = new ArrayList<>();
        complaints.add(complaint);
        user.setComplaints(complaints);

        UserReadExtendedDTO readExtendedDTO = applicationUserService.getExtendedUser(user.getId());

        Assertions.assertThat(readExtendedDTO).isEqualToIgnoringGivenFields(user, "complaints");

        Assert.assertEquals(readExtendedDTO.getComplaints().get(0).getAuthorId(), complaint.getAuthor().getId());
        Assertions.assertThat(readExtendedDTO.getComplaints().get(0)).isEqualToIgnoringGivenFields(complaint, "authorId");
    }

    @Test
    public void testGetUser() {
        ApplicationUser user = createUser();

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
        ApplicationUser applicationUser = createUser();

        UserPatchDTO patch = new UserPatchDTO();
        patch.setUsername("Volodya");
        patch.setEmail("vovka@mail.ru");
        patch.setPassword("098765");

        UserReadDTO readDTO = applicationUserService.patchUser(applicationUser.getId(), patch);

        Assertions.assertThat(patch).isEqualToComparingFieldByField(readDTO);

        applicationUser = applicationUserRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(applicationUser).isEqualToIgnoringGivenFields(readDTO, "complaints");
    }

    @Transactional
    @Test
    public void testPatchUserEmptyPatch() {
        ApplicationUser applicationUser = createUser();

        UserPatchDTO patchDTO = new UserPatchDTO();
        UserReadDTO readDTO = applicationUserService.patchUser(applicationUser.getId(), patchDTO);

        Assert.assertNotNull(readDTO.getEmail());
        Assert.assertNotNull(readDTO.getUsername());
        Assert.assertNotNull(readDTO.getPassword());

        ApplicationUser applicationUserAfterUpdate = applicationUserRepository.findById(readDTO.getId()).get();

        Assert.assertNotNull(applicationUserAfterUpdate.getEmail());
        Assert.assertNotNull(applicationUserAfterUpdate.getUsername());
        Assert.assertNotNull(applicationUserAfterUpdate.getPassword());

        Assertions.assertThat(applicationUser).isEqualToComparingFieldByField(applicationUserAfterUpdate);
    }

    @Test
    public void testUpdateUser() {
        ApplicationUser user = createUser();

        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("new username");
        updateDTO.setPassword("new password");
        updateDTO.setEmail("new_email@gmail.com");

        UserReadDTO readDTO = applicationUserService.updateUser(user.getId(), updateDTO);

        Assertions.assertThat(updateDTO).isEqualToComparingFieldByField(readDTO);

        user = applicationUserRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(user).isEqualToIgnoringGivenFields(readDTO, "complaints");
    }

    @Test
    public void testDeleteUser() {
        ApplicationUser applicationUser = createUser();
        applicationUserService.deleteUser(applicationUser.getId());

        Assert.assertFalse(applicationUserRepository.existsById(applicationUser.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteUserNotFound() {
        applicationUserService.deleteUser(UUID.randomUUID());
    }


}