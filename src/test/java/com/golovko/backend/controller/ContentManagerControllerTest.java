package com.golovko.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.Misprint;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.misprint.MisprintConfirmDTO;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.dto.misprint.MisprintRejectDTO;
import com.golovko.backend.exception.UnprocessableEntityException;
import com.golovko.backend.service.MisprintService;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ContentManagerController.class)
public class ContentManagerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MisprintService misprintService;

    @Test
    public void testConfirmModeration() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID targetObjectId = UUID.randomUUID();

        MisprintReadDTO readDTO = createMistakeReadDTO(userId, targetObjectId, moderatorId);

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(moderatorId);
        confirmDTO.setStartIndex(5);
        confirmDTO.setEndIndex(20);
        confirmDTO.setReplaceTo("new text");

        Mockito.when(misprintService.confirmModeration(targetObjectId, readDTO.getId(), confirmDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/articles/{articleId}/misprints/{id}/confirm",
                        targetObjectId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(misprintService).confirmModeration(targetObjectId, readDTO.getId(), confirmDTO);
    }

    @Test
    public void testRejectModeration() throws Exception {
        UUID userId = UUID.randomUUID();
        UUID moderatorId = UUID.randomUUID();
        UUID targetObjectId = UUID.randomUUID();

        MisprintReadDTO readDTO = createMistakeReadDTO(userId, targetObjectId, moderatorId);

        MisprintRejectDTO rejectDTO = new MisprintRejectDTO();
        rejectDTO.setModeratorId(moderatorId);
        rejectDTO.setStatus(ComplaintStatus.CLOSED);
        rejectDTO.setReason("whatever");

        Mockito.when(misprintService.rejectModeration(targetObjectId, readDTO.getId(), rejectDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/articles/{articleId}/misprints/{id}/reject",
                        targetObjectId, readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(misprintService).rejectModeration(targetObjectId, readDTO.getId(), rejectDTO);
    }

    @Ignore
    @Test
    public void testRejectMisprintStatusCode422() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID targetObjectId = UUID.randomUUID();
        UUID misprintId = UUID.randomUUID();

        MisprintRejectDTO rejectDTO = new MisprintRejectDTO();
        rejectDTO.setModeratorId(UUID.randomUUID());
        rejectDTO.setStatus(ComplaintStatus.CLOSED);
        rejectDTO.setReason("whatever");

        UnprocessableEntityException ex = new UnprocessableEntityException(Misprint.class, misprintId);

        Mockito.when(misprintService.rejectModeration(articleId, targetObjectId, rejectDTO)).thenThrow(ex);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/misprints/{id}/reject",
                        targetObjectId, misprintId))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(ex.getMessage()));
    }

    @Ignore
    @Test
    public void testConfirmMisprintStatusCode422() throws Exception {
        UUID articleId = UUID.randomUUID();
        UUID targetObjectId = UUID.randomUUID();
        UUID misprintId = UUID.randomUUID();

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(UUID.randomUUID());
        confirmDTO.setStartIndex(5);
        confirmDTO.setEndIndex(20);
        confirmDTO.setReplaceTo("new text");

        UnprocessableEntityException ex = new UnprocessableEntityException(Misprint.class, misprintId);

        Mockito.when(misprintService.confirmModeration(articleId, targetObjectId, confirmDTO)).thenThrow(ex);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/misprints/{id}/confirm",
                        targetObjectId, misprintId))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(ex.getMessage()));
    }

    private MisprintReadDTO createMistakeReadDTO(UUID authorId, UUID parentId, UUID moderatorId) {
        MisprintReadDTO dto = new MisprintReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setStartIndex(5);
        dto.setEndIndex(20);
        dto.setReplaceTo("replace to this");
        dto.setStatus(ComplaintStatus.INITIATED);
        dto.setAuthorId(authorId);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        dto.setTargetObjectType(TargetObjectType.ARTICLE);
        dto.setTargetObjectId(parentId);
        dto.setModeratorId(moderatorId);
        return dto;
    }
}
