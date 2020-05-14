package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.*;
import com.golovko.backend.controller.validation.ControllerValidationUtil;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.user.*;
import com.golovko.backend.service.ApplicationUserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class ApplicationUserController {

    @Autowired
    private ApplicationUserService applicationUserService;

    @ApiPageable
    @ApiOperation(value = "Get all users", notes = "Needs ADMIN authority")
    @Admin
    @GetMapping
    public PageResult<UserReadDTO> getAllUsers(@ApiIgnore Pageable pageable) {
        return applicationUserService.getAllUsers(pageable);
    }

    @ApiOperation(value = "Get users in leader board",
            notes = "Needs USER authority. Sorted by trust level of user")
    @PreAuthorize("hasAuthority('USER')")
    @GetMapping("/leader-board")
    public List<UserInLeaderBoardDTO> getUsersLeaderBoard() {
        return applicationUserService.getUsersLeaderBoard();
    }

    @ApiOperation(value = "Get user by userId",
            notes = "Needs ADMIN or MODERATOR or current user authorities.")
    @AdminOrModeratorOrCurrentUser
    @GetMapping("/{userId}")
    public UserReadDTO getUser(@PathVariable UUID userId) {
        return applicationUserService.getUser(userId);
    }

    @ApiOperation(value = "Get user details by userId",
            notes = "Needs ADMIN or MODERATOR or current user authorities .")
    @AdminOrModeratorOrCurrentUser
    @GetMapping("/{userId}/extended")
    public UserReadExtendedDTO getExtendedUser(@PathVariable UUID userId) {
        return applicationUserService.getExtendedUser(userId);
    }

    @ApiOperation(value = "Create user")
    @PostMapping
    public UserReadDTO createUser(@RequestBody @Valid UserCreateDTO createDTO) {
        ControllerValidationUtil.validateEquals(createDTO.getPassword(), createDTO.getPasswordConfirmation(),
                "password", "passwordConfirmation");
        return applicationUserService.createUser(createDTO);
    }

    @AdminOrCurrentUser
    @ApiOperation(value = "Update user",
            notes = "Needs ADMIN or current user authorities. Empty fields will not be updated.")
    @PatchMapping("/{userId}")
    public UserReadDTO patchUser(@PathVariable UUID userId, @RequestBody @Valid UserPatchDTO patch) {
        return applicationUserService.patchUser(userId, patch);
    }

    @AdminOrCurrentUser
    @ApiOperation(value = "Update user",
            notes = "Needs ADMIN or current user authorities. All fields will be updated.")
    @PutMapping("/{userId}")
    public UserReadDTO updateUser(@PathVariable UUID userId, @RequestBody @Valid UserPutDTO update) {
        return applicationUserService.updateUser(userId, update);
    }

    @Admin
    @ApiOperation(value = "Delete user by userId", notes = "Needs ADMIN authority.")
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable UUID userId) {
        applicationUserService.deleteUser(userId);
    }

    @AdminOrModerator
    @ApiOperation(value = "Block(ban) user by userId", notes = "Needs ADMIN authority.")
    @PostMapping("/{userId}/ban")
    public UserReadDTO banUser(@PathVariable UUID userId) {
        return applicationUserService.ban(userId);
    }

    @AdminOrModerator
    @ApiOperation(value = "Unblock user by userId", notes = "Needs ADMIN authority.")
    @PostMapping("/{userId}/pardon")
    public UserReadDTO pardonUser(@PathVariable UUID userId) {
        return applicationUserService.pardon(userId);
    }

    @CurrentUser
    @ApiOperation(value = "Reset password", notes = "Needs current user authority.")
    @PostMapping("/{userId}/reset-password")
    public UserReadDTO resetPassword(@PathVariable UUID userId, @RequestBody @Valid ResetPasswordDTO resetDTO) {
        ControllerValidationUtil.validateEquals(resetDTO.getPassword(), resetDTO.getPasswordConfirmation(),
                "password", "passwordConfirmation");
        return applicationUserService.resetPassword(userId, resetDTO);
    }
}
