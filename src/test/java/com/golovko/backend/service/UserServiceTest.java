package com.golovko.backend.service;

import com.golovko.backend.domain.User;
import com.golovko.backend.dto.UserCreateDTO;
import com.golovko.backend.dto.UserPatchDTO;
import com.golovko.backend.dto.UserReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.UserRepository;
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
@Sql(statements = "delete from usr", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    private User createUser() {
        User user = new User();
        user.setUsername("Vitalka");
        user.setPassword("123456");
        user.setEmail("vetal@gmail.com");
        user = userRepository.save(user);
        return user;
    }

    @Test
    public void testGetUser() {
        User user = createUser();

        UserReadDTO readDTO = userService.getUser(user.getId());
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(user);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testGetUserWrongId() {
        userService.getUser(UUID.randomUUID());
    }

    @Test
    public void testCreateUser() {
        UserCreateDTO create = new UserCreateDTO();
        create.setUsername("Vitalik");
        create.setPassword("123456");
        create.setEmail("vetal@gmail.com");
        UserReadDTO readDTO = userService.createUser(create);

        Assertions.assertThat(create).isEqualToComparingFieldByField(readDTO);
        Assert.assertNotNull(readDTO.getId());

        User user = userRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(user);
    }

    @Test
    public void testPatchUser() {
        User user = createUser();

        UserPatchDTO patch = new UserPatchDTO();
        patch.setUsername("Volodya");
        patch.setEmail("vovka@mail.ru");
        patch.setPassword("098765");
        UserReadDTO readDTO = userService.patchUser(user.getId(), patch);

        Assertions.assertThat(patch).isEqualToComparingFieldByField(readDTO);

        user = userRepository.findById(readDTO.getId()).get();
        Assertions.assertThat(user).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchUserEmptyPatch() {
        User user = createUser();

        UserPatchDTO patchDTO = new UserPatchDTO();
        UserReadDTO readDTO = userService.patchUser(user.getId(), patchDTO);

        Assert.assertNotNull(readDTO.getEmail());
        Assert.assertNotNull(readDTO.getUsername());
        Assert.assertNotNull(readDTO.getPassword());

        User userAfterUpdate = userRepository.findById(readDTO.getId()).get();

        Assert.assertNotNull(userAfterUpdate.getEmail());
        Assert.assertNotNull(userAfterUpdate.getUsername());
        Assert.assertNotNull(userAfterUpdate.getPassword());

        Assertions.assertThat(user).isEqualToComparingFieldByField(userAfterUpdate);
    }

    @Test
    public void testDeleteUser() {
        User user = createUser();

        userService.deleteUser(user.getId());
        Assert.assertFalse(userRepository.existsById(user.getId()));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDeleteUserNotFound() {
        userService.deleteUser(UUID.randomUUID());
    }


}