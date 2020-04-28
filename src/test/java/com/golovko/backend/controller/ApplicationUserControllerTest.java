package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.user.*;
import com.golovko.backend.exception.ControllerValidationException;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.UserAlreadyExistsException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.ApplicationUserService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApplicationUserController.class)
public class ApplicationUserControllerTest extends BaseControllerTest {

    @MockBean
    private ApplicationUserService applicationUserService;

    @WithMockUser
    @Test
    public void testGetAllUsers() throws Exception {
        UserReadDTO u1 = generateObject(UserReadDTO.class);
        UserReadDTO u2 = generateObject(UserReadDTO.class);
        UserReadDTO u3 = generateObject(UserReadDTO.class);

        PageResult<UserReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(u1, u2, u3));

        Mockito.when(applicationUserService.getAllUsers(PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<UserReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult.getData()).extracting(UserReadDTO::getId)
                .containsExactlyInAnyOrder(u1.getId(), u2.getId(), u3.getId());

        Mockito.verify(applicationUserService).getAllUsers(PageRequest.of(0, defaultPageSize));
    }

    @WithMockUser
    @Test
    public void testGetUsersLeaderBoardByMovieComments() throws Exception {
        UserInLeaderBoardDTO u1 = generateObject(UserInLeaderBoardDTO.class);
        UserInLeaderBoardDTO u2 = generateObject(UserInLeaderBoardDTO.class);
        UserInLeaderBoardDTO u3 = generateObject(UserInLeaderBoardDTO.class);

        List<UserInLeaderBoardDTO> expectedResult = List.of(u1, u2, u3);

        Mockito.when(applicationUserService.getUsersLeaderBoard()).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/leader-board"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<UserInLeaderBoardDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting("id")
                .containsExactlyInAnyOrder(u1.getId(), u2.getId(), u3.getId());

        Mockito.verify(applicationUserService).getUsersLeaderBoard();
    }

    @WithMockUser
    @Test
    public void testGetAllUsersWithPagingAndSorting() throws Exception {
        UserReadDTO u1 = generateObject(UserReadDTO.class);

        int page = 1;
        int size = 30;

        PageResult<UserReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(u1));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(applicationUserService.getAllUsers(pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/")
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<UserReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetUser() throws Exception {
        UserReadDTO user = generateObject(UserReadDTO.class);

        Mockito.when(applicationUserService.getUser(user.getId())).thenReturn(user);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualUser = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assertions.assertThat(actualUser).isEqualToComparingFieldByField(user);

        Mockito.verify(applicationUserService).getUser(user.getId());
    }

    @WithMockUser
    @Test
    public void testGetExtendedUser() throws Exception {
        UserReadExtendedDTO user = generateObject(UserReadExtendedDTO.class);

        Mockito.when(applicationUserService.getExtendedUser(user.getId())).thenReturn(user);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{id}/extended", user.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadExtendedDTO actualUser = objectMapper.readValue(resultJson, UserReadExtendedDTO.class);
        Assertions.assertThat(actualUser).isEqualToComparingFieldByField(user);

        Mockito.verify(applicationUserService).getExtendedUser(user.getId());
    }

    @WithMockUser
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

    @WithMockUser
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

        UserReadDTO readDTO = generateObject(UserReadDTO.class);

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
    public void testCreateUserThrowsUserAlreadyExistsException() throws Exception {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setUsername("david");
        createDTO.setPassword("1234567890");
        createDTO.setPasswordConfirmation("1234567890");
        createDTO.setEmail("david101@email.com");

        UserAlreadyExistsException ex = new UserAlreadyExistsException(createDTO.getEmail());

        Mockito.when(applicationUserService.createUser(createDTO)).thenThrow(ex);

        String resultJson = mockMvc
                .perform(post("/api/v1/users")
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo actualResult = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(actualResult.getMessage(), ex.getMessage());

        Mockito.verify(applicationUserService).createUser(createDTO);

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

    @WithMockUser
    @Test
    public void testPatchUser() throws Exception {
        UserPatchDTO patchDTO = new UserPatchDTO();
        patchDTO.setUsername("david");
        patchDTO.setPassword("securedPassword");
        patchDTO.setPasswordConfirmation("securedPassword");
        patchDTO.setEmail("david101@email.com");

        UserReadDTO readDTO = generateObject(UserReadDTO.class);

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

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser
    @Test
    public void testUpdateUser() throws Exception {
        UserPutDTO updateDTO = new UserPutDTO();
        updateDTO.setUsername("new username");
        updateDTO.setPasswordConfirmation("securedPassword");
        updateDTO.setPassword("securedPassword");
        updateDTO.setEmail("new_user_email@gmail.com");

        UserReadDTO readDTO = generateObject(UserReadDTO.class);

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

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser
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

    @WithMockUser
    @Test
    public void testDeleteUser() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/users/{id}", id))
                .andExpect(status().isOk());

        Mockito.verify(applicationUserService).deleteUser(id);
    }

    @WithMockUser
    @Test
    public void testBanUser() throws Exception {
        UserReadDTO readDTO = generateObject(UserReadDTO.class);

        Mockito.when(applicationUserService.ban(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc.perform(post("/api/v1/users/{id}/ban", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualResult = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assert.assertEquals(actualResult, readDTO);

        Mockito.verify(applicationUserService).ban(readDTO.getId());
    }

    @WithMockUser
    @Test
    public void testPardonUser() throws Exception {
        UserReadDTO readDTO = generateObject(UserReadDTO.class);

        Mockito.when(applicationUserService.pardon(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc.perform(post("/api/v1/users/{id}/pardon", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualResult = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assert.assertEquals(actualResult, readDTO);

        Mockito.verify(applicationUserService).pardon(readDTO.getId());
    }
}