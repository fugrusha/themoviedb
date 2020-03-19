package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.ComplaintStatus;
import com.golovko.backend.domain.Misprint;
import com.golovko.backend.domain.TargetObjectType;
import com.golovko.backend.dto.article.ArticleManagerFilter;
import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.misprint.MisprintConfirmDTO;
import com.golovko.backend.dto.misprint.MisprintFilter;
import com.golovko.backend.dto.misprint.MisprintReadDTO;
import com.golovko.backend.dto.misprint.MisprintRejectDTO;
import com.golovko.backend.exception.EntityWrongStatusException;
import com.golovko.backend.service.ArticleService;
import com.golovko.backend.service.MisprintService;
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
import java.util.Set;
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

    @MockBean
    private ArticleService articleService;

    @Test
    public void testConfirmModeration() throws Exception {
        MisprintReadDTO readDTO = createMistakeReadDTO();

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

    @Test
    public void testRejectModeration() throws Exception {
        MisprintReadDTO readDTO = createMistakeReadDTO();

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

    @Test
    public void testGetAllMisprintsByArticleId() throws Exception {
        UUID articleId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO();
        List<MisprintReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(misprintService.getAllMisprintsByTargetId(articleId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/misprints/", articleId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting("id").contains(readDTO.getId());
    }

    @Test
    public void testGetAllMisprintsByMovieId() throws Exception {
        UUID movieId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO();
        List<MisprintReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(misprintService.getAllMisprintsByTargetId(movieId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/misprints/", movieId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting("id").contains(readDTO.getId());
    }

    @Test
    public void testGetAllMisprintsByPersonId() throws Exception {
        UUID personId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO();
        List<MisprintReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(misprintService.getAllMisprintsByTargetId(personId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/persons/{personId}/misprints/", personId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting("id").contains(readDTO.getId());
    }

    @Test
    public void testGetAllMisprintsByMovieCastId() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO();
        List<MisprintReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(misprintService.getAllMisprintsByTargetId(movieCastId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-casts/{movieCastId}/misprints/", movieCastId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting("id").contains(readDTO.getId());
    }

    @Test
    public void testGetAllMisprintsByMovieCrewId() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO();
        List<MisprintReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(misprintService.getAllMisprintsByTargetId(movieCrewId)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-crews/{movieCrew}/misprints/", movieCrewId))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting("id").contains(readDTO.getId());
    }

    @Test
    public void testGetMisprintsByArticleIdAndMisprintId() throws Exception {
        UUID articleId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO();

        Mockito.when(misprintService.getMisprintByTargetId(articleId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{articleId}/misprints/{id}",
                        articleId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testGetAllMisprintsByMovieIdAndMisprintId() throws Exception {
        UUID movieId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO();

        Mockito.when(misprintService.getMisprintByTargetId(movieId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movies/{movieId}/misprints/{id}",
                        movieId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testGetAllMisprintsByPersonIdAndMisprintId() throws Exception {
        UUID personId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO();

        Mockito.when(misprintService.getMisprintByTargetId(personId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/persons/{personId}/misprints/{id}",
                        personId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testGetAllMisprintsByMovieCastIdAndMisprintId() throws Exception {
        UUID movieCastId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO();

        Mockito.when(misprintService.getMisprintByTargetId(movieCastId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-casts/{movieCastId}/misprints/{id}",
                        movieCastId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testGetAllMisprintsByMovieCrewIdAndMisprintId() throws Exception {
        UUID movieCrewId = UUID.randomUUID();
        MisprintReadDTO readDTO = createMistakeReadDTO();

        Mockito.when(misprintService.getMisprintByTargetId(movieCrewId, readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/movie-crews/{movieCrewId}/misprints/{id}",
                        movieCrewId, readDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        MisprintReadDTO actualResult = objectMapper.readValue(resultJson, MisprintReadDTO.class);
        Assertions.assertThat(actualResult).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testGetMisprintsWithFilter() throws Exception {
        MisprintFilter filter = new MisprintFilter();
        filter.setAuthorId(UUID.randomUUID());
        filter.setModeratorId(UUID.randomUUID());
        filter.setStatuses(Set.of(ComplaintStatus.INITIATED));
        filter.setTargetObjectTypes(Set.of(TargetObjectType.ARTICLE));

        MisprintReadDTO readDTO = createMistakeReadDTO();

        List<MisprintReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(misprintService.getAllMisprints(filter)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/misprints")
                .param("moderatorId", filter.getModeratorId().toString())
                .param("authorId", filter.getAuthorId().toString())
                .param("statuses", "INITIATED")
                .param("targetObjectTypes", "ARTICLE"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<MisprintReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(misprintService).getAllMisprints(filter);
    }

    @Test
    public void testGetArticlesByFilter() throws Exception {
        ArticleManagerFilter filter = new ArticleManagerFilter();
        filter.setAuthorId(UUID.randomUUID());
        filter.setStatuses(Set.of(ArticleStatus.DRAFT));

        ArticleReadDTO readDTO = createArticleReadDTO(filter.getAuthorId());
        List<ArticleReadDTO> expectedResult = List.of(readDTO);

        Mockito.when(articleService.getArticlesByFilter(filter)).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/filter")
                .param("authorId", filter.getAuthorId().toString())
                .param("statuses", "DRAFT"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ArticleReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(expectedResult, actualResult);

        Mockito.verify(articleService).getArticlesByFilter(filter);
    }

    private MisprintReadDTO createMistakeReadDTO() {
        MisprintReadDTO dto = new MisprintReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setReplaceTo("replace to this");
        dto.setStatus(ComplaintStatus.INITIATED);
        dto.setAuthorId(UUID.randomUUID());
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        dto.setTargetObjectType(TargetObjectType.ARTICLE);
        dto.setTargetObjectId(UUID.randomUUID());
        dto.setModeratorId(UUID.randomUUID());
        return dto;
    }

    private ArticleReadDTO createArticleReadDTO(UUID authorId) {
        ArticleReadDTO dto = new ArticleReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setTitle("Title");
        dto.setText("Some Text");
        dto.setStatus(ArticleStatus.PUBLISHED);
        dto.setAuthorId(authorId);
        dto.setDislikesCount(555);
        dto.setLikesCount(333);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }
}
