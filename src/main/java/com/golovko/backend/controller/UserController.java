package com.golovko.backend.controller;

import com.golovko.backend.dto.UserCreateDTO;
import com.golovko.backend.dto.UserPatchDTO;
import com.golovko.backend.dto.UserReadDTO;
import com.golovko.backend.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

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

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable UUID id) {
        applicationUserService.deleteUser(id);
    }
}
