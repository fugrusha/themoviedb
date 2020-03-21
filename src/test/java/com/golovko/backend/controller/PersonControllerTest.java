package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonPutDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.service.PersonService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(PersonController.class)
public class PersonControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PersonService personService;

    @Test
    public void testGetPerson() throws Exception {
        PersonReadDTO readDTO = createPersonReadDTO();

        Mockito.when(personService.getPerson(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/persons/{id}", readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PersonReadDTO actualPerson = objectMapper.readValue(resultJson, PersonReadDTO.class);

        Assertions.assertThat(actualPerson).isEqualToComparingFieldByField(readDTO);
        Mockito.verify(personService).getPerson(readDTO.getId());
    }

    @Test
    public void testGetAllPersons() throws Exception {
        PersonReadDTO p1 = createPersonReadDTO();
        PersonReadDTO p2 = createPersonReadDTO();
        PersonReadDTO p3 = createPersonReadDTO();

        List<PersonReadDTO> expectedResult = List.of(p1, p2, p3);

        Mockito.when(personService.getAllPersons()).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/persons"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<PersonReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(personService).getAllPersons();
    }

    @Test
    public void testCreatePerson() throws Exception {
        PersonCreateDTO createDTO = new PersonCreateDTO();
        createDTO.setFirstName("Max");
        createDTO.setLastName("Popov");
        createDTO.setBio("some text");
        createDTO.setGender(Gender.MALE);

        PersonReadDTO readDTO = createPersonReadDTO();

        Mockito.when(personService.createPerson(createDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PersonReadDTO actualPerson = objectMapper.readValue(resultJson, PersonReadDTO.class);

        Assertions.assertThat(actualPerson).isEqualToComparingFieldByField(readDTO);
        Mockito.verify(personService).createPerson(createDTO);
    }

    @Test
    public void testPatchPerson() throws Exception {
        PersonReadDTO readDTO = createPersonReadDTO();

        PersonPatchDTO patchDTO = new PersonPatchDTO();
        patchDTO.setFirstName("Lolita");
        patchDTO.setLastName("Bulgakova");
        patchDTO.setBio("some text");
        patchDTO.setGender(Gender.FEMALE);

        Mockito.when(personService.patchPerson(readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/persons/{id}", readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PersonReadDTO actualPerson = objectMapper.readValue(resultJson, PersonReadDTO.class);

        Assert.assertEquals(readDTO, actualPerson);
    }

    @Test
    public void testUpdatePerson() throws Exception {
        PersonReadDTO readDTO = createPersonReadDTO();

        PersonPutDTO updateDTO = new PersonPutDTO();
        updateDTO.setFirstName("Lolita");
        updateDTO.setLastName("Bulgakova");
        updateDTO.setBio("some text");
        updateDTO.setGender(Gender.FEMALE);

        Mockito.when(personService.updatePerson(readDTO.getId(), updateDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/persons/{id}", readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PersonReadDTO actualPerson = objectMapper.readValue(resultJson, PersonReadDTO.class);

        Assert.assertEquals(readDTO, actualPerson);
    }

    @Test
    public void testDeletePerson() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/persons/{id}", id))
                .andExpect(status().is2xxSuccessful());

        Mockito.verify(personService).deletePerson(id);
    }

    private PersonReadDTO createPersonReadDTO() {
        PersonReadDTO dto = new PersonReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setFirstName("Max");
        dto.setLastName("Popov");
        dto.setBio("some text");
        dto.setGender(Gender.MALE);
        dto.setAverageRatingByRoles(5.0);
        dto.setAverageRatingByMovies(9.0);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
