package com.golovko.backend.controller;

import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.genre.*;
import com.golovko.backend.service.GenreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/genres")
public class GenreController {

    @Autowired
    private GenreService genreService;

    @GetMapping
    public PageResult<GenreReadDTO> getAllGenres(Pageable pageable) {
        return genreService.getGenres(pageable);
    }

    @GetMapping("/{id}/extended")
    public GenreReadExtendedDTO getExtendedGenre(@PathVariable UUID id) {
        return genreService.getExtendedGenre(id);
    }

    @GetMapping("/{id}")
    public GenreReadDTO getGenre(@PathVariable UUID id) {
        return genreService.getGenre(id);
    }

    @PostMapping
    public GenreReadDTO createGenre(@RequestBody @Valid GenreCreateDTO createDTO) {
        return genreService.createGenre(createDTO);
    }

    @PatchMapping("/{id}")
    public GenreReadDTO patchGenre(@PathVariable UUID id, @RequestBody @Valid GenrePatchDTO patchDTO) {
        return genreService.patchGenre(id, patchDTO);
    }

    @PutMapping("/{id}")
    public GenreReadDTO updateGenre(@PathVariable UUID id, @RequestBody @Valid GenrePutDTO putDTO) {
        return genreService.updateGenre(id, putDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteGenre(@PathVariable UUID id) {
        genreService.deleteGenre(id);
    }
}
