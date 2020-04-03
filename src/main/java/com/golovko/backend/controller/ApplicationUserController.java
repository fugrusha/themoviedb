package com.golovko.backend.controller;

import com.golovko.backend.controller.validation.ControllerValidationUtil;
import com.golovko.backend.dto.user.UserCreateDTO;
import com.golovko.backend.dto.user.UserPatchDTO;
import com.golovko.backend.dto.user.UserPutDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class ApplicationUserController {

    @Autowired
    private ApplicationUserService applicationUserService;

    // TODO list of users for admin

    @GetMapping("/{id}")
    public UserReadDTO getUser(@PathVariable UUID id) {
        return applicationUserService.getUser(id);
    }

    @PostMapping
    public UserReadDTO createUser(@RequestBody @Valid UserCreateDTO createDTO) {
        ControllerValidationUtil.validateEquals(createDTO.getPassword(), createDTO.getPasswordConfirmation(),
                "password", "passwordConfirmation");
        return applicationUserService.createUser(createDTO);
    }

    @PatchMapping("/{id}")
    public UserReadDTO patchUser(@PathVariable UUID id, @RequestBody @Valid UserPatchDTO patch) {
        ControllerValidationUtil.validateEquals(patch.getPassword(), patch.getPasswordConfirmation(),
                "password", "passwordConfirmation");
        return applicationUserService.patchUser(id, patch);
    }

    @PutMapping("/{id}")
    public UserReadDTO updateUser(@PathVariable UUID id, @RequestBody @Valid UserPutDTO update) {
        ControllerValidationUtil.validateEquals(update.getPassword(), update.getPasswordConfirmation(),
                "password", "passwordConfirmation");
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
}
