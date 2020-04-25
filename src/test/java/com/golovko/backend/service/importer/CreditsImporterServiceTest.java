package com.golovko.backend.service.importer;

import com.golovko.backend.BaseTest;
import com.golovko.backend.client.themoviedb.TheMovieDbClient;
import com.golovko.backend.client.themoviedb.dto.CastReadDTO;
import com.golovko.backend.client.themoviedb.dto.CrewReadDTO;
import com.golovko.backend.client.themoviedb.dto.MovieCreditsReadDTO;
import com.golovko.backend.client.themoviedb.dto.PersonReadDTO;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.repository.MovieRepository;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.golovko.backend.domain.Gender.*;
import static com.golovko.backend.domain.MovieCrewType.*;

public class CreditsImporterServiceTest extends BaseTest {

    @MockBean
    private TheMovieDbClient theMovieDbClient;

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private CreditsImporterService creditsImporterService;

    @Test
    public void testImportMovieCreditsWithCastAndEditorCrew() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CastReadDTO cast = createCastReadDTO();
        CrewReadDTO crew = createCrewReadDTO("Editing");
        MovieCreditsReadDTO movieCredits = createMovieCredits(List.of(cast), List.of(crew));

        PersonReadDTO castPerson = createPersonReadDTO();
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(cast.getPersonId(), null)).thenReturn(castPerson);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(EDITOR);
            Assertions.assertThat(savedMovie.getMovieCasts()).extracting("movieCrewType").contains(CAST);
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCasts()).extracting("person").isNotNull();
        });
    }

    @Test
    public void testImportMovieCastWithFemaleCharacter() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CastReadDTO cast = createCastReadDTO();
        cast.setGender(1); // female

        MovieCreditsReadDTO movieCredits = createMovieCredits(List.of(cast), new ArrayList<CrewReadDTO>());
        PersonReadDTO castPerson = createPersonReadDTO();;

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(cast.getPersonId(), null)).thenReturn(castPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCasts()).extracting("movieCrewType").contains(CAST);
            Assertions.assertThat(savedMovie.getMovieCasts()).extracting("gender").contains(FEMALE);
        });
    }

    @Test
    public void testImportMovieCastWithMaleCharacter() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CastReadDTO cast = createCastReadDTO();
        cast.setGender(2);  // male

        MovieCreditsReadDTO movieCredits = createMovieCredits(List.of(cast), new ArrayList<CrewReadDTO>());
        PersonReadDTO castPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(cast.getPersonId(), null)).thenReturn(castPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCasts()).extracting("movieCrewType").contains(CAST);
            Assertions.assertThat(savedMovie.getMovieCasts()).extracting("gender").contains(MALE);
        });
    }

    @Test
    public void testImportMovieCastWithUndefinedGender() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CastReadDTO cast = createCastReadDTO();
        cast.setGender(0);  // undefined

        MovieCreditsReadDTO movieCredits = createMovieCredits(List.of(cast), new ArrayList<CrewReadDTO>());
        PersonReadDTO castPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(cast.getPersonId(), null)).thenReturn(castPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCasts()).extracting("movieCrewType").contains(CAST);
            Assertions.assertThat(savedMovie.getMovieCasts()).extracting("gender").contains(UNDEFINED);
        });
    }

    @Test
    public void testImportMovieCreditsWithCameraCrew() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CrewReadDTO crew = createCrewReadDTO("Camera");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(CAMERA);
        });
    }

    @Test
    public void testImportMovieCreditsWithCostumeCrew() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CrewReadDTO crew = createCrewReadDTO("Costume & Make-Up");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(COSTUME_DESIGNER);
        });
    }

    @Test
    public void testImportMovieCreditsWithSoundCrew() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CrewReadDTO crew = createCrewReadDTO("Sound");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(SOUND);
        });
    }

    @Test
    public void testImportMovieCreditsWithLightingCrew() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CrewReadDTO crew = createCrewReadDTO("Lighting");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(LIGHTING);
        });
    }

    @Test
    public void testImportMovieCreditsWithArtCrew() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CrewReadDTO crew = createCrewReadDTO("Art");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(ART);
        });
    }

    @Test
    public void testImportMovieCreditsWithDirector() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CrewReadDTO crew = createCrewReadDTO("Directing");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(DIRECTOR);
        });
    }

    @Test
    public void testImportMovieCreditsWithCrew() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CrewReadDTO crew = createCrewReadDTO("Crew");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(CREW);
        });
    }

    @Test
    public void testImportMovieCreditsWithProductionCrew() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CrewReadDTO crew = createCrewReadDTO("Production");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(PRODUCER);
        });
    }

    @Test
    public void testImportMovieCreditsWithVisualEffectsCrew() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CrewReadDTO crew = createCrewReadDTO("Visual Effects");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(VISUAL_EFFECTS);
        });
    }

    @Test
    public void testImportMovieCreditsWithWritingCrew() {
        String externalMovieId = "id200";
        Movie movie = testObjectFactory.createMovie();

        CrewReadDTO crew = createCrewReadDTO("Writing");
        MovieCreditsReadDTO movieCredits = createMovieCredits(new ArrayList<CastReadDTO>(), List.of(crew));
        PersonReadDTO crewPerson = createPersonReadDTO();

        Mockito.when(theMovieDbClient.getMovieCastAndCrew(externalMovieId, null)).thenReturn(movieCredits);
        Mockito.when(theMovieDbClient.getPerson(crew.getPersonId(), null)).thenReturn(crewPerson);

        creditsImporterService.importMovieCredits(externalMovieId, movie);

        inTransaction(() -> {
            Movie savedMovie = movieRepository.findById(movie.getId()).get();

            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("person").isNotNull();
            Assertions.assertThat(savedMovie.getMovieCrews()).extracting("movieCrewType")
                    .containsExactlyInAnyOrder(WRITER);
        });
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
        dto.setBirthday(LocalDate.of(1998, 2, 10));
        dto.setGender(2);
        return dto;
    }
}
