package com.golovko.backend.service.importer;

import com.golovko.backend.BaseTest;
import com.golovko.backend.client.themoviedb.TheMovieDbClient;
import com.golovko.backend.client.themoviedb.dto.*;
import com.golovko.backend.domain.*;
import com.golovko.backend.exception.ImportAlreadyPerformedException;
import com.golovko.backend.exception.ImportedEntityAlreadyExistsException;
import com.golovko.backend.repository.ExternalSystemImportRepository;
import com.golovko.backend.repository.MovieRepository;
import com.golovko.backend.repository.PersonRepository;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.golovko.backend.domain.Gender.*;
import static com.golovko.backend.domain.ImportedEntityType.PERSON;
import static com.golovko.backend.domain.MovieCrewType.*;
import static org.mockito.ArgumentMatchers.any;

public class MovieImporterServiceTest extends BaseTest {

    @MockBean
    private TheMovieDbClient theMovieDbClient;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private ExternalSystemImportRepository esiRepository;

    @Autowired
    private PersonRepository personRepository;

    @SpyBean
    private MovieImporterService movieImporterService;

    @Test
    public void testMovieImport()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";
        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(readDTO);
        Mockito.doNothing().when(movieImporterService).importMovieCredits(any(), any());

        UUID movieId = movieImporterService.importMovie(externalMovieId);
        Movie movie = movieRepository.findById(movieId).get();

