package com.golovko.backend.service;

import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.User;
import com.golovko.backend.dto.MovieCreateDTO;
import com.golovko.backend.dto.MovieReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.repository.MovieRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MovieService {

    @Autowired
    private MovieRepository movieRepository;

    public MovieReadDTO getMovie(UUID id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> {
            throw new EntityNotFoundException(User.class, id);
        });

        return toRead(movie);
    }

    private MovieReadDTO toRead(Movie movie) {
        MovieReadDTO dto = new MovieReadDTO();
        dto.setId(movie.getId());
        dto.setMovieTitle(movie.getMovieTitle());
        dto.setDescription(movie.getDescription());
        dto.setReleaseDate(movie.getReleaseDate());
        dto.setReleased(movie.isReleased());

        return dto;
    }

    public MovieReadDTO createMovie(MovieCreateDTO createDTO) {
        Movie movie = new Movie();
        movie.setMovieTitle(createDTO.getMovieTitle());
        movie.setDescription(createDTO.getDescription());
        movie.setReleaseDate(createDTO.getReleaseDate());
        movie.setReleased(createDTO.isReleased());

        movie = movieRepository.save(movie);

        return toRead(movie);
    }
}
