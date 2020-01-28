package com.golovko.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.MovieCast;
import com.golovko.backend.domain.PartType;
import com.golovko.backend.dto.moviecast.MovieCastReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.MovieCastService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MovieCastController.class)
public class MovieCastControllerTest {

    @MockBean
    private MovieCastService movieCastService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getMovieCastTest() throws Exception {
        MovieCastReadDTO readDTO = createMovieCastDTO();

        Mockito.when(movieCastService.getMovieCast(readDTO.getId())).thenReturn(readDTO);

        String result = mockMvc.perform(get("/api/v1/movie-cast/{id}", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieCastReadDTO actualMovieCast = objectMapper.readValue(result, MovieCastReadDTO.class);
        Assertions.assertThat(actualMovieCast).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(movieCastService).getMovieCast(readDTO.getId());
    }

    @Test
    public void getMovieCastWrongIdTest() throws Exception {
        UUID id = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(MovieCast.class, id);
        Mockito.when(movieCastService.getMovieCast(id)).thenThrow(exception);

        String resultJson = mockMvc.perform(get("/api/v1/movie-cast/{id}", id))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(exception.getMessage()));
    }

    @Test
    public void deleteMovieCastTest() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/movie-cast/{id}", id.toString()))
                .andExpect(status().isOk());

        Mockito.verify(movieCastService).deleteMovieCast(id);
    }

    private MovieCastReadDTO createMovieCastDTO() {
        MovieCastReadDTO dto = new MovieCastReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setPartInfo("Some Text");
        dto.setAverageRating(5.3);
        dto.setPartType(PartType.CAST);
        dto.setMovieId(UUID.randomUUID());
        dto.setPersonId(UUID.randomUUID());
        dto.setCharacter("Leon Killer");
        return dto;
    }
}
