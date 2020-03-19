package com.golovko.backend.controller;

import com.golovko.backend.dto.user.*;
import com.golovko.backend.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class ApplicationUserController {

    @Autowired
    private ApplicationUserService applicationUserService;

    @GetMapping("/{id}")
    public UserReadDTO getUser(@PathVariable UUID id) {
        return applicationUserService.getUser(id);
    }

    @PostMapping
    public UserReadDTO createUser(@RequestBody UserCreateDTO createDTO) {
        return applicationUserService.createUser(createDTO);
    }

    @PatchMapping("/{id}")
    public UserReadDTO patchUser(@PathVariable UUID id, @RequestBody UserPatchDTO patch) {
        return applicationUserService.patchUser(id, patch);
    }

    @PutMapping("/{id}")
    public UserReadDTO updateUser(@PathVariable UUID id, @RequestBody UserPutDTO update) {
        return applicationUserService.updateUser(id, update);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        applicationUserService.deleteUser(id);
    }

    @PostMapping("/{id}/ban")
    public UserReadDTO banUser(@PathVariable UUID id) {
        return applicationUserService.ban(id);
    }

    @PostMapping("/{id}/pardon")
    public UserReadDTO pardonUser(@PathVariable UUID id) {
        return applicationUserService.pardon(id);
    }

    @PostMapping("/{id}/add-user-role")
    public UserReadDTO addUserRole(
            @PathVariable UUID id,
            @RequestBody UserRoleDTO dto
    ) {
        return applicationUserService.addUserRole(id, dto);
    }

    @PostMapping("/{id}/remove-user-role")
    public UserReadDTO removeUserRole(
            @PathVariable UUID id,
            @RequestBody UserRoleDTO dto
    ) {
        return applicationUserService.removeUserRole(id, dto);
    }
}
