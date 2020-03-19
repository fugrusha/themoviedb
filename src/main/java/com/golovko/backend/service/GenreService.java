package com.golovko.backend.service;

import com.golovko.backend.domain.Genre;
import com.golovko.backend.dto.genre.*;
import com.golovko.backend.repository.GenreRepository;
import com.golovko.backend.repository.RepositoryHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class GenreService {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private TranslationService translationService;

    @Autowired
    private RepositoryHelper repoHelper;

    public List<GenreReadDTO> getAllGenres() {
        List<Genre> genres = genreRepository.findAllByOrderByGenreNameAsc();

        return genres.stream()
                .map(g -> translationService.translate(g, GenreReadDTO.class))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GenreReadExtendedDTO getExtendedGenre(UUID id) {
        Genre genre = repoHelper.getEntityById(Genre.class, id);
        return translationService.translate(genre, GenreReadExtendedDTO.class);
    }

    public GenreReadDTO getGenre(UUID id) {
        Genre genre = repoHelper.getEntityById(Genre.class, id);
        return translationService.translate(genre, GenreReadDTO.class);
    }

    public GenreReadDTO createGenre(GenreCreateDTO createDTO) {
        Genre genre = translationService.translate(createDTO, Genre.class);

        genre = genreRepository.save(genre);

        return translationService.translate(genre, GenreReadDTO.class);
    }

    public GenreReadDTO patchGenre(UUID id, GenrePatchDTO patchDTO) {
        Genre genre = repoHelper.getEntityById(Genre.class, id);

        translationService.map(patchDTO, genre);
        genre = genreRepository.save(genre);

        return translationService.translate(genre, GenreReadDTO.class);
    }

    public GenreReadDTO updateGenre(UUID id, GenrePutDTO putDTO) {
        Genre genre = repoHelper.getEntityById(Genre.class, id);

        translationService.map(putDTO, genre);
        genre = genreRepository.save(genre);

        return translationService.translate(genre, GenreReadDTO.class);
    }

    public void deleteGenre(UUID id) {
        genreRepository.delete(repoHelper.getEntityById(Genre.class, id));
    }
}
