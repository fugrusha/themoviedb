package com.golovko.backend.controller;

import com.golovko.backend.controller.security.CurrentUser;
import com.golovko.backend.dto.like.LikeCreateDTO;
import com.golovko.backend.dto.like.LikePatchDTO;
import com.golovko.backend.dto.like.LikePutDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.service.LikeService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @ApiOperation(value = "Get single like", notes = "Needs current user authority")
    @CurrentUser
    @GetMapping("/{id}")
    public LikeReadDTO getLikeById(@PathVariable UUID userId, @PathVariable UUID id) {
        return likeService.getLike(userId, id);
    }

    @ApiOperation(value = "Create like")
    @PostMapping
    public LikeReadDTO createLike(
            @PathVariable UUID userId,
            @RequestBody @Valid LikeCreateDTO createDTO
    ) {
        return likeService.createLike(userId, createDTO);
    }

    @ApiOperation(value = "Update like", notes = "Needs current user authority")
    @CurrentUser
    @PatchMapping("/{id}")
    public LikeReadDTO patchLike(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody LikePatchDTO patchDTO
    ) {
        return likeService.patchLike(userId, id, patchDTO);
    }

    @ApiOperation(value = "Update like", notes = "Needs current user authority")
    @CurrentUser
    @PutMapping("/{id}")
    public LikeReadDTO updateLike(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody LikePutDTO updateDTO
    ) {
        return likeService.updateLike(userId, id, updateDTO);
    }

    @ApiOperation(value = "Delete like", notes = "Needs current user authority")
    @CurrentUser
    @DeleteMapping("/{id}")
    public void deleteLike(@PathVariable UUID userId, @PathVariable UUID id) {
        likeService.deleteLike(userId, id);
    }
}
