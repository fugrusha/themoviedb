package com.golovko.backend.controller;

import com.golovko.backend.controller.security.Admin;
import com.golovko.backend.dto.userrole.UserRoleReadDTO;
import com.golovko.backend.service.UserRoleService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Admin
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @ApiOperation(value = "Get all roles of user", notes = "Needs ADMIN authority")
    @GetMapping("/users/{userId}/roles")
    public List<UserRoleReadDTO> getUserRoles(@PathVariable UUID userId) {
        return userRoleService.getUserRoles(userId);
    }

    @ApiOperation(value = "Add role to user", notes = "Needs ADMIN authority")
    @PostMapping("/users/{userId}/roles/{id}")
    public List<UserRoleReadDTO> addRoleToUser(@PathVariable UUID userId, @PathVariable UUID id) {
        return userRoleService.addUserRole(userId, id);
    }

    @ApiOperation(value = "Remove role from user", notes = "Needs ADMIN authority")
    @DeleteMapping("/users/{userId}/roles/{id}")
    public List<UserRoleReadDTO> removeRoleFromUser(@PathVariable UUID userId, @PathVariable UUID id) {
        return userRoleService.removeRoleFromUser(userId, id);
    }
}
