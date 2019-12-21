package com.golovko.backend.service;

import com.golovko.backend.domain.User;
import com.golovko.backend.dto.UserReadDTO;
import com.golovko.backend.repository.UserRepository;
import org.assertj.core.api.Assertions;
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
@Sql(statements = "delete from users", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class UserServiceTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Test
    public void testGetUser() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("Vitalka");
        user.setPassword("123456");
        user.setEmail("vetal@gmail.com");
        user.setActive(true);
        user =userRepository.save(user);

        UserReadDTO readDTO = userService.getUser(user.getId());
        Assertions.assertThat(readDTO).isEqualToComparingFieldByField(user);
    }
}