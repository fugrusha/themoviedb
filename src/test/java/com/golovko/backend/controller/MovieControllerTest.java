package com.golovko.backend.controller;


import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.domain.MovieCrewType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.genre.GenreReadDTO;
import com.golovko.backend.dto.movie.*;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.dto.moviecrew.MovieCrewReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.MovieService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MovieController.class)
public class MovieControllerTest extends BaseControllerTest {

    @MockBean
    private MovieService movieService;

    @Test
    public void testGetMovieById() throws Exception {
        MovieReadDTO dto = createMovieReadDTO();

        Mockito.when(movieService.getMovie(dto.getId())).thenReturn(dto);

        String resultJSON = mockMvc
                .perform(get("/api/v1/movies/{id}", dto.getId()))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        MovieReadDTO actualMovie = objectMapper.readValue(resultJSON, MovieReadDTO.class);
        Assertions.assertThat(actualMovie).isEqualToComparingFieldByField(dto);

        Mockito.verify(movieService).getMovie(dto.getId());
    }

    @Test
    public void testGetMovieExtended() throws Exception {
        List<GenreReadDTO> genres = List.of(createGenreReadDTO());
        List<MovieCastReadDTO> movieCasts = List.of(createMovieCastReadDTO());
        List<MovieCrewReadDTO> movieCrews = List.of(createMovieCrewReadDTO());

        MovieReadExtendedDTO movieExtendedDTO = createMovieReadExtendedDTO(genres, movieCasts, movieCrews);

        Mockito.when(movieService.getMovieExtended(movieExtendedDTO.getId())).thenReturn(movieExtendedDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{id}/extended", movieExtendedDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();


        MovieReadExtendedDTO actualResult = objectMapper.readValue(resultJson, MovieReadExtendedDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(movieExtendedDTO);

        Mockito.verify(movieService).getMovieExtended(movieExtendedDTO.getId());
    }

    @Test
    public void testGetMovieWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);
        Mockito.when(movieService.getMovie(wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/movies/{id}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @WithMockUser
    @Test
    public void testCreateMovie() throws Exception {
        MovieCreateDTO createDTO = new MovieCreateDTO();
        createDTO.setMovieTitle("Guess Who");
        createDTO.setDescription("12345");
        createDTO.setReleaseDate(LocalDate.parse("1990-12-05"));
        createDTO.setIsReleased(false);

        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(movieService.createMovie(createDTO)).thenReturn(readDTO);

        String result = mockMvc
                .perform(post("/api/v1/movies")
                .content(objectMapper.writeValueAsString(createDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieReadDTO actualMovie = objectMapper.readValue(result, MovieReadDTO.class);
        Assertions.assertThat(actualMovie).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testCreateMovieNotNullValidationException() throws Exception {
        MovieCreateDTO createDTO = new MovieCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieService, Mockito.never()).createMovie(any());
    }

    @WithMockUser
    @Test
    public void testCreateMovieMinSizeValidationException() throws Exception {
        MovieCreateDTO createDTO = new MovieCreateDTO();
        createDTO.setMovieTitle("Guess Who".repeat(100));
        createDTO.setDescription("12345".repeat(1000));
        createDTO.setPosterUrl("poster url".repeat(1000));
        createDTO.setTrailerUrl("trailer url".repeat(1000));
        createDTO.setReleaseDate(LocalDate.parse("1990-12-05"));
        createDTO.setIsReleased(false);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieService, Mockito.never()).createMovie(any());
    }

    @WithMockUser
    @Test
    public void testCreateMovieMaxSizeValidationException() throws Exception {
        MovieCreateDTO createDTO = new MovieCreateDTO();
        createDTO.setMovieTitle("");
        createDTO.setDescription("");
        createDTO.setPosterUrl("");
        createDTO.setTrailerUrl("");
        createDTO.setReleaseDate(LocalDate.parse("1990-12-05"));
        createDTO.setIsReleased(false);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieService, Mockito.never()).createMovie(any());
    }

    @WithMockUser
    @Test
    public void testCreateMoviePositiveOrZeroValidationException() throws Exception {
        MovieCreateDTO createDTO = new MovieCreateDTO();
        createDTO.setMovieTitle("title");
        createDTO.setDescription("text");
        createDTO.setReleaseDate(LocalDate.parse("1990-12-05"));
        createDTO.setIsReleased(false);
        createDTO.setRevenue(-20);
        createDTO.setRuntime(-10);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieService, Mockito.never()).createMovie(any());
    }

    @WithMockUser
    @Test
    public void testPatchMovie() throws Exception {
        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setMovieTitle("title");
        patchDTO.setDescription("some description");
        patchDTO.setIsReleased(true);
        patchDTO.setReleaseDate(LocalDate.parse("1800-07-10"));
        patchDTO.setRevenue(10000);
        patchDTO.setRuntime(130);

        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(movieService.patchMovie(readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{id}", readDTO.getId())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieReadDTO actualMovie = objectMapper.readValue(resultJson, MovieReadDTO.class);
        Assert.assertEquals(readDTO, actualMovie);
    }

    @WithMockUser
    @Test
    public void testPatchMovieMinSizeValidationException() throws Exception {
        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setMovieTitle("");
        patchDTO.setDescription("");

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieService, Mockito.never()).patchMovie(any(), any());
    }

    @WithMockUser
    @Test
    public void testPatchMovieMaxSizeValidationException() throws Exception {
        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setMovieTitle("Guess Who".repeat(100));
        patchDTO.setDescription("12345".repeat(1000));

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieService, Mockito.never()).patchMovie(any(), any());
    }

    @WithMockUser
    @Test
    public void testPatchMoviePositiveOrZeroValidationException() throws Exception {
        MoviePatchDTO patchDTO = new MoviePatchDTO();
        patchDTO.setRuntime(-10);
        patchDTO.setRevenue(-10);

        String resultJson = mockMvc
                .perform(patch("/api/v1/movies/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieService, Mockito.never()).patchMovie(any(), any());
    }

    @WithMockUser
    @Test
    public void testUpdateMovie() throws Exception {
        MoviePutDTO updateDTO = new MoviePutDTO();
        updateDTO.setMovieTitle("new title");
        updateDTO.setDescription("some NEW description");
        updateDTO.setIsReleased(false);
        updateDTO.setReleaseDate(LocalDate.parse("1900-07-10"));

        MovieReadDTO readDTO = createMovieReadDTO();

        Mockito.when(movieService.updateMovie(readDTO.getId(), updateDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{id}", readDTO.getId())
                .content(objectMapper.writeValueAsString(updateDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieReadDTO actualMovie = objectMapper.readValue(resultJson, MovieReadDTO.class);
        Assert.assertEquals(readDTO, actualMovie);
    }

    @WithMockUser
    @Test
    public void testUpdateMovieMinSizeValidationException() throws Exception {
        MoviePutDTO updateDTO = new MoviePutDTO();
        updateDTO.setMovieTitle("");
        updateDTO.setDescription("");
        updateDTO.setReleaseDate(LocalDate.parse("1990-12-05"));
        updateDTO.setIsReleased(false);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieService, Mockito.never()).patchMovie(any(), any());
    }

    @WithMockUser
    @Test
    public void testUpdateMovieMaxSizeValidationException() throws Exception {
        MoviePutDTO updateDTO = new MoviePutDTO();
        updateDTO.setMovieTitle("Guess Who".repeat(100));
        updateDTO.setDescription("12345".repeat(1000));
        updateDTO.setReleaseDate(LocalDate.parse("1990-12-05"));
        updateDTO.setIsReleased(false);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieService, Mockito.never()).patchMovie(any(), any());
    }

    @WithMockUser
    @Test
    public void testUpdateMoviePositiveOrZeroValidationException() throws Exception {
        MoviePutDTO updateDTO = new MoviePutDTO();
        updateDTO.setRevenue(-10);
        updateDTO.setRuntime(-10);

        String resultJson = mockMvc
                .perform(put("/api/v1/movies/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(movieService, Mockito.never()).patchMovie(any(), any());
    }

    @WithMockUser
    @Test
    public void testDeleteMovie() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movies/{id}", id))
                .andExpect(status().isOk());

        Mockito.verify(movieService).deleteMovie(id);
    }

    @Test
    public void testGetMoviesWithFilter() throws Exception {
        MovieFilter filter = new MovieFilter();
        filter.setPersonId(UUID.randomUUID());
        filter.setMovieCrewTypes(Set.of(MovieCrewType.SOUND, MovieCrewType.WRITER));
        filter.setReleasedFrom(LocalDate.parse("1980-07-10"));
        filter.setReleasedTo(LocalDate.parse("1992-07-10"));
        filter.setGenreNames(Set.of("Comedy", "Fantasy"));

        MovieReadDTO movieReadDTO = new MovieReadDTO();
        movieReadDTO.setId(UUID.randomUUID());
        movieReadDTO.setMovieTitle("Title text");
        movieReadDTO.setDescription("Some text");
        movieReadDTO.setAverageRating(5.4);
        movieReadDTO.setReleaseDate(LocalDate.parse("1985-01-10"));
        movieReadDTO.setIsReleased(true);

        MovieCrewReadDTO movieCrewReadDTO = new MovieCrewReadDTO();
        movieCrewReadDTO.setMovieId(movieReadDTO.getId());
        movieCrewReadDTO.setPersonId(filter.getPersonId());
        movieCrewReadDTO.setMovieCrewType(MovieCrewType.WRITER);
        movieCrewReadDTO.setId(UUID.randomUUID());
        movieCrewReadDTO.setAverageRating(5.4);
        movieCrewReadDTO.setDescription("Some text");

        PageResult<MovieReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(movieReadDTO));

        Mockito.when(movieService.getMovies(filter, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc.perform(get("/api/v1/movies")
            .param("personId", filter.getPersonId().toString())
            .param("movieCrewTypes", "SOUND, WRITER")
            .param("releasedFrom", filter.getReleasedFrom().toString())
            .param("releasedTo", filter.getReleasedTo().toString())
            .param("genreNames", "Comedy, Fantasy"))
            .andDo(MockMvcResultHandlers.print())
            .andExpect(status().isOk())
            .andReturn().getResponse().getContentAsString();

        PageResult<MovieReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(pageResult, actualResult);

        Mockito.verify(movieService).getMovies(filter, PageRequest.of(0, defaultPageSize));
    }

    @Test
    public void testGetMoviesWithPagingAndSorting() throws Exception {
        MovieReadDTO readDTO = createMovieReadDTO();
        MovieFilter filter = new MovieFilter();

        int page = 1;
        int size = 25;

        PageResult<MovieReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(100);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "releaseDate"));

        Mockito.when(movieService.getMovies(filter, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies")
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "releaseDate,desc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MovieReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @Test
    public void testGetMoviesLeaderBoardWithPagingAndSorting() throws Exception {
        MoviesTopRatedDTO readDTO = createMovieInLeaderBoardDTO();

        int page = 1;
        int size = 25;

        PageResult<MoviesTopRatedDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(100);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "averageRating"));

        Mockito.when(movieService.getTopRatedMovies(pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/top-rated")
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "averageRating,desc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MoviesTopRatedDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    private MoviesTopRatedDTO createMovieInLeaderBoardDTO() {
        return generateObject(MoviesTopRatedDTO.class);
    }

    private MovieReadDTO createMovieReadDTO() {
        return generateObject(MovieReadDTO.class);
    }

    private MovieReadExtendedDTO createMovieReadExtendedDTO(
            List<GenreReadDTO> genres,
            List<MovieCastReadDTO> movieCasts,
            List<MovieCrewReadDTO> movieCrews
    ) {
       MovieReadExtendedDTO dto = generateObject(MovieReadExtendedDTO.class);
        dto.setGenres(genres);
        dto.setMovieCrews(movieCrews);
        dto.setMovieCasts(movieCasts);
        return dto;
    }

    private MovieCastReadDTO createMovieCastReadDTO() {
        return generateObject(MovieCastReadDTO.class);
    }

    private MovieCrewReadDTO createMovieCrewReadDTO() {
        return generateObject(MovieCrewReadDTO.class);
    }

    private GenreReadDTO createGenreReadDTO() {
        return generateObject(GenreReadDTO.class);
    }
}