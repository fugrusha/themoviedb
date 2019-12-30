package com.golovko.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.User;
import com.golovko.backend.dto.UserCreateDTO;
import com.golovko.backend.dto.UserReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.UserService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    public void testGetUser() throws Exception {
        UserReadDTO user = new UserReadDTO();
        user.setId(UUID.randomUUID());
        user.setUsername("Vitalka");
        user.setPassword("123456");
        user.setEmail("vetal@gmail.com");

        Mockito.when(userService.getUser(user.getId())).thenReturn(user);

        String resultJson = mvc.perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println(resultJson);

        UserReadDTO actualUser = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assertions.assertThat(actualUser).isEqualToComparingFieldByField(user);

        Mockito.verify(userService).getUser(user.getId());
    }

    @Test
    public void testGetUserWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(User.class, wrongId);
        Mockito.when(userService.getUser(wrongId)).thenThrow(exception);

        String resultJson = mvc.perform(get("/api/v1/users/{id}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }

    @Test
    public void testGetUserIdTypeMismatch() throws Exception {
        String invalidId = "123";

        String result = mvc.perform(get("/api/v1/users/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(MethodArgumentTypeMismatchException.class.getSimpleName()));
    }
    

    @Test
    public void testCreateUser() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("david");
        createDTO.setPassword("12345");
        createDTO.setEmail("david101@email.com");

        UserReadDTO readDTO = new UserReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setUsername("david");
        readDTO.setPassword("12345");
        readDTO.setEmail("david101@email.com");

        Mockito.when(userService.createUser(createDTO)).thenReturn(readDTO);

        String result = mvc.perform(post("/api/v1/users")
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualUser = objectMapper.readValue(result, UserReadDTO.class);
        Assertions.assertThat(actualUser).isEqualToComparingFieldByField(readDTO);
    }
}