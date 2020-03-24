package com.golovko.backend.controller;

import org.junit.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthController.class)
public class HealthControllerTest extends BaseControllerTest {

    @Test
    public void testHealth() throws Exception {
        mockMvc.perform(get("/health")).andExpect(status().isOk());
    }
}