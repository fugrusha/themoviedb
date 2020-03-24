package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.comment.CommentFilter;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.comment.CommentStatusDTO;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import com.golovko.backend.dto.complaint.ComplaintModerateDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.dto.user.UserTrustLevelDTO;
import com.golovko.backend.service.ApplicationUserService;
import com.golovko.backend.service.CommentService;
import com.golovko.backend.service.ComplaintService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModeratorController.class)
public class ModeratorControllerTest extends BaseControllerTest {

    @MockBean
    private ComplaintService complaintService;

    @MockBean
    private CommentService commentService;

    @MockBean
    private ApplicationUserService applicationUserService;

    @Test
    public void testGetComplaintsWithFilter() throws Exception {
        ComplaintFilter filter = new ComplaintFilter();
        filter.setAuthorId(UUID.randomUUID());
        filter.setModeratorId(UUID.randomUUID());
        filter.setComplaintTypes(Set.of(ComplaintType.VIOLENCE, ComplaintType.SPAM));
        filter.setStatuses(Set.of(ComplaintStatus.INITIATED));
        filter.setTargetObjectTypes(Set.of(TargetObjectType.COMMENT));

        ComplaintReadDTO readDTO = new ComplaintReadDTO();
        readDTO.setModeratorId(filter.getModeratorId());
        readDTO.setAuthorId(filter.getAuthorId());
        readDTO.setComplaintType(ComplaintType.SPAM);
        readDTO.setComplaintStatus(ComplaintStatus.INITIATED);
        readDTO.setTargetObjectType(TargetObjectType.COMMENT);
        readDTO.setTargetObjectId(UUID.randomUUID());
        readDTO.setId(UUID.randomUUID());
        readDTO.setComplaintTitle("title");
        readDTO.setComplaintText("description");

        List<ComplaintReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(complaintService.getAllComplaints(filter)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/complaints")
                .param("moderatorId", filter.getModeratorId().toString())
                .param("authorId", filter.getAuthorId().toString())
                .param("statuses", "INITIATED")
                .param("complaintTypes", "VIOLENCE, SPAM")
                .param("targetObjectTypes", "COMMENT"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ComplaintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(complaintService).getAllComplaints(filter);
    }

    @Test
    public void testModerateComplaint() throws Exception {
        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();
        moderDTO.setModeratorId(UUID.randomUUID());
        moderDTO.setComplaintStatus(ComplaintStatus.CLOSED);

        UUID userId = UUID.randomUUID();
        ComplaintReadDTO complaintDTO = createComplaintReadDTO(userId, moderDTO.getModeratorId());

        Mockito.when(complaintService.moderateComplaint(complaintDTO.getId(), moderDTO))
                .thenReturn(complaintDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/complaints/{id}/moderate", complaintDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moderDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ComplaintReadDTO actualResult = objectMapper.readValue(resultJson, ComplaintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(complaintDTO);

        Mockito.verify(complaintService).moderateComplaint(complaintDTO.getId(), moderDTO);
    }

    @Test
    public void testSetUserTrustLevel() throws Exception {
        UserTrustLevelDTO dto = new UserTrustLevelDTO();
        dto.setTrustLevel(6.5);

        UserReadDTO readDTO = createUserReadDTO();

        Mockito.when(applicationUserService.changeTrustLevel(readDTO.getId(), dto)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{id}/set-trust-level", readDTO.getId())
                .content(objectMapper.writeValueAsString(dto))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        UserReadDTO actualResult = objectMapper.readValue(resultJson, UserReadDTO.class);
        Assert.assertEquals(readDTO, actualResult);

        Mockito.verify(applicationUserService).changeTrustLevel(readDTO.getId(), dto);
    }

    @Test
    public void testGetCommentsByFilter() throws Exception {
        CommentReadDTO readDTO = createCommentReadDTO(UUID.randomUUID());

        CommentFilter filter = new CommentFilter();
        filter.setAuthorId(readDTO.getAuthorId());
        filter.setStatuses(Set.of(CommentStatus.PENDING, CommentStatus.NEED_MODERATION));
        filter.setTypes(Set.of(TargetObjectType.MOVIE));

        List<CommentReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(commentService.getCommentsByFilter(filter)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/comments")
                .param("authorId", filter.getAuthorId().toString())
                .param("statuses", "PENDING, NEED_MODERATION")
                .param("types", "MOVIE"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(actualResult, expectedResult);

        Mockito.verify(commentService).getCommentsByFilter(filter);
    }

    @Test
    public void testChangeCommentStatus() throws Exception {
        CommentStatusDTO statusDTO = new CommentStatusDTO();
        statusDTO.setStatus(CommentStatus.APPROVED);

        CommentReadDTO readDTO = createCommentReadDTO(UUID.randomUUID());

        Mockito.when(commentService.changeStatus(readDTO.getId(), statusDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/comments/{id}/change-status", readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        CommentReadDTO actualResult = objectMapper.readValue(resultJson, CommentReadDTO.class);
        Assert.assertEquals(actualResult, readDTO);

        Mockito.verify(commentService).changeStatus(readDTO.getId(), statusDTO);
    }

    private ComplaintReadDTO createComplaintReadDTO(UUID authorId, UUID moderatorId) {
        ComplaintReadDTO readDTO = new ComplaintReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setComplaintTitle("Report 1");
        readDTO.setComplaintText("I have noticed a spoiler");
        readDTO.setComplaintType(ComplaintType.SPOILER);
        readDTO.setComplaintStatus(ComplaintStatus.INITIATED);
        readDTO.setAuthorId(authorId);
        readDTO.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        readDTO.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        readDTO.setTargetObjectType(TargetObjectType.PERSON);
        readDTO.setTargetObjectId(UUID.randomUUID());
        readDTO.setModeratorId(moderatorId);
        return readDTO;
    }

    private UserReadDTO createUserReadDTO() {
        UserReadDTO readDTO = new UserReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setUsername("david");
        readDTO.setEmail("david101@email.com");
        readDTO.setIsBlocked(false);
        readDTO.setTrustLevel(6.5);
        readDTO.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        readDTO.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return readDTO;
    }

    private CommentReadDTO createCommentReadDTO(UUID authorId) {
        CommentReadDTO dto = new CommentReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setMessage("some text");
        dto.setAuthorId(authorId);
        dto.setTargetObjectType(TargetObjectType.MOVIE);
        dto.setTargetObjectId(UUID.randomUUID());
        dto.setDislikesCount(46);
        dto.setLikesCount(120);
        dto.setStatus(CommentStatus.PENDING);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
