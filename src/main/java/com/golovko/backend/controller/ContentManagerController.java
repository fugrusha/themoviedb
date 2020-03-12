package com.golovko.backend.controller;

import com.golovko.backend.dto.misprint.MisprintConfirmDTO;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.dto.misprint.MisprintRejectDTO;
import com.golovko.backend.service.MisprintService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class ContentManagerController {

    @Autowired
    private MisprintService misprintService;

    @GetMapping("/articles/{articleId}/misprints/")
    public List<MisprintReadDTO> getAllMisprintsByArticleId(@PathVariable UUID articleId) {
        return misprintService.getMisprintsByTargetId(articleId);
    }

    @GetMapping("/articles/{articleId}/misprints/{id}")
    public MisprintReadDTO getMisprintById(@PathVariable UUID articleId, @PathVariable UUID id) {
        return misprintService.getMisprintById(articleId, id);
    }

    @PostMapping("/articles/{articleId}/misprints/{id}/confirm")
    public MisprintReadDTO confirmModeration(
            @PathVariable UUID articleId,
            @PathVariable UUID id,
            @RequestBody MisprintConfirmDTO dto
    ) {
        return misprintService.confirmModeration(articleId, id, dto);
    }

    @PostMapping("/articles/{articleId}/misprints/{id}/reject")
    public MisprintReadDTO rejectModeration(
            @PathVariable UUID articleId,
            @PathVariable UUID id,
            @RequestBody MisprintRejectDTO dto
    ) {
        return misprintService.rejectModeration(articleId, id, dto);
    }
}
