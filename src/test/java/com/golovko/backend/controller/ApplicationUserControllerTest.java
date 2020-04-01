package com.golovko.backend.controller;

import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.dto.user.UserCreateDTO;
import com.golovko.backend.dto.user.UserPatchDTO;
import com.golovko.backend.dto.user.UserPutDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.exception.ControllerValidationException;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.ApplicationUserService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationUserController.class)
public class ApplicationUserControllerTest extends BaseControllerTest {

    @MockBean
    private ApplicationUserService applicationUserService;

    @Test
    public void testGetUser() throws Exception {
        UserReadDTO user = createUserReadDTO();

        Mockito.when(applicationUserService.getUser(user.getId())).thenReturn(user);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualUser = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assertions.assertThat(actualUser).isEqualToComparingFieldByField(user);

        Mockito.verify(applicationUserService).getUser(user.getId());
    }

    @Test
    public void testGetUserWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(ApplicationUser.class, wrongId);
        Mockito.when(applicationUserService.getUser(wrongId)).thenThrow(exception);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{id}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }

    @Test
    public void testGetUserIdTypeMismatch() throws Exception {
        String invalidId = "123";

        String errorMsg = "Invalid type of id. It should be of type java.util.UUID";

        ErrorInfo expectedErrorInfo = new ErrorInfo(
                HttpStatus.BAD_REQUEST,
                MethodArgumentTypeMismatchException.class,
                errorMsg);

        String result = mockMvc
                .perform(get("/api/v1/users/{id}", invalidId))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo actualErrorInfo = objectMapper.readValue(result, ErrorInfo.class);
        Assertions.assertThat(actualErrorInfo).isEqualToComparingFieldByField(expectedErrorInfo);
    }

    @Test
    public void testCreateUser() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("david");
        createDTO.setPassword("1234567890");
        createDTO.setPasswordConfirmation("1234567890");
        createDTO.setEmail("david101@email.com");

        UserReadDTO readDTO = createUserReadDTO();

        Mockito.when(applicationUserService.createUser(createDTO)).thenReturn(readDTO);

        String result = mockMvc
                .perform(post("/api/v1/users")
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualUser = objectMapper.readValue(result, UserReadDTO.class);
        Assertions.assertThat(actualUser).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testCreateUserNotNullValidationException() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).createUser(any());
    }

    @Test
    public void testCreateUserEmailValidationException() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("david");
        createDTO.setPassword("1234567890");
        createDTO.setPasswordConfirmation("1234567890");
        createDTO.setEmail("david101email.com"); // wrong email

        String resultJson = mockMvc
                .perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).createUser(any());
    }

    @Test
    public void testCreateUserShortPassword() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("david");
        createDTO.setPassword("1234567");
        createDTO.setPasswordConfirmation("1234567");
        createDTO.setEmail("david101@email.com");

        String resultJson = mockMvc
                .perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertTrue(error.getMessage().contains("Password should contain at least 8 characters without spaces"));
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).createUser(any());
    }

    @Test
    public void testCreateUserPasswordWithSpaces() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("david");
        createDTO.setPassword("12 34 567");
        createDTO.setPasswordConfirmation("12 34 567");
        createDTO.setEmail("david101@email.com");

        String resultJson = mockMvc
                .perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertTrue(error.getMessage().contains("Password should contain at least 8 characters without spaces"));
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).createUser(any());
    }

    @Test
    public void testCreateUserDifferentPasswords() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("david");
        createDTO.setPassword("12345789");
        createDTO.setPasswordConfirmation("xyxyxyxyxyxyxy");
        createDTO.setEmail("david101@email.com");

        String resultJson = mockMvc
                .perform(post("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo errorInfo = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertTrue(errorInfo.getMessage().contains("password"));
        Assert.assertTrue(errorInfo.getMessage().contains("passwordConfirmation"));
        Assert.assertEquals(ControllerValidationException.class, errorInfo.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).createUser(any());
    }

    @Test
    public void testPatchUser() throws Exception {
        UserPatchDTO patchDTO = new UserPatchDTO();
        patchDTO.setUsername("david");
        patchDTO.setPassword("securedPassword");
        patchDTO.setPasswordConfirmation("securedPassword");
        patchDTO.setEmail("david101@email.com");

        UserReadDTO readDTO = createUserReadDTO();

        Mockito.when(applicationUserService.patchUser(readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{id}", readDTO.getId())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualUser = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assert.assertEquals(readDTO, actualUser);
    }

    @Test
    public void testPatchUserShortPassword() throws Exception {
        UserPatchDTO patchDTO = new UserPatchDTO();
        patchDTO.setUsername("david");
        patchDTO.setPassword("1234567");
        patchDTO.setPasswordConfirmation("1234567");
        patchDTO.setEmail("david101@email.com");

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{id}", UUID.randomUUID())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertTrue(error.getMessage().contains("Password should contain at least 8 characters without spaces"));
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).patchUser(any(), any());
    }

    @Test
    public void testPatchUserPasswordWithSpaces() throws Exception {
        UserPatchDTO patchDTO = new UserPatchDTO();
        patchDTO.setUsername("david");
        patchDTO.setPassword("1234 56789");
        patchDTO.setPasswordConfirmation("1234 56789");
        patchDTO.setEmail("david101@email.com");

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{id}", UUID.randomUUID())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertTrue(error.getMessage().contains("Password should contain at least 8 characters without spaces"));
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).patchUser(any(), any());
    }

    @Test
    public void testPatchUserDifferentPasswords() throws Exception {
        UserPatchDTO patchDTO = new UserPatchDTO();
        patchDTO.setUsername("david");
        patchDTO.setPassword("123456789");
        patchDTO.setPasswordConfirmation("xyxyxyxyxyxyxy");
        patchDTO.setEmail("david101@email.com");

        String resultJson = mockMvc
                .perform(patch("/api/v1/users/{id}", UUID.randomUUID())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo errorInfo = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertTrue(errorInfo.getMessage().contains("password"));
        Assert.assertTrue(errorInfo.getMessage().contains("passwordConfirmation"));
        Assert.assertEquals(ControllerValidationException.class, errorInfo.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).patchUser(any(), any());
    }

    @Test
    public void testUpdateUser() throws Exception {
        UserPutDTO updateDTO = new UserPutDTO();
        updateDTO.setUsername("new username");
        updateDTO.setPasswordConfirmation("securedPassword");
        updateDTO.setPassword("securedPassword");
        updateDTO.setEmail("new_user_email@gmail.com");

        UserReadDTO readDTO = createUserReadDTO();

        Mockito.when(applicationUserService.updateUser(readDTO.getId(), updateDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/users/{id}", readDTO.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualUser = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assert.assertEquals(readDTO, actualUser);
    }


    @Test
    public void testUpdateUserShortPassword() throws Exception {
        UserPutDTO updateDTO = new UserPutDTO();
        updateDTO.setUsername("david");
        updateDTO.setPassword("1234567");
        updateDTO.setPasswordConfirmation("1234567");
        updateDTO.setEmail("david101@email.com");

        String resultJson = mockMvc
                .perform(put("/api/v1/users/{id}", UUID.randomUUID())
                .content(objectMapper.writeValueAsString(updateDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertTrue(error.getMessage().contains("Password should contain at least 8 characters without spaces"));
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).updateUser(any(), any());
    }

    @Test
    public void testUpdateUserPasswordWithSpaces() throws Exception {
        UserPutDTO updateDTO = new UserPutDTO();
        updateDTO.setUsername("david");
        updateDTO.setPassword("1234 56789");
        updateDTO.setPasswordConfirmation("1234 56789");
        updateDTO.setEmail("david101@email.com");

        String resultJson = mockMvc
                .perform(put("/api/v1/users/{id}", UUID.randomUUID())
                .content(objectMapper.writeValueAsString(updateDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertTrue(error.getMessage().contains("Password should contain at least 8 characters without spaces"));
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).updateUser(any(), any());
    }

    @Test
    public void testUpdateUserDifferentPasswords() throws Exception {
        UserPutDTO updateDTO = new UserPutDTO();
        updateDTO.setUsername("david");
        updateDTO.setPassword("123456789");
        updateDTO.setPasswordConfirmation("xyxyxyxyxyxyxy");
        updateDTO.setEmail("david101@email.com");

        String resultJson = mockMvc
                .perform(put("/api/v1/users/{id}", UUID.randomUUID())
                .content(objectMapper.writeValueAsString(updateDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo errorInfo = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertTrue(errorInfo.getMessage().contains("password"));
        Assert.assertTrue(errorInfo.getMessage().contains("passwordConfirmation"));
        Assert.assertEquals(ControllerValidationException.class, errorInfo.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).updateUser(any(), any());
    }

    @Test
    public void testDeleteUser() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isOk());

        Mockito.verify(applicationUserService).deleteUser(id);
    }

    @Test
    public void testBanUser() throws Exception {
        UserReadDTO readDTO = createUserReadDTO();

        Mockito.when(applicationUserService.ban(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc.perform(post("/api/v1/users/{id}/ban", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualResult = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assert.assertEquals(actualResult, readDTO);

        Mockito.verify(applicationUserService).ban(readDTO.getId());
    }

    @Test
    public void testPardonUser() throws Exception {
        UserReadDTO readDTO = createUserReadDTO();

        Mockito.when(applicationUserService.pardon(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc.perform(post("/api/v1/users/{id}/pardon", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualResult = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assert.assertEquals(actualResult, readDTO);

        Mockito.verify(applicationUserService).pardon(readDTO.getId());
    }

    private UserReadDTO createUserReadDTO() {
        UserReadDTO readDTO = new UserReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setUsername("david");
        readDTO.setEmail("david101@email.com");
        readDTO.setIsBlocked(false);
        readDTO.setTrustLevel(6.5);
        readDTO.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        readDTO.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return readDTO;
    }
}