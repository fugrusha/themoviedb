package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.PageResult;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.exception.handler.ErrorInfo;
import com.golovko.backend.service.ArticleService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ArticleController.class)
public class ArticleControllerTest extends BaseControllerTest {

    @MockBean
    private ArticleService articleService;

    @Test
    public void testGetArticleById() throws Exception {
        ArticleReadDTO readDTO = createArticleReadDTO();

        Mockito.when(articleService.getArticle(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{id}", readDTO.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArticleReadDTO actualArticle = objectMapper.readValue(resultJson, ArticleReadDTO.class);
        Assertions.assertThat(actualArticle).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(articleService).getArticle(readDTO.getId());
    }

    @Test
    public void testGetAllArticles() throws Exception {
        ArticleReadDTO a1 = createArticleReadDTO();
        ArticleReadDTO a2 = createArticleReadDTO();
        ArticleReadDTO a3 = createArticleReadDTO();
        ArticleReadDTO a4 = createArticleReadDTO();

        PageResult<ArticleReadDTO> pageResult = new PageResult<>();
        pageResult.setData(List.of(a1, a2, a3, a4));

        Mockito.when(articleService.getAllPublishedArticles(PageRequest.of(0, defaultPageSize)))
                .thenReturn(pageResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<ArticleReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult.getData()).extracting(ArticleReadDTO::getId)
                .containsExactlyInAnyOrder(a1.getId(), a2.getId(), a3.getId(), a4.getId());

        Mockito.verify(articleService).getAllPublishedArticles(PageRequest.of(0, defaultPageSize));
    }

    @Test
    public void testGetArticleByWrongId() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);

        Mockito.when(articleService.getArticle(wrongId)).thenThrow(exception);

        String result = mockMvc
                .perform(get("/api/v1/articles/{id}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void testGetArticleExtended() throws Exception {
        UserReadDTO userReadDTO = createUserReadDTO();
        ArticleReadExtendedDTO extendedDTO = createArticleReadExtendedDTO(userReadDTO);

        Mockito.when(articleService.getArticleExtended(extendedDTO.getId())).thenReturn(extendedDTO);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/{id}/extended", extendedDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArticleReadExtendedDTO actualDTO = objectMapper.readValue(resultJson, ArticleReadExtendedDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(extendedDTO);

        Mockito.verify(articleService).getArticleExtended(extendedDTO.getId());
    }

    @Test
    public void testCreateArticle() throws Exception {
        ArticleReadDTO readDTO = createArticleReadDTO();

        ArticleCreateDTO createDTO = new ArticleCreateDTO();
        createDTO.setTitle("Text title");
        createDTO.setText("Some text");
        createDTO.setStatus(ArticleStatus.DRAFT);
        createDTO.setAuthorId(UUID.randomUUID());

        Mockito.when(articleService.createArticle(createDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(post("/api/v1/articles/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArticleReadDTO actualDTO = objectMapper.readValue(resultJson, ArticleReadDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(articleService).createArticle(createDTO);
    }

    @Test
    public void testCreateArticleNotNullValidationFailed() throws Exception {
        ArticleCreateDTO createDTO = new ArticleCreateDTO();

        String resultJson = mockMvc
                .perform(post("/api/v1/articles/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(articleService, Mockito.never()).createArticle(any());
    }

    @Test
    public void testCreateArticleMinSizeValidationFailed() throws Exception {
        ArticleCreateDTO createDTO = new ArticleCreateDTO();
        createDTO.setTitle("");
        createDTO.setText("");
        createDTO.setStatus(ArticleStatus.DRAFT);
        createDTO.setAuthorId(UUID.randomUUID());

        String resultJson = mockMvc
                .perform(post("/api/v1/articles/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(articleService, Mockito.never()).createArticle(any());
    }

    @Test
    public void testCreateArticleMaxSizeValidationFailed() throws Exception {
        ArticleCreateDTO createDTO = new ArticleCreateDTO();
        createDTO.setTitle("Text title".repeat(100));
        createDTO.setText("Some text".repeat(1000));
        createDTO.setStatus(ArticleStatus.DRAFT);
        createDTO.setAuthorId(UUID.randomUUID());

        String resultJson = mockMvc
                .perform(post("/api/v1/articles/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(articleService, Mockito.never()).createArticle(any());
    }

    @Test
    public void testUpdateArticle() throws Exception {
        ArticleReadDTO readDTO = createArticleReadDTO();

        ArticlePutDTO updateDTO = new ArticlePutDTO();
        updateDTO.setTitle("Title");
        updateDTO.setText("Some text");
        updateDTO.setStatus(ArticleStatus.PUBLISHED);

        Mockito.when(articleService.updateArticle(readDTO.getId(), updateDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(put("/api/v1/articles/{id}", readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArticleReadDTO actualArticle = objectMapper.readValue(resultJson, ArticleReadDTO.class);
        Assertions.assertThat(actualArticle).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testUpdateArticleMinSizeValidationFailed() throws Exception {
        ArticlePutDTO updateDTO = new ArticlePutDTO();
        updateDTO.setTitle("");
        updateDTO.setText("");
        updateDTO.setStatus(ArticleStatus.DRAFT);

        String resultJson = mockMvc
                .perform(put("/api/v1/articles/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(articleService, Mockito.never()).updateArticle(any(), any());
    }

    @Test
    public void testUpdateArticleMaxSizeValidationFailed() throws Exception {
        ArticlePutDTO updateDTO = new ArticlePutDTO();
        updateDTO.setTitle("Text title".repeat(100));
        updateDTO.setText("Some text".repeat(1000));
        updateDTO.setStatus(ArticleStatus.DRAFT);

        String resultJson = mockMvc
                .perform(put("/api/v1/articles/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(articleService, Mockito.never()).updateArticle(any(), any());
    }

    @Test
    public void testPatchArticle() throws Exception {
        ArticleReadDTO readDTO = createArticleReadDTO();

        ArticlePatchDTO patchDTO = new ArticlePatchDTO();
        patchDTO.setTitle("Article title");
        patchDTO.setText("Article text");
        patchDTO.setStatus(ArticleStatus.NEED_MODERATION);

        Mockito.when(articleService.patchArticle(readDTO.getId(), patchDTO)).thenReturn(readDTO);

        String resultJson = mockMvc
                .perform(patch("/api/v1/articles/{id}", readDTO.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArticleReadDTO actualArticle = objectMapper.readValue(resultJson, ArticleReadDTO.class);
        Assertions.assertThat(actualArticle).isEqualToComparingFieldByField(readDTO);
    }

    @Test
    public void testPatchArticleMinSizeValidationFailed() throws Exception {
        ArticlePatchDTO patchDTO = new ArticlePatchDTO();
        patchDTO.setTitle("");
        patchDTO.setText("");
        patchDTO.setStatus(ArticleStatus.DRAFT);

        String resultJson = mockMvc
                .perform(patch("/api/v1/articles/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(articleService, Mockito.never()).patchArticle(any(), any());
    }

    @Test
    public void testPatchArticleMaxSizeValidationFailed() throws Exception {
        ArticlePatchDTO patchDTO = new ArticlePatchDTO();
        patchDTO.setTitle("Text title".repeat(100));
        patchDTO.setText("Some text".repeat(1000));
        patchDTO.setStatus(ArticleStatus.DRAFT);

        String resultJson = mockMvc
                .perform(patch("/api/v1/articles/{id}", UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patchDTO)))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();

        ErrorInfo error = objectMapper.readValue(resultJson, ErrorInfo.class);
        Assert.assertEquals(MethodArgumentNotValidException.class, error.getExceptionClass());

        Mockito.verify(articleService, Mockito.never()).patchArticle(any(), any());
    }

    @Test
    public void testDeleteArticle() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/articles/{id}", id))
                .andExpect(status().isOk());

        Mockito.verify(articleService).deleteArticle(id);
    }

    @Test
    public void testGetPublishedArticlesWithPagingAndSorting() throws Exception {
        ArticleReadDTO readDTO = createArticleReadDTO();

        int page = 1;
        int size = 30;

        PageResult<ArticleReadDTO> result = new PageResult<>();
        result.setPage(page);
        result.setPageSize(size);
        result.setTotalElements(120);
        result.setTotalPages(4);
        result.setData(List.of(readDTO));

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "createdAt"));

        Mockito.when(articleService.getAllPublishedArticles(pageRequest)).thenReturn(result);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/")
                .param("page", Integer.toString(page))
                .param("size", Integer.toString(size))
                .param("sort", "createdAt,asc"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageResult<ArticleReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assert.assertEquals(result, actualResult);
    }

    private ArticleReadDTO createArticleReadDTO() {
        return generateObject(ArticleReadDTO.class);
    }

    private ArticleReadExtendedDTO createArticleReadExtendedDTO(UserReadDTO userReadDTO) {
        ArticleReadExtendedDTO dto = generateObject(ArticleReadExtendedDTO.class);
        dto.setAuthor(userReadDTO);
        return dto;
    }

    private UserReadDTO createUserReadDTO() {
        return generateObject(UserReadDTO.class);
    }
}