        Assert.assertEquals(readDTO.getTitle(), movie.getMovieTitle());
    }

    @Test
    public void testMovieImportAlreadyExists() {
        String externalMovieId = "id200";

        Movie existingMovie = testObjectFactory.createMovie();

        MovieReadDTO readDTO = createMovieReadDTO();
        readDTO.setTitle(existingMovie.getMovieTitle());
        readDTO.setReleaseDate(existingMovie.getReleaseDate().toString());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(readDTO);

        ImportedEntityAlreadyExistsException ex = Assertions.catchThrowableOfType(
                () -> movieImporterService.importMovie(externalMovieId),
                ImportedEntityAlreadyExistsException.class);

        Assertions.assertThat(ex.getMessage()).contains(existingMovie.getMovieTitle());
    }

    @Test
    public void testNoCallToClientOnDuplicateImport()
            throws ImportAlreadyPerformedException, ImportedEntityAlreadyExistsException {
        String externalMovieId = "id200";
        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(readDTO);
        Mockito.doNothing().when(movieImporterService).importMovieCredits(any(), any());

        movieImporterService.importMovie(externalMovieId);
        Mockito.verify(theMovieDbClient).getMovie(externalMovieId, null);
        Mockito.reset(theMovieDbClient);

        Assertions.assertThatThrownBy(() -> movieImporterService.importMovie(externalMovieId))
                .isInstanceOf(ImportAlreadyPerformedException.class);

        Mockito.verifyNoInteractions(theMovieDbClient);
    }

    @Test
    public void testMovieImportWithNotExistedGenre()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        GenreShortDTO genreDTO = createGenreShortDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(List.of(genreDTO));

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.doNothing().when(movieImporterService).importMovieCredits(any(), any());

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertNotNull(movie.getGenres());
        });
    }

    @Test
    public void testMovieImportWithExistedGenre()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        Genre genre = testObjectFactory.createGenre("Thriller");
        GenreShortDTO genreDTO = createGenreShortDTO();
        genreDTO.setName(genre.getGenreName());

        MovieReadDTO movieDTO = createMovieWithGenresDTO(List.of(genreDTO));

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.doNothing().when(movieImporterService).importMovieCredits(any(), any());

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertEquals(1, movieDTO.getGenres().size());
            Assertions.assertThat(movie.getGenres()).extracting("genreName")
                    .contains(genreDTO.getName());
        });
    }

    @Test
    public void testMovieImportWithEmptyGenres()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.doNothing().when(movieImporterService).importMovieCredits(any(), any());

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());
        });
    }

    @Test
    public void testMovieImportWithCastAndEditorCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CastReadDTO cast = createCastReadDTO();
        CrewReadDTO crew = createCrewReadDTO("Editing");
        MovieCreditsReadDTO movieCredits = createMovieCredits(List.of(cast), List.of(crew));

        PersonReadDTO castPerson = createPersonReadDTO();
        PersonReadDTO crewPerson = createPersonReadDTO();

        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(cast.getPersonId(), null)).thenReturn(castPerson);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCasts()).extracting("movieCrewType").contains(CAST);

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(EDITOR);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testMovieImportCastWithFemaleCharacter()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CastReadDTO cast = createCastReadDTO();
        cast.setGender(1); // female

        MovieCreditsReadDTO movieCredits = createMovieCredits(List.of(cast), new ArrayList<CrewReadDTO>());
        PersonReadDTO castPerson = createPersonReadDTO();

        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(cast.getPersonId(), null)).thenReturn(castPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assertions.assertThat(movie.getMovieCasts()).extracting("movieCrewType").contains(CAST);
            Assertions.assertThat(movie.getMovieCasts()).extracting("gender").contains(FEMALE);
        });
    }

    @Test
    public void testMovieImportCastWithMaleCharacter()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CastReadDTO cast = createCastReadDTO();
        cast.setGender(2);  // male

        MovieCreditsReadDTO movieCredits = createMovieCredits(List.of(cast), new ArrayList<CrewReadDTO>());
        PersonReadDTO castPerson = createPersonReadDTO();

        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(cast.getPersonId(), null)).thenReturn(castPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assertions.assertThat(movie.getMovieCasts()).extracting("movieCrewType").contains(CAST);
            Assertions.assertThat(movie.getMovieCasts()).extracting("gender").contains(MALE);
        });
    }

    @Test
    public void testMovieImportCastWithUndefinedGender()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CastReadDTO cast = createCastReadDTO();
        cast.setGender(0);  // undefined

        MovieCreditsReadDTO movieCredits = createMovieCredits(List.of(cast), new ArrayList<CrewReadDTO>());
        PersonReadDTO castPerson = createPersonReadDTO();

        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(cast.getPersonId(), null)).thenReturn(castPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assertions.assertThat(movie.getMovieCasts()).extracting("movieCrewType").contains(CAST);
            Assertions.assertThat(movie.getMovieCasts()).extracting("gender").contains(UNDEFINED);
        });
    }

    @Test
    public void testMovieImportWithCameraCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Camera");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(CAMERA);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testMovieImportWithCostumeCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Costume & Make-Up");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(COSTUME_DESIGNER);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testMovieImportWithSoundCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Sound");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(SOUND);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testMovieImportWithLightingCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Lighting");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(LIGHTING);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testMovieImportWithArtCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Art");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(ART);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testMovieImportWithDirectingCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Directing");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(DIRECTOR);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testMovieImportWithCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Crew");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(CREW);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testMovieImportProductionCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Production");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(PRODUCER);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testMovieImportVisualEffectsCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Visual Effects");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(VISUAL_EFFECTS);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testMovieImportWritingCrew()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Writing");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());
            Assert.assertTrue(movie.getGenres().isEmpty());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(WRITER);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testImportAlreadyImportedPerson()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        CrewReadDTO crew = createCrewReadDTO("Writing");
        Person p1 = testObjectFactory.createPerson();
        createESI(p1.getId(), crew.getPersonId(), ImportedEntityType.PERSON);
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        Mockito.verify(theMovieDbClient, Mockito.never()).getPerson(any(), any());

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(WRITER);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testImportAlreadyExistedPerson()
            throws ImportedEntityAlreadyExistsException, ImportAlreadyPerformedException {
        String externalMovieId = "id200";

        Person p1 = testObjectFactory.createPerson(); // create person
        PersonReadDTO personDTO = createPersonReadDTO();
        personDTO.setName(p1.getFirstName() + " " + p1.getLastName());

        CrewReadDTO crew = createCrewReadDTO("Writing");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        MovieReadDTO movieDTO = createMovieWithGenresDTO(new ArrayList<GenreShortDTO>());

        Mockito.when(theMovieDbClient.getMovie(externalMovieId, null)).thenReturn(movieDTO);
        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(personDTO);

        UUID movieId = movieImporterService.importMovie(externalMovieId);

        inTransaction(() -> {
            Movie movie = movieRepository.findById(movieId).get();

            Assert.assertEquals(movieDTO.getTitle(), movie.getMovieTitle());

            Assertions.assertThat(movie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(WRITER);

            Assertions.assertThat(movie.getMovieCrews()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testImportPersonWithNullFields() {
        PersonReadDTO personDTO = createPersonReadDTO();
        personDTO.setBirthday(null);
        personDTO.setBiography(null);
        personDTO.setPlaceOfBirth(null);

        String expectedBio = String.format("Biography for %s will be added later", personDTO.getName());
        LocalDate expectedBirthday = LocalDate.of(1900, 1, 1);
        String expectedPlaceOfBirth = "Unknown";

        Mockito.when(theMovieDbClient.getPerson(personDTO.getId(), null)).thenReturn(personDTO);

        movieImporterService.importPerson(personDTO.getId());

        ExternalSystemImport actualEsi = esiRepository
                .findByIdInExternalSystemAndEntityType(personDTO.getId(), PERSON);

        Person savedPerson = personRepository.findById(actualEsi.getEntityId()).get();
        Assert.assertEquals(expectedBio, savedPerson.getBio());
        Assert.assertEquals(expectedBirthday, savedPerson.getBirthday());
        Assert.assertEquals(expectedPlaceOfBirth, savedPerson.getPlaceOfBirth());
    }

    @Test
    public void testImportPersonWithMaleGender() {
        PersonReadDTO personDTO = createPersonReadDTO();
        personDTO.setGender(2); // male

        Mockito.when(theMovieDbClient.getPerson(personDTO.getId(), null)).thenReturn(personDTO);

        movieImporterService.importPerson(personDTO.getId());

        ExternalSystemImport actualEsi = esiRepository
                .findByIdInExternalSystemAndEntityType(personDTO.getId(), PERSON);

        Person savedPerson = personRepository.findById(actualEsi.getEntityId()).get();
        Assert.assertEquals(Gender.MALE, savedPerson.getGender());
    }

    @Test
    public void testImportPersonWithFemaleGender() {
        PersonReadDTO personDTO = createPersonReadDTO();
        personDTO.setGender(1); // female

        Mockito.when(theMovieDbClient.getPerson(personDTO.getId(), null)).thenReturn(personDTO);

        movieImporterService.importPerson(personDTO.getId());

        ExternalSystemImport actualEsi = esiRepository
                .findByIdInExternalSystemAndEntityType(personDTO.getId(), PERSON);

        Person savedPerson = personRepository.findById(actualEsi.getEntityId()).get();
        Assert.assertEquals(FEMALE, savedPerson.getGender());
    }

    @Test
    public void testImportPersonWithUndefinedGender() {
        PersonReadDTO personDTO = createPersonReadDTO();
        personDTO.setGender(0); // undefined

        Mockito.when(theMovieDbClient.getPerson(personDTO.getId(), null)).thenReturn(personDTO);

        movieImporterService.importPerson(personDTO.getId());

        ExternalSystemImport actualEsi = esiRepository
                .findByIdInExternalSystemAndEntityType(personDTO.getId(), PERSON);

        Person savedPerson = personRepository.findById(actualEsi.getEntityId()).get();
        Assert.assertEquals(Gender.UNDEFINED, savedPerson.getGender());
    }

    private ExternalSystemImport createESI(
            UUID entityId,
            String idInExternalSystem,
            ImportedEntityType entityType
    ) {
        ExternalSystemImport esi = new ExternalSystemImport();
        esi.setEntityId(entityId);
        esi.setIdInExternalSystem(idInExternalSystem);
        esi.setEntityType(entityType);
        return esiRepository.save(esi);
    }

    private MovieReadDTO createMovieReadDTO() {
        MovieReadDTO dto = generateObject(MovieReadDTO.class);
        dto.setReleaseDate("1998-12-11");
        dto.setStatus("Released");
        return dto;
    }

    private MovieReadDTO createMovieWithGenresDTO(List<GenreShortDTO> genres) {
        MovieReadDTO dto = generateObject(MovieReadDTO.class);
        dto.setReleaseDate("1998-12-11");
        dto.setStatus("Released");
        dto.setGenres(genres);
        return dto;
    }

    private GenreShortDTO createGenreShortDTO() {
        return generateObject(GenreShortDTO.class);
    }

    private MovieCreditsReadDTO createMovieCredits(List<CastReadDTO> cast, List<CrewReadDTO> crew) {
        MovieCreditsReadDTO dto = generateObject(MovieCreditsReadDTO.class);
        dto.setCast(cast);
        dto.setCrew(crew);
        return dto;
    }

    private CastReadDTO createCastReadDTO() {
        return generateObject(CastReadDTO.class);
    }

    private CrewReadDTO createCrewReadDTO(String department) {
        CrewReadDTO dto = generateObject(CrewReadDTO.class);
        dto.setDepartment(department);
        return dto;
    }

    private PersonReadDTO createPersonReadDTO() {
        PersonReadDTO dto = generateObject(PersonReadDTO.class);
        dto.setName("Alisa Winter");
        dto.setBirthday("1988-05-12");
        dto.setGender(2);
        return dto;
    }
}
