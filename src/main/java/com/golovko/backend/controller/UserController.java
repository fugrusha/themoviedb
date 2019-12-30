package com.golovko.backend.controller;

import com.golovko.backend.dto.UserCreateDTO;
import com.golovko.backend.dto.UserReadDTO;
import com.golovko.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public UserReadDTO getUser(@PathVariable UUID id) {
        return userService.getUser(id);
    }

    @PostMapping
    public UserReadDTO createUser(@RequestBody UserCreateDTO createDTO) {
        return userService.createUser(createDTO);
    }
}
