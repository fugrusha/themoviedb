package com.golovko.backend.controller;

import com.golovko.backend.controller.validation.ControllerValidationUtil;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.user.*;
import com.golovko.backend.service.ApplicationUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class ApplicationUserController {

    @Autowired
    private ApplicationUserService applicationUserService;

    @GetMapping
    public PageResult<UserReadDTO> getAllUsers(Pageable pageable) {
        return applicationUserService.getAllUsers(pageable);
    }

    @GetMapping("/leader-board")
    public List<UserInLeaderBoardDTO> getUsersLeaderBoard() {
        return applicationUserService.getUsersLeaderBoard();
    }

    @GetMapping("/{id}")
    public UserReadDTO getUser(@PathVariable UUID id) {
        return applicationUserService.getUser(id);
    }

    @GetMapping("/{id}/extended")
    public UserReadExtendedDTO getExtendedUser(@PathVariable UUID id) {
        return applicationUserService.getExtendedUser(id);
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
