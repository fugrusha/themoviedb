package com.golovko.backend.controller;

import com.golovko.backend.controller.documentation.ApiPageable;
import com.golovko.backend.controller.security.ContentManager;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.genre.*;
import com.golovko.backend.service.GenreService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/genres")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @ApiPageable
    @ApiOperation(value = "Get all genres")
    @GetMapping
    public PageResult<GenreReadDTO> getAllGenres(@ApiIgnore Pageable pageable) {
        return genreService.getGenres(pageable);
    }

    @ApiOperation(value = "Get genre with related movies")
    @GetMapping("/{id}/extended")
    public GenreReadExtendedDTO getExtendedGenre(@PathVariable UUID id) {
        return genreService.getExtendedGenre(id);
    }

    @ApiOperation(value = "Get single genre")
    @GetMapping("/{id}")
    public GenreReadDTO getGenre(@PathVariable UUID id) {
        return genreService.getGenre(id);
    }

    @ApiOperation(value = "Create new genre", notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @PostMapping
    public GenreReadDTO createGenre(@RequestBody @Valid GenreCreateDTO createDTO) {
        return genreService.createGenre(createDTO);
    }

    @ApiOperation(value = "Update genre",
            notes = "Needs CONTENT_MANAGER authority. Empty fields will not be updated")
    @ContentManager
    @PatchMapping("/{id}")
    public GenreReadDTO patchGenre(@PathVariable UUID id, @RequestBody @Valid GenrePatchDTO patchDTO) {
        return genreService.patchGenre(id, patchDTO);
    }

    @ApiOperation(value = "Update genre",
            notes = "Needs CONTENT_MANAGER authority. All fields will be updated")
    @ContentManager
    @PutMapping("/{id}")
    public GenreReadDTO updateGenre(@PathVariable UUID id, @RequestBody @Valid GenrePutDTO putDTO) {
        return genreService.updateGenre(id, putDTO);
    }

    @ApiOperation(value = "Delete genre by id", notes = "Needs CONTENT_MANAGER authority")
    @ContentManager
    @DeleteMapping("/{id}")
    public void deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);
    }
}
