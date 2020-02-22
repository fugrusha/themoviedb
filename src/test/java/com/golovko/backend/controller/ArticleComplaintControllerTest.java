package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.*;
import com.golovko.backend.dto.complaint.ComplaintCreateDTO;
import com.golovko.backend.dto.complaint.ComplaintPatchDTO;
import com.golovko.backend.dto.complaint.ComplaintPutDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.ArticleComplaintService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Ignore;
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
@WebMvcTest(ArticleComplaintController.class)
public class ArticleComplaintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArticleComplaintService articleComplaintService;

    @Test
    public void getArticleComplaintByIdTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, articleId, moderatorId);

        Mockito.when(articleComplaintService.getComplaint(articleId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/complaints/{id}", articleId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualComplaint).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(articleComplaintService).getComplaint(articleId, readDTO.getId());
    }

    @Test
    public void getListOfArticleComplaintsTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        ComplaintReadDTO c1 = createComplaintReadDTO(userId, articleId, moderatorId);
        ComplaintReadDTO c2 = createComplaintReadDTO(userId, articleId, moderatorId);

        List<ComplaintReadDTO> expectedResult = List.of(c1, c2);

        Mockito.when(articleComplaintService.getAllComplaints(articleId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/complaints/", articleId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ComplaintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(ComplaintReadDTO::getId)
                .containsExactlyInAnyOrder(c1.getId(), c2.getId());

        Mockito.verify(articleComplaintService).getAllComplaints(articleId);
    }

    @Test
    public void getArticleComplaintByWrongIdTest() throws Exception {
        UUID wrongId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        EntityNotFoundException ex = new EntityNotFoundException(Complaint.class, wrongId, Movie.class, articleId);

        Mockito.when(articleComplaintService.getComplaint(articleId, wrongId)).thenThrow(ex);

        String result = mockMvc
                .perform(get("/api/v1/articles/{articleId}/complaints/{id}", articleId, wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(ex.getMessage()));
    }

    @Ignore // TODO add user authentication
    @Test
    public void createArticleComplaintTest() throws Exception {
        ComplaintCreateDTO createDTO = new ComplaintCreateDTO();
        createDTO.setComplaintTitle("Complaint Title");
        createDTO.setComplaintText("Text text text");
        createDTO.setComplaintType(ComplaintType.SPAM);

        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, articleId, moderatorId);

//        Mockito.when(articleComplaintService.createComplaint(articleId, createDTO, author)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/articles/{articleId}/complaints/", articleId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualComplaint).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void patchArticleComplaintTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, articleId, moderatorId);

        ComplaintPatchDTO patchDTO = new ComplaintPatchDTO();
        patchDTO.setComplaintTitle("another title");
        patchDTO.setComplaintText("another text");
        patchDTO.setComplaintType(ComplaintType.CHILD_ABUSE);

        Mockito.when(articleComplaintService.patchComplaint(articleId, readDTO.getId(), patchDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/articles/{articleId}/complaints/{id}", articleId, readDTO.getId())
                .content(objectMapper.writeValueAsString(patchDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assert.assertEquals(readDTO, actualComplaint);
    }

    @Test
    public void updateArticleComplaintTest() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();
        ComplaintReadDTO readDTO = createComplaintReadDTO(userId, articleId, moderatorId);

        ComplaintPutDTO updateDTO = new ComplaintPutDTO();
        updateDTO.setComplaintText("new text");
        updateDTO.setComplaintTitle("new title");
        updateDTO.setComplaintType(ComplaintType.CHILD_ABUSE);

        Mockito.when(articleComplaintService.updateComplaint(articleId, readDTO.getId(), updateDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/articles/{articleId}/complaints/{id}", articleId, readDTO.getId())
                .content(objectMapper.writeValueAsString(updateDTO))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualComplaint = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assert.assertEquals(readDTO, actualComplaint);
    }

    @Test
    public void deleteArticleComplaintTest() throws Exception {
        UUID id = UUID.randomUUID();
        UUID articleId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/articles/{articleId}/complaints/{id}", articleId, id))
                .andExpect(status().isOk());

        Mockito.verify(articleComplaintService).deleteComplaint(articleId, id);
    }

    private ComplaintReadDTO createComplaintReadDTO(UUID authorId, UUID parentId, UUID moderatorId) {
        ComplaintReadDTO readDTO = new ComplaintReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setComplaintTitle("Report 1");
        readDTO.setComplaintText("I have noticed a spoiler");
        readDTO.setComplaintType(ComplaintType.SPOILER);
        readDTO.setComplaintStatus(ComplaintStatus.DUPLICATE);
        readDTO.setAuthorId(authorId);
        readDTO.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        readDTO.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        readDTO.setParentType(ParentType.PERSON);
        readDTO.setParentId(parentId);
        readDTO.setModeratorId(moderatorId);
        return readDTO;
    }
}
