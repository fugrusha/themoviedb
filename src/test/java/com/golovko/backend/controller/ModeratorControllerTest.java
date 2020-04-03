package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.CommentStatus;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.ComplaintType;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.comment.CommentFilter;
import com.golovko.backend.dto.comment.CommentReadDTO;
import com.golovko.backend.dto.comment.CommentStatusDTO;
import com.golovko.backend.dto.complaint.ComplaintFilter;
import com.golovko.backend.dto.complaint.ComplaintModerateDTO;
import com.golovko.backend.dto.complaint.ComplaintReadDTO;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.dto.user.UserTrustLevelDTO;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.ApplicationUserService;
import com.golovko.backend.service.CommentService;
import com.golovko.backend.service.ComplaintService;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
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

        PageResult<ComplaintReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(complaintService.getAllComplaints(filter, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

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

        PageResult<ComplaintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(pageResult, actualResult);

        Mockito.verify(complaintService).getAllComplaints(filter, PageRequest.of(0, defaultPageSize));
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
    public void testModerateComplaintNotNUllValidationException() throws Exception {
        ComplaintModerateDTO moderDTO = new ComplaintModerateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/complaints/{id}/moderate", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moderDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(complaintService, Mockito.never()).moderateComplaint(any(), any());
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
    public void testSetUserTrustLevelNotNullValidationException() throws Exception {
        UserTrustLevelDTO dto = new UserTrustLevelDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{id}/set-trust-level", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).changeTrustLevel(any(), any());
    }

    @Test
    public void testSetUserTrustLevelMaxValueValidationException() throws Exception {
        UserTrustLevelDTO dto = new UserTrustLevelDTO();
        dto.setTrustLevel(10.1);

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{id}/set-trust-level", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).changeTrustLevel(any(), any());
    }

    @Test
    public void testSetUserTrustLevelMinValueValidationException() throws Exception {
        UserTrustLevelDTO dto = new UserTrustLevelDTO();
        dto.setTrustLevel(0.9);

        String resultJson = mockMvc
                .perform(post("/api/v1/users/{id}/set-trust-level", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(applicationUserService, Mockito.never()).changeTrustLevel(any(), any());
    }

    @Test
    public void testGetCommentsByFilter() throws Exception {
        CommentReadDTO readDTO = createCommentReadDTO();

        CommentFilter filter = new CommentFilter();
        filter.setAuthorId(readDTO.getAuthorId());
        filter.setStatuses(Set.of(CommentStatus.PENDING, CommentStatus.NEED_MODERATION));
        filter.setTypes(Set.of(TargetObjectType.MOVIE));

        PageResult<CommentReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(commentService.getCommentsByFilter(filter, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/comments")
                .param("authorId", filter.getAuthorId().toString())
                .param("statuses", "PENDING, NEED_MODERATION")
                .param("types", "MOVIE"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(actualResult, pageResult);

        Mockito.verify(commentService).getCommentsByFilter(filter, PageRequest.of(0, defaultPageSize));
    }

    @Test
    public void testChangeCommentStatus() throws Exception {
        CommentStatusDTO statusDTO = new CommentStatusDTO();
        statusDTO.setStatus(CommentStatus.APPROVED);

        CommentReadDTO readDTO = createCommentReadDTO();

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

    @Test
    public void testChangeCommentStatusValidationException() throws Exception {
        CommentStatusDTO statusDTO = new CommentStatusDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/comments/{id}/change-status", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(statusDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(commentService, Mockito.never()).changeStatus(any(), any());
    }

    @Test
    public void testGetCommentsWithPagingAndSorting() throws Exception {
        CommentReadDTO readDTO = createCommentReadDTO();
        CommentFilter filter = new CommentFilter();

        int page = 1;
        int size = 25;

        PageResult<CommentReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(100);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "status"));

        Mockito.when(commentService.getCommentsByFilter(filter, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/comments")
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "status,desc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<CommentReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @Test
    public void testGetComplaintsWithPagingAndSorting() throws Exception {
        ComplaintReadDTO readDTO = createComplaintReadDTO(UUID.randomUUID(), UUID.randomUUID());
        ComplaintFilter filter = new ComplaintFilter();

        int page = 1;
        int size = 25;

        PageResult<ComplaintReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(100);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "complaintType"));

        Mockito.when(complaintService.getAllComplaints(filter, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/complaints")
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "complaintType,desc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<ComplaintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    private ComplaintReadDTO createComplaintReadDTO(UUID authorId, UUID moderatorId) {
        ComplaintReadDTO readDTO = generateObject(ComplaintReadDTO.class);
        readDTO.setAuthorId(authorId);
        readDTO.setModeratorId(moderatorId);
        return readDTO;
    }

    private UserReadDTO createUserReadDTO() {
        return generateObject(UserReadDTO.class);
    }

    private CommentReadDTO createCommentReadDTO() {
        return generateObject(CommentReadDTO.class);
    }
}
