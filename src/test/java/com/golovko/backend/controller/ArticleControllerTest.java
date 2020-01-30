package com.golovko.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.golovko.backend.domain.ArticleStatus;
import com.golovko.backend.domain.Movie;
import com.golovko.backend.dto.article.ArticleReadDTO;
import com.golovko.backend.dto.article.ArticleReadExtendedDTO;
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
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
    public void getArticleByIdTest() throws Exception {
        UUID authorId = UUID.randomUUID();
        ArticleReadDTO readDTO = createArticleReadDTO(authorId);

        Mockito.when(articleService.getArticle(readDTO.getId())).thenReturn(readDTO);

        String resultJson = mockMvc.perform(get("/api/v1/articles/{id}", readDTO.getId()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArticleReadDTO actualArticle = objectMapper.readValue(resultJson, ArticleReadDTO.class);
        Assertions.assertThat(actualArticle).isEqualToComparingFieldByField(readDTO);

        Mockito.verify(articleService).getArticle(readDTO.getId());
    }

    @Test
    public void getArticleByWrongIdTest() throws Exception {
        UUID wrongId = UUID.randomUUID();

        EntityNotFoundException exception = new EntityNotFoundException(Movie.class, wrongId);

        Mockito.when(articleService.getArticle(wrongId)).thenThrow(exception);

        String result = mockMvc.perform(get("/api/v1/articles/{id}", wrongId))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getContentAsString();

        Assert.assertTrue(result.contains(exception.getMessage()));
    }

    @Test
    public void getArticleExtendedTest() throws Exception {
        UserReadDTO userReadDTO = createUserReadDTO();
        ArticleReadExtendedDTO extendedDTO = createArticleReadExtendedDTO(userReadDTO);

        Mockito.when(articleService.getArticleExtended(extendedDTO.getId())).thenReturn(extendedDTO);

        String resultJson = mockMvc.perform(get("/api/v1/articles/{id}/extended", extendedDTO.getId()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ArticleReadExtendedDTO actualDTO = objectMapper.readValue(resultJson, ArticleReadExtendedDTO.class);
        Assertions.assertThat(actualDTO).isEqualToComparingFieldByField(extendedDTO);

        Mockito.verify(articleService).getArticleExtended(extendedDTO.getId());
    }

    private ArticleReadDTO createArticleReadDTO(UUID authorId) {
        ArticleReadDTO dto = new ArticleReadDTO();
        dto.setId(UUID.randomUUID());
        dto.setTitle("BREAKING NEWS");
        dto.setText("Some text");
        dto.setPublishedDate(Instant.now());
        dto.setStatus(ArticleStatus.PUBLISHED);
        dto.setDislikesCount(5656);
        dto.setLikesCount(100000);
        dto.setAuthorId(authorId);
        return dto;
    }

    public ArticleReadExtendedDTO createArticleReadExtendedDTO(UserReadDTO authorDTO) {
        ArticleReadExtendedDTO dto = new ArticleReadExtendedDTO();
        dto.setId(UUID.randomUUID());
        dto.setTitle("BREAKING NEWS");
        dto.setText("Some text");
        dto.setPublishedDate(Instant.now());
        dto.setStatus(ArticleStatus.PUBLISHED);
        dto.setDislikesCount(5656);
        dto.setLikesCount(100000);
        dto.setAuthor(authorDTO);
        return dto;
    }

    private UserReadDTO createUserReadDTO() {
        UserReadDTO readDTO = new UserReadDTO();
        readDTO.setId(UUID.randomUUID());
        readDTO.setUsername("david");
        readDTO.setEmail("david101@email.com");
        readDTO.setPassword("12345");
        return readDTO;
    }
}
