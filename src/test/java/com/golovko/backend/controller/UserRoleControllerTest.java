package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.UserRoleType;
import com.golovko.backend.dto.userrole.UserRoleReadDTO;
import com.golovko.backend.service.UserRoleService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRoleController.class)
public class UserRoleControllerTest extends BaseControllerTest {

    @MockBean
    private UserRoleService userRoleService;

    @Test
    public void testGetUserRoleByUserId() throws Exception {
        UserRoleReadDTO readDTO = createUserRoleReadDTO();
        List<UserRoleReadDTO> expectedResult = List.of(readDTO);

        UUID userId = UUID.randomUUID();

        Mockito.when(userRoleService.getUserRoles(userId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/users/{userId}/roles", userId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<UserRoleReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(userRoleService).getUserRoles(userId);
    }

    @Test
    public void testAddRoleToUser() throws Exception {
        UserRoleReadDTO readDTO = createUserRoleReadDTO();
        UUID userId = UUID.randomUUID();
        UUID userRoleId = UUID.randomUUID();

        List<UserRoleReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(userRoleService.addUserRole(userId, userRoleId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{userId}/roles/{id}", userId, userRoleId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<UserRoleReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);
    }

    @Test
    public void testRemoveRoleFromUser() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID userRoleId = UUID.randomUUID();

        List<UserRoleReadDTO> emptyList= new ArrayList<>();

        Mockito.when(userRoleService.removeRoleFromUser(userId, userRoleId)).thenReturn(emptyList);

        String resultJson = mockMvc
                .perform(delete("/api/v1/users/{userId}/roles/{id}", userId, userRoleId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<UserRoleReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertTrue(actualResult.isEmpty());

        Mockito.verify(userRoleService).removeRoleFromUser(userId, userRoleId);
    }

    private UserRoleReadDTO createUserRoleReadDTO() {
        UserRoleReadDTO dto = new UserRoleReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setType(UserRoleType.ADMIN);
        return dto;
    }
}
