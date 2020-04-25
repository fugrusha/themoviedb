package com.golovko.backend.controller;

import com.golovko.backend.dto.person.PersonReadDTO;
import com.golovko.backend.service.PersonService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PersonController.class)
public class NoSessionTest extends BaseControllerTest {

    @MockBean
    private PersonService personService;

    @Test
    public void testNoSession() throws Exception {
        UUID personId = UUID.randomUUID();

        Mockito.when(personService.getPerson(personId)).thenReturn(new PersonReadDTO());

        MvcResult mvcResult = mockMvc
                .perform(get("/api/v1/people/{id}", personId))
                .andExpect(status().isOk())
                .andReturn();

        Assert.assertNull(mvcResult.getRequest().getSession(false));
    }
}
