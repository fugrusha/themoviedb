package com.golovko.backend.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.article.*;
import com.golovko.backend.dto.user.UserReadDTO;
import com.golovko.backend.exception.EntityNotFoundException;
import com.golovko.backend.service.ArticleService;
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
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ArticleController.class)
public class ArticleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ArticleService articleService;

    @Test
    public void testGetArticleById() throws Exception {
        UUID authorId = UUID.randomUUID();
        ArticleReadDTO readDTO = createArticleReadDTO(authorId);

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
        UUID authorId1 = UUID.randomUUID();
        UUID authorId2 = UUID.randomUUID();
        ArticleReadDTO a1 = createArticleReadDTO(authorId1);
        ArticleReadDTO a2 = createArticleReadDTO(authorId1);
        ArticleReadDTO a3 = createArticleReadDTO(authorId2);
        ArticleReadDTO a4 = createArticleReadDTO(authorId2);

        List<ArticleReadDTO> expectedResult = List.of(a1, a2, a3, a4);

        Mockito.when(articleService.getAllArticles()).thenReturn(expectedResult);

        String resultJson = mockMvc
                .perform(get("/api/v1/articles/"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ArticleReadDTO> actualResult = objectMapper.readValue(resultJson, new TypeReference<>() {});
        Assertions.assertThat(actualResult).extracting(ArticleReadDTO::getId)
                .containsExactlyInAnyOrder(a1.getId(), a2.getId(), a3.getId(), a4.getId());

        Mockito.verify(articleService).getAllArticles();
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
        UUID authorId = UUID.randomUUID();
        ArticleReadDTO readDTO = createArticleReadDTO(authorId);

        ArticleCreateDTO createDTO = new ArticleCreateDTO();
        createDTO.setTitle("Text title");
        createDTO.setText("Some text");
        createDTO.setStatus(ArticleStatus.DRAFT);
        createDTO.setAuthorId(authorId);

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
    public void testUpdateArticle() throws Exception {
        UUID authorId = UUID.randomUUID();
        ArticleReadDTO readDTO = createArticleReadDTO(authorId);

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
    public void testPatchArticle() throws Exception {
        UUID authorId = UUID.randomUUID();
        ArticleReadDTO readDTO = createArticleReadDTO(authorId);

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
    public void testDeleteArticle() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/articles/{id}", id))
                .andExpect(status().isOk());

        Mockito.verify(articleService).deleteArticle(id);
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

    private ArticleReadExtendedDTO createArticleReadExtendedDTO(UserReadDTO author) {
        ArticleReadExtendedDTO dto = new ArticleReadExtendedDTO();
        dto.setId(UUID.randomUUID());
        dto.setTitle("Title");
        dto.setText("Some Text");
        dto.setStatus(ArticleStatus.PUBLISHED);
        dto.setAuthor(author);
        dto.setDislikesCount(555);
        dto.setLikesCount(333);
        dto.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        dto.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return dto;
    }

    private UserReadDTO createUserReadDTO() {
        UserReadDTO readDTO = new UserReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setUsername("david");
        readDTO.setEmail("david101@email.com");
        readDTO.setIsBlocked(false);
        readDTO.setCreatedAt(Instant.parse("2019-05-12T12:45:22.00Z"));
        readDTO.setUpdatedAt(Instant.parse("2019-12-01T05:45:12.00Z"));
        return readDTO;
    }
}
