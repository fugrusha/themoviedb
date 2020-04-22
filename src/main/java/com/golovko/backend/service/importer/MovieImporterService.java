package com.golovko.backend.service.importer;

import com.golovko.backend.client.themoviedb.TheMovieDbClient;
import com.golovko.backend.client.themoviedb.dto.GenreShortDTO;
import com.golovko.backend.client.themoviedb.dto.MovieReadDTO;
import com.golovko.backend.domain.Genre;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.exception.ImportAlreadyPerformedException;
import com.golovko.backend.exception.ImportedEntityAlreadyExistsException;
import com.golovko.backend.repository.GenreRepository;
import com.golovko.backend.repository.MovieRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class MovieImporterService {

    @Autowired
    private TheMovieDbClient theMovieDbClient;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ExternalSystemImportService externalSystemImportService;

    @Autowired
    private CreditsImporterService creditsImporterService;

    @Transactional
    public UUID importMovie(String externalMovieId)
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        log.info("Importing movie with external id={}", externalMovieId);

        externalSystemImportService.validateNotImported(Movie.class, externalMovieId);

        MovieReadDTO movieDTO = theMovieDbClient.getMovie(externalMovieId, null);

        boolean isMovieExist = movieRepository.existsMovieByMovieTitleAndReleaseDate(movieDTO.getTitle(),
                LocalDate.parse(movieDTO.getReleaseDate()));

        if (isMovieExist) {
            throw new ImportedEntityAlreadyExistsException("Movie with title="
                    + movieDTO.getTitle() + "already exists");
        }

        Movie movie = createMovie(movieDTO);
        if (CollectionUtils.isNotEmpty(movieDTO.getGenres())) {
            addGenres(movie, movieDTO.getGenres());
        }

        movieRepository.save(movie);
        externalSystemImportService.createExternalSystemImport(movie, externalMovieId);

        // TODO productionCompanies, productionCountries
        creditsImporterService.importMovieCredits(externalMovieId, movie);

        log.info("Movie with external id={} imported", externalMovieId);
        return movie.getId();
    }

    private void addGenres(Movie movie, List<GenreShortDTO> genres) throws ImportAlreadyPerformedException {
        List<Genre> genresForMovie = new ArrayList<>();

        for (GenreShortDTO dto : genres) {
            Genre genre = genreRepository.findByGenreName(dto.getName());

            if (genre == null) {
                genre = createGenre(dto.getName());
                externalSystemImportService.createExternalSystemImport(genre, dto.getId());
            }

            genresForMovie.add(genre);

            log.info("Genre {} is added to movie {}", genre.getGenreName(), movie.getMovieTitle());
        }

        movie.getGenres().addAll(genresForMovie);
    }

    private Movie createMovie(MovieReadDTO dto) {
        Movie movie = new Movie();
        movie.setMovieTitle(dto.getTitle());
        movie.setReleaseDate(LocalDate.parse(dto.getReleaseDate()));
        movie.setDescription(dto.getOverview());
        movie.setIsReleased(dto.getStatus().equals("Released"));
        movie.setRevenue(dto.getRevenue());
        movie.setRuntime(dto.getRuntime());
        return movie;
    }

    private Genre createGenre(String genreName) {
        Genre genre = new Genre();
        genre.setGenreName(genreName);
        return genreRepository.save(genre);
    }
}
