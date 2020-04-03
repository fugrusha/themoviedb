package com.golovko.backend.controller;

import com.golovko.backend.dto.userrole.UserRoleReadDTO;
import com.golovko.backend.service.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class UserRoleController {

    @Autowired
    private UserRoleService userRoleService;

    @GetMapping("/users/{userId}/roles")
    public List<UserRoleReadDTO> getUserRoles(@PathVariable UUID userId) {
        return userRoleService.getUserRoles(userId);
    }

    @PostMapping("/users/{userId}/roles/{id}")
    public List<UserRoleReadDTO> addRoleToUser(@PathVariable UUID userId, @PathVariable UUID id) {
        return userRoleService.addUserRole(userId, id);
    }

    @DeleteMapping("/users/{userId}/roles/{id}")
    List<UserRoleReadDTO> removeRoleFromUser(@PathVariable UUID userId, @PathVariable UUID id) {
        return userRoleService.removeRoleFromUser(userId, id);
    }
}
