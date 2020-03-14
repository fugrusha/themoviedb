package com.golovko.backend.controller;

import com.golovko.backend.dto.like.LikeCreateDTO;
import com.golovko.backend.dto.like.LikePatchDTO;
import com.golovko.backend.dto.like.LikePutDTO;
import com.golovko.backend.dto.like.LikeReadDTO;
import com.golovko.backend.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/likes")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @GetMapping("/{id}")
    public LikeReadDTO getLikeById(@PathVariable UUID userId, @PathVariable UUID id) {
        return likeService.getLike(userId, id);
    }

    @PostMapping
    public LikeReadDTO createLike(@PathVariable UUID userId, @RequestBody LikeCreateDTO createDTO) {
        return likeService.createLike(userId, createDTO);
    }

    @PatchMapping("/{id}")
    public LikeReadDTO patchLike(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody LikePatchDTO patchDTO) {
        return likeService.patchLike(userId, id, patchDTO);
    }

    @PutMapping("/{id}")
    public LikeReadDTO updateLike(
            @PathVariable UUID userId,
            @PathVariable UUID id,
            @RequestBody LikePutDTO updateDTO) {
        return likeService.updateLike(userId, id, updateDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteLike(@PathVariable UUID userId, @PathVariable UUID id) {
        likeService.deleteLike(userId, id);
    }
}
