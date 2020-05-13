package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.Misprint;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.ArticleManagerFilter;
import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.misprint.MisprintConfirmDTO;
import com.golovko.backend.dto.misprint.MisprintFilter;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.dto.misprint.MisprintRejectDTO;
import com.golovko.backend.dto.movie.MovieReadExtendedDTO;
import com.golovko.backend.exception.ControllerValidationException;
import com.golovko.backend.exception.EntityWrongStatusException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.ArticleService;
import com.golovko.backend.service.ContentManagerService;
import com.golovko.backend.service.MisprintService;
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
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ContentManagerController.class)
public class ContentManagerControllerTest extends BaseControllerTest {

    @MockBean
    private MisprintService misprintService;

    @MockBean
    private ArticleService articleService;

    @MockBean
    private ContentManagerService contentManagerService;

    @WithMockUser
    @Test
    public void testConfirmModeration() throws Exception {
        MisprintReadDTO readDTO = createMisprintReadDTO();

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(UUID.randomUUID());
        confirmDTO.setStartIndex(5);
        confirmDTO.setEndIndex(20);
        confirmDTO.setReplaceTo("new text");

        Mockito.when(misprintService.confirmModeration(readDTO.getId(), confirmDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/confirm", readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(misprintService).confirmModeration(readDTO.getId(), confirmDTO);
    }

    @WithMockUser
    @Test
    public void testConfirmMisprintNotNullValidationException() throws Exception {
        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/confirm", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).confirmModeration(any(), any());
    }

    @WithMockUser
    @Test
    public void testConfirmMisprintMinSizeValidationException() throws Exception {
        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(UUID.randomUUID());
        confirmDTO.setStartIndex(5);
        confirmDTO.setEndIndex(20);
        confirmDTO.setReplaceTo("");

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/confirm", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).confirmModeration(any(), any());
    }

    @WithMockUser
    @Test
    public void testConfirmMisprintMaxSizeValidationException() throws Exception {
        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(UUID.randomUUID());
        confirmDTO.setStartIndex(5);
        confirmDTO.setEndIndex(20);
        confirmDTO.setReplaceTo("misprint".repeat(1000));

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/confirm", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).confirmModeration(any(), any());
    }

    @WithMockUser
    @Test
    public void testConfirmMisprintNegativeIndexValidationException() throws Exception {
        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(UUID.randomUUID());
        confirmDTO.setStartIndex(-5);
        confirmDTO.setEndIndex(-20);
        confirmDTO.setReplaceTo("new text");

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/confirm", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).confirmModeration(any(), any());
    }

    @WithMockUser
    @Test
    public void testConfirmMisprintWrongIndexesValidationException() throws Exception {
        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(UUID.randomUUID());
        confirmDTO.setStartIndex(20); // grater than end index
        confirmDTO.setEndIndex(10);
        confirmDTO.setReplaceTo("new text");

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/confirm", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo errorInfo = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertTrue(errorInfo.getMessage().contains("startIndex"));
        Assert.assertTrue(errorInfo.getMessage().contains("endIndex"));
        Assert.assertEquals(ControllerValidationException.class, errorInfo.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).confirmModeration(any(), any());
    }

    @WithMockUser
    @Test
    public void testRejectModeration() throws Exception {
        MisprintReadDTO readDTO = createMisprintReadDTO();

        MisprintRejectDTO rejectDTO = new MisprintRejectDTO();
        rejectDTO.setModeratorId(UUID.randomUUID());
        rejectDTO.setStatus(ComplaintStatus.CLOSED);
        rejectDTO.setReason("whatever");

        Mockito.when(misprintService.rejectModeration(readDTO.getId(), rejectDTO))
                .thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/reject", readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(misprintService).rejectModeration(readDTO.getId(), rejectDTO);
    }

    @WithMockUser
    @Test
    public void testRejectMisprintNotNullValidationException() throws Exception {
        MisprintRejectDTO rejectDTO = new MisprintRejectDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/reject", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).rejectModeration(any(), any());
    }

    @WithMockUser
    @Test
    public void testRejectMisprintMaxSizeValidationException() throws Exception {
        MisprintRejectDTO rejectDTO = new MisprintRejectDTO();
        rejectDTO.setModeratorId(UUID.randomUUID());
        rejectDTO.setStatus(ComplaintStatus.CLOSED);
        rejectDTO.setReason("whatever".repeat(100));;

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/reject", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).rejectModeration(any(), any());
    }

    @WithMockUser
    @Test
    public void testRejectMisprintMinSizeValidationException() throws Exception {
        MisprintRejectDTO rejectDTO = new MisprintRejectDTO();
        rejectDTO.setModeratorId(UUID.randomUUID());
        rejectDTO.setStatus(ComplaintStatus.CLOSED);
        rejectDTO.setReason("");

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/reject", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(misprintService, Mockito.never()).rejectModeration(any(), any());
    }

    @WithMockUser
    @Test
    public void testRejectMisprintStatusCode422() throws Exception {
        UUID misprintId = UUID.randomUUID();

        MisprintRejectDTO rejectDTO = new MisprintRejectDTO();
        rejectDTO.setModeratorId(UUID.randomUUID());
        rejectDTO.setStatus(ComplaintStatus.CLOSED);
        rejectDTO.setReason("whatever");

        EntityWrongStatusException ex = new EntityWrongStatusException(Misprint.class, misprintId);

        Mockito.when(misprintService.rejectModeration(misprintId, rejectDTO)).thenThrow(ex);

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/reject", misprintId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rejectDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(ex.getMessage()));
    }

    @WithMockUser
    @Test
    public void testConfirmMisprintStatusCode422() throws Exception { ;
        UUID misprintId = UUID.randomUUID();

        MisprintConfirmDTO confirmDTO = new MisprintConfirmDTO();
        confirmDTO.setModeratorId(UUID.randomUUID());
        confirmDTO.setStartIndex(5);
        confirmDTO.setEndIndex(20);
        confirmDTO.setReplaceTo("new text");

        EntityWrongStatusException ex = new EntityWrongStatusException(Misprint.class, misprintId);

        Mockito.when(misprintService.confirmModeration(misprintId, confirmDTO)).thenThrow(ex);

        String resultJson = mockMvc
                .perform(post("/api/v1/misprints/{id}/confirm", misprintId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(confirmDTO)))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(resultJson.contains(ex.getMessage()));
    }

    @WithMockUser
    @Test
    public void testGetAllMisprintsByArticleId() throws Exception {
        UUID articleId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        PageResult<MisprintReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(misprintService.getMisprintsByTargetId(articleId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/misprints/", articleId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(pageResult, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetAllMisprintsByMovieId() throws Exception {
        UUID movieId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        PageResult<MisprintReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(misprintService.getMisprintsByTargetId(movieId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/misprints/", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(pageResult, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetAllMisprintsByPersonId() throws Exception {
        UUID personId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        PageResult<MisprintReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(misprintService.getMisprintsByTargetId(personId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/people/{personId}/misprints/", personId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(pageResult, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetAllMisprintsByMovieCastId() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        PageResult<MisprintReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(misprintService.getMisprintsByTargetId(movieCastId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-casts/{movieCastId}/misprints/", movieCastId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(pageResult, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetAllMisprintsByMovieCrewId() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        PageResult<MisprintReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(misprintService.getMisprintsByTargetId(movieCrewId, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-crews/{movieCrew}/misprints/", movieCrewId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(pageResult, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetMisprintsByArticleIdAndMisprintId() throws Exception {
        UUID articleId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        Mockito.when(misprintService.getMisprintByTargetId(articleId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/misprints/{id}",
                        articleId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testGetAllMisprintsByMovieIdAndMisprintId() throws Exception {
        UUID movieId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        Mockito.when(misprintService.getMisprintByTargetId(movieId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/misprints/{id}",
                        movieId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testGetAllMisprintsByPersonIdAndMisprintId() throws Exception {
        UUID personId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        Mockito.when(misprintService.getMisprintByTargetId(personId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/people/{personId}/misprints/{id}",
                        personId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testGetAllMisprintsByMovieCastIdAndMisprintId() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        Mockito.when(misprintService.getMisprintByTargetId(movieCastId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-casts/{movieCastId}/misprints/{id}",
                        movieCastId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testGetAllMisprintsByMovieCrewIdAndMisprintId() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        Mockito.when(misprintService.getMisprintByTargetId(movieCrewId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-crews/{movieCrewId}/misprints/{id}",
                        movieCrewId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @WithMockUser
    @Test
    public void testGetMisprintsWithFilter() throws Exception {
        MisprintFilter filter = new MisprintFilter();
        filter.setAuthorId(UUID.randomUUID());
        filter.setModeratorId(UUID.randomUUID());
        filter.setStatuses(Set.of(ComplaintStatus.INITIATED));
        filter.setTargetObjectTypes(Set.of(TargetObjectType.ARTICLE));

        MisprintReadDTO readDTO = createMisprintReadDTO();

        PageResult<MisprintReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(readDTO));

        Mockito.when(misprintService.getMisprintsByFilter(filter, PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/misprints")
                .param("moderatorId", filter.getModeratorId().toString())
                .param("authorId", filter.getAuthorId().toString())
                .param("statuses", "INITIATED")
                .param("targetObjectTypes", "ARTICLE"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(pageResult, actualResult);

        Mockito.verify(misprintService).getMisprintsByFilter(filter, PageRequest.of(0, defaultPageSize));
    }

    @WithMockUser
    @Test
    public void testGetArticlesByFilter() throws Exception {
        ArticleManagerFilter filter = new ArticleManagerFilter();
        filter.setAuthorId(UUID.randomUUID());
        filter.setStatuses(Set.of(ArticleStatus.DRAFT));

        ArticleReadDTO readDTO = createArticleReadDTO(filter.getAuthorId());

        PageResult<ArticleReadDTO> expectedResult = new PageResult<>();
        expectedResult.setData(List.of(readDTO));

        Mockito.when(articleService.getArticlesByFilter(filter, PageRequest.of(0, defaultPageSize)))
                .thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/filter")
                .param("authorId", filter.getAuthorId().toString())
                .param("statuses", "DRAFT"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<ArticleReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(articleService).getArticlesByFilter(filter, PageRequest.of(0, defaultPageSize));
    }

    @WithMockUser
    @Test
    public void testGetArticlesWithPagingAndSorting() throws Exception {
        ArticleManagerFilter filter = new ArticleManagerFilter();
        ArticleReadDTO readDTO = createArticleReadDTO(UUID.randomUUID());

        int page = 1;
        int size = 25;

        PageResult<ArticleReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(100);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "title"));

        Mockito.when(articleService.getArticlesByFilter(filter, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/filter")
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "title,desc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<ArticleReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetMisprintsWithPagingAndSorting() throws Exception {
        MisprintFilter filter = new MisprintFilter();
        MisprintReadDTO readDTO = createMisprintReadDTO();

        int page = 1;
        int size = 25;

        PageResult<MisprintReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(100);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Mockito.when(misprintService.getMisprintsByFilter(filter, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/misprints")
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,desc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetArticlesWithBigPage() throws Exception {
        ArticleManagerFilter filter = new ArticleManagerFilter();
        ArticleReadDTO readDTO = createArticleReadDTO(UUID.randomUUID());

        int page = 1;
        int size = 99999;

        PageResult<ArticleReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(100);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, maxPageSize, Sort.by(Sort.Direction.DESC, "title"));

        Mockito.when(articleService.getArticlesByFilter(filter, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/filter")
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "title,desc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<ArticleReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetMisprintsByArticleIdWithPagingAndSorting() throws Exception {
        UUID articleId = UUID.randomUUID();
        MisprintReadDTO m1 = createMisprintReadDTO();

        int page = 1;
        int size = 30;

        PageResult<MisprintReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(m1));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(misprintService.getMisprintsByTargetId(articleId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/misprints/", articleId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetMisprintsByMovieIdWithPagingAndSorting() throws Exception {
        UUID movieId = UUID.randomUUID();
        MisprintReadDTO m1 = createMisprintReadDTO();

        int page = 1;
        int size = 30;

        PageResult<MisprintReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(m1));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(misprintService.getMisprintsByTargetId(movieId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/misprints/", movieId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetMisprintsByPersonIdWithPagingAndSorting() throws Exception {
        UUID personId = UUID.randomUUID();
        MisprintReadDTO m1 = createMisprintReadDTO();

        int page = 1;
        int size = 30;

        PageResult<MisprintReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(m1));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(misprintService.getMisprintsByTargetId(personId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/people/{personId}/misprints/", personId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetMisprintsByMovieCastIdWithPagingAndSorting() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        MisprintReadDTO m1 = createMisprintReadDTO();

        int page = 1;
        int size = 30;

        PageResult<MisprintReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(m1));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(misprintService.getMisprintsByTargetId(movieCastId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-casts/{movieCastId}/misprints/", movieCastId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @WithMockUser
    @Test
    public void testGetMisprintsByMovieCrewIdWithPagingAndSorting() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        MisprintReadDTO m1 = createMisprintReadDTO();

        int page = 1;
        int size = 30;

        PageResult<MisprintReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(m1));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(misprintService.getMisprintsByTargetId(movieCrewId, pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-crews/{movieCrewId}/misprints/", movieCrewId)
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    @WithMockUser
    @Test
    public void testImportMovie() throws Exception {
        MovieReadExtendedDTO readDTO = generateObject(MovieReadExtendedDTO.class);
        String externalMovieId = "id200";

        Mockito.when(contentManagerService.importMovie(externalMovieId)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/movies/import-movie/{id}", externalMovieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MovieReadExtendedDTO actualResult = objectMapper.readValue(resultJson, MovieReadExtendedDTO.class);
        Assert.assertEquals(readDTO, actualResult);

        Mockito.verify(contentManagerService).importMovie(externalMovieId);
    }

    private MisprintReadDTO createMisprintReadDTO() {
        return generateObject(MisprintReadDTO.class);
    }

    private ArticleReadDTO createArticleReadDTO(UUID authorId) {
        ArticleReadDTO dto = generateObject(ArticleReadDTO.class);
        dto.setAuthorId(authorId);
        return dto;
    }
}
