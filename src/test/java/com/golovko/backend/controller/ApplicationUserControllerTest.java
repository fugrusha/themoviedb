package com.golovko.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.ApplicationUser;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.user.*;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.ApplicationUserService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ApplicationUserController.class)
public class ApplicationUserControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ApplicationUserService applicationUserService;

    @Test
    public void testGetUserTest() throws Exception {
        UserReadDTO user = createUserReadDTO();

        Mockito.when(applicationUserService.getUser(user.getId())).thenReturn(user);

        String resultJson = mvc
                .perform(get("/api/v1/users/{id}", user.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserReadDTO actualUser = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assertions.assertThat(actualUser).isEqualToComparingFieldByField(user);

        Mockito.verify(applicationUserService).getUser(user.getId());
    }

    @Test
    public void testGetExtendedUser() throws Exception {
        ComplaintReadDTO complaint = createComplaintReadDTO();
        List<ComplaintReadDTO> complaints = new ArrayList<>();
        complaints.add(complaint);

        UserReadExtendedDTO userDTO = createUserReadExtendedDTO(complaints);

        Mockito.when(applicationUserService.getExtendedUser(userDTO.getId())).thenReturn(userDTO);

        String resultJson = mvc
                .perform(get("/api/v1/users/{id}/extended", userDTO.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        UserReadExtendedDTO actualUser = objectMapper.readValue(resultJson, UserReadExtendedDTO.class);
        Assertions.assertThat(actualUser).isEqualToComparingFieldByField(userDTO);

        Mockito.verify(applicationUserService).getExtendedUser(userDTO.getId());
    }

    @Test
    public void testGetUserWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(ApplicationUser.class, wrongId);
        Mockito.when(applicationUserService.getUser(wrongId)).thenThrow(exception);

        String resultJson = mvc
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

        String result = mvc
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
        createDTO.setPassword("12345");
        createDTO.setEmail("david101@email.com");

        UserReadDTO readDTO = createUserReadDTO();

        Mockito.when(applicationUserService.createUser(createDTO)).thenReturn(readDTO);

        String result = mvc
                .perform(post("/api/v1/users")
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualUser = objectMapper.readValue(result, UserReadDTO.class);
        Assertions.assertThat(actualUser).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchUser() throws Exception {
        UserPatchDTO patchDTO = new UserPatchDTO();
        patchDTO.setUsername("david");
        patchDTO.setEmail("david101@email.com");
        patchDTO.setPassword("12345");

        UserReadDTO readDTO = createUserReadDTO();

        Mockito.when(applicationUserService.patchUser(readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mvc
                .perform(patch("/api/v1/users/{id}", readDTO.getId().toString())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualUser = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assert.assertEquals(readDTO, actualUser);
    }

    @Test
    public void testUpdateUser() throws Exception {
        UserPutDTO updateDTO = new UserPutDTO();
        updateDTO.setUsername("new username");
        updateDTO.setPassword("new password");
        updateDTO.setEmail("new_user_email@gmail.com");

        UserReadDTO readDTO = createUserReadDTO();

        Mockito.when(applicationUserService.updateUser(readDTO.getId(), updateDTO)).thenReturn(readDTO);

        String resultJson = mvc
                .perform(put("/api/v1/users/{id}", readDTO.getId().toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualUser = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assert.assertEquals(readDTO, actualUser);
    }

    @Test
    public void testDeleteUser() throws Exception {
        UUID id = UUID.randomUUID();

        mvc.perform(delete("/api/v1/users/{id}", id.toString()))
                .andExpect(status().isOk());

        Mockito.verify(applicationUserService).deleteUser(id);
    }

    private UserReadDTO createUserReadDTO() {
        UserReadDTO readDTO = new UserReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setUsername("david");
        readDTO.setEmail("david101@email.com");
        readDTO.setPassword("12345");
        return readDTO;
    }

    private UserReadExtendedDTO createUserReadExtendedDTO(List<ComplaintReadDTO> complaints) {
        UserReadExtendedDTO readDTO = new UserReadExtendedDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setUsername("david");
        readDTO.setEmail("david101@email.com");
        readDTO.setComplaints(complaints);
        return readDTO;
    }

    private ComplaintReadDTO createComplaintReadDTO() {
        ComplaintReadDTO readDTO = new ComplaintReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setComplaintTitle("Report 1");
        readDTO.setComplaintText("I have noticed a spoiler");
        readDTO.setComplaintType(ComplaintType.SPOILER);
        readDTO.setAuthorId(UUID.randomUUID());
        return readDTO;
    }
}