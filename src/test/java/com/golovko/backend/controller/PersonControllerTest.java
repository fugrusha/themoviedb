package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.Gender;
import com.golovko.backend.dto.person.PersonCreateDTO;
import com.golovko.backend.dto.person.PersonPatchDTO;
import com.golovko.backend.dto.person.PersonPutDTO;
import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.PersonService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PersonController.class)
public class PersonControllerTest extends BaseControllerTest {

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
    public void testCreatePersonNotNullValidationException() throws Exception {
        PersonCreateDTO createDTO = new PersonCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(personService, Mockito.never()).createPerson(any());
    }

    @Test
    public void testCreatePersonMinSizeValidationException() throws Exception {
        PersonCreateDTO createDTO = new PersonCreateDTO();
        createDTO.setFirstName("");
        createDTO.setLastName("");
        createDTO.setBio("");
        createDTO.setGender(Gender.MALE);

        String resultJson = mockMvc
                .perform(post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(personService, Mockito.never()).createPerson(any());
    }

    @Test
    public void testCreatePersonMaxSizeValidationException() throws Exception {
        PersonCreateDTO createDTO = new PersonCreateDTO();
        createDTO.setFirstName("first name".repeat(100));
        createDTO.setLastName("last name".repeat(100));
        createDTO.setBio("long text about life".repeat(100));
        createDTO.setGender(Gender.MALE);

        String resultJson = mockMvc
                .perform(post("/api/v1/persons")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(personService, Mockito.never()).createPerson(any());
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
    public void testPatchPersonMinSizeValidationException() throws Exception {
        PersonPatchDTO patchDTO = new PersonPatchDTO();
        patchDTO.setFirstName("");
        patchDTO.setLastName("");
        patchDTO.setBio("");
        patchDTO.setGender(Gender.MALE);

        String resultJson = mockMvc
                .perform(patch("/api/v1/persons/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(personService, Mockito.never()).patchPerson(any(), any());
    }

    @Test
    public void testPatchPersonMaxSizeValidationException() throws Exception {
        PersonPatchDTO patchDTO = new PersonPatchDTO();
        patchDTO.setFirstName("first name".repeat(100));
        patchDTO.setLastName("last name".repeat(100));
        patchDTO.setBio("long text about life".repeat(100));
        patchDTO.setGender(Gender.MALE);

        String resultJson = mockMvc
                .perform(patch("/api/v1/persons/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(personService, Mockito.never()).patchPerson(any(), any());
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
    public void testUpdatePersonMinSizeValidationException() throws Exception {
        PersonPutDTO updateDTO = new PersonPutDTO();
        updateDTO.setFirstName("");
        updateDTO.setLastName("");
        updateDTO.setBio("");
        updateDTO.setGender(Gender.MALE);

        String resultJson = mockMvc
                .perform(put("/api/v1/persons/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(personService, Mockito.never()).patchPerson(any(), any());
    }

    @Test
    public void testUpdatePersonMaxSizeValidationException() throws Exception {
        PersonPutDTO updateDTO = new PersonPutDTO();
        updateDTO.setFirstName("first name".repeat(100));
        updateDTO.setLastName("last name".repeat(100));
        updateDTO.setBio("long text about life".repeat(100));
        updateDTO.setGender(Gender.MALE);

        String resultJson = mockMvc
                .perform(put("/api/v1/persons/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(personService, Mockito.never()).patchPerson(any(), any());
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
