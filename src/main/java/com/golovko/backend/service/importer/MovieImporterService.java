package com.golovko.backend.service.importer;

import com.golovko.backend.client.themoviedb.TheMovieDbClient;
import com.golovko.backend.client.themoviedb.dto.*;
import com.golovko.backend.domain.*;
import com.golovko.backend.exception.ImportAlreadyPerformedException;
import com.golovko.backend.exception.ImportedEntityAlreadyExistsException;
import com.golovko.backend.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

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
    private MovieCrewRepository movieCrewRepository;

    @Autowired
    private MovieCastRepository movieCastRepository;

    @Autowired
    private PersonRepository personRepository;

    @Autowired
    private RepositoryHelper repoHelper;

    @Autowired
    private ExternalSystemImportService externalSystemImportService;

    @Transactional
    public UUID importMovie(String externalMovieId)
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        log.info("Importing movie with external id={}", externalMovieId);

        externalSystemImportService.validateNotImported(Movie.class, externalMovieId);

        MovieReadDTO movieDTO = theMovieDbClient.getMovie(externalMovieId, null);

        boolean isMovieExist = movieRepository.existsMovieByMovieTitle(movieDTO.getTitle());
        if (isMovieExist) {
            throw new ImportedEntityAlreadyExistsException("Movie with title="
                    + movieDTO.getTitle() + "already exists");
        }

        Movie movie = createMovie(movieDTO);
        if (movieDTO.getGenres() != null && !movieDTO.getGenres().isEmpty()) {
            addGenres(movie, movieDTO.getGenres());
        }

        movieRepository.save(movie);
        externalSystemImportService.createExternalSystemImport(movie, externalMovieId);

        // TODO productionCompanies, productionCountries, revenue, runtime
        importMovieCredits(externalMovieId, movie);

        log.info("Movie with external id={} imported", externalMovieId);
        return movie.getId();
    }

    private void addGenres(Movie movie, List<GenreShortDTO> genres) throws ImportAlreadyPerformedException {
        List<Genre> genresForMovie = new ArrayList<>();

        for (GenreShortDTO dto : genres) {
            externalSystemImportService.validateNotImported(Genre.class, dto.getId());

            Genre genre = genreRepository.findByGenreName(dto.getName());

            if (genre == null) {
                genre = createGenre(dto.getName());
            }

            genresForMovie.add(genre);
            externalSystemImportService.createExternalSystemImport(genre, dto.getId());

            log.info("Genre {} is added to movie {}", genre.getGenreName(), movie.getMovieTitle());
        }

        movie.getGenres().addAll(genresForMovie);
    }

    public void importMovieCredits(String externalMovieId, Movie movie) {
        log.info("Importing crews and casts for movie with external id={}", externalMovieId);

        MovieCreditsReadDTO readDTO = theMovieDbClient.getMovieCastAndCrew(externalMovieId, null);

        if (readDTO.getCast() != null && !readDTO.getCast().isEmpty()) {
            importCasts(readDTO.getCast(), movie);
        }
        if (readDTO.getCrew() != null && !readDTO.getCrew().isEmpty()) {
            importCrews(readDTO.getCrew(), movie);
        }

        log.info("Import credits finished for movie with external id={}", externalMovieId);

    }

    private void importCrews(List<CrewReadDTO> crews, Movie movie) {
        Set<MovieCrew> movieCrews = new HashSet<>();

        for (CrewReadDTO crewDTO : crews) {
            MovieCrew mc = new MovieCrew();
            mc.setMovie(movie);
//            mc.setPerson(importPerson(crewDTO.getPersonId())); //TODO
            mc.setDescription(crewDTO.getJob());
            mc.setMovieCrewType(getMovieCrewType(crewDTO.getDepartment()));
            movieCrewRepository.save(mc);

            externalSystemImportService.createExternalSystemImport(mc, crewDTO.getCreditId());
            movieCrews.add(mc);
        }

        movie.setMovieCrews(movieCrews);
        log.info("Imported {} crew members for movie: {}", movieCrews.size(), movie.getMovieTitle());
    }

    private void importCasts(List<CastReadDTO> casts, Movie movie) {
        Set<MovieCast> movieCasts = new HashSet<>();

        for (CastReadDTO castDTO : casts) {
            MovieCast mc = new MovieCast();
            mc.setMovie(movie);
//            mc.setPerson(importPerson(castDTO.getPersonId())); //TODO
            mc.setMovieCrewType(MovieCrewType.CAST);
            mc.setCharacter(castDTO.getCharacter());
            movieCastRepository.save(mc);

            externalSystemImportService.createExternalSystemImport(mc, castDTO.getCastId());
            movieCasts.add(mc);
        }

        movie.setMovieCasts(movieCasts);
        log.info("Imported {} cast members for movie: {}", movieCasts.size(), movie.getMovieTitle());
    }

    private MovieCrewType getMovieCrewType(String department) {

        if (department.equalsIgnoreCase("editing")) {
            return MovieCrewType.EDITOR;
        } else if (department.equalsIgnoreCase("camera")) {
            return MovieCrewType.CAMERA;
        } else if (department.contains("Costume")) {
            return MovieCrewType.COSTUME_DESIGNER;
        } else if (department.equalsIgnoreCase("sound")) {
            return MovieCrewType.SOUND;
        } else if (department.equalsIgnoreCase("lighting")) {
            return MovieCrewType.LIGHTING;
        } else if (department.equalsIgnoreCase("art")) {
            return MovieCrewType.ART;
        } else if (department.equalsIgnoreCase("directing")) {
            return MovieCrewType.DIRECTOR;
        } else if (department.equalsIgnoreCase("crew")) {
            return MovieCrewType.CREW;
        } else if (department.equalsIgnoreCase("production")) {
            return MovieCrewType.PRODUCER;
        } else if (department.equalsIgnoreCase("Visual Effects")) {
            return MovieCrewType.VISUAL_EFFECTS;
        } else if (department.equalsIgnoreCase("writing")) {
            return MovieCrewType.WRITER;
        }

        throw new IllegalArgumentException("Importing of " + department
                + " job for movie credits is not supported");
    }

    public Person importPerson(String personId) {

        UUID entityId = externalSystemImportService.getImportedEntityId(Person.class, personId);

        if (entityId == null) {
            PersonReadDTO readDTO = theMovieDbClient.getPerson(personId, null);
            Person person = createPerson(readDTO);
            externalSystemImportService.createExternalSystemImport(person, personId);

            log.info("Imported person {} with external id={}", readDTO.getName(), personId);
            return person;
        } else {
            return repoHelper.getReferenceIfExist(Person.class, entityId);
        }
    }

    private Movie createMovie(MovieReadDTO dto) {
        Movie movie = new Movie();
        movie.setMovieTitle(dto.getTitle());
        movie.setReleaseDate(LocalDate.parse(dto.getReleaseDate()));
        movie.setDescription(dto.getOverview());
        movie.setIsReleased(dto.getStatus().equals("Released"));
        return movie;
    }

    private Genre createGenre(String genreName) {
        Genre genre = new Genre();
        genre.setGenreName(genreName);
        return genreRepository.save(genre);
    }

    private Person createPerson(PersonReadDTO readDTO) {
        Person person = new Person();
        person.setFirstName(readDTO.getName().split(" ")[0]);
        person.setLastName(readDTO.getName().split(" ")[1]);
        person.setBirthday(LocalDate.parse(readDTO.getBirthday()));
        person.setPlaceOfBirth(readDTO.getPlaceOfBirth());
        person.setBio(readDTO.getBiography());
        return personRepository.save(person);
    }
}
