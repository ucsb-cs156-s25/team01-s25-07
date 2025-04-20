package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.util.Optional;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

    @MockBean
    ArticlesRepository articlesRepository;

    @MockBean
    UserRepository userRepository;

    // Authorization tests for /api/articles/all

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
        mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().is(200)); // logged in users can get all
    }

    @Test
    public void logged_out_users_cannot_get_by_id() throws Exception {
        mockMvc.perform(get("/api/articles?id=7"))
                .andExpect(status().is(403)); // logged out users can't get by id
    }

    // Authorization tests for /api/articles/post

    @Test
    public void logged_out_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/articles/post"))
                .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
        mockMvc.perform(post("/api/articles/post"))
                .andExpect(status().is(403)); // only admins can post
    }

    // Tests with mocks for database actions

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

        // arrange

        Article article = Article.builder()
                .title("Using AI to automate testing")
                .url("https://example.org/article1")
                .explanation("An article about AI testing")
                .email("phtcon@ucsb.edu")
                .dateAdded(LocalDateTime.parse("2022-01-03T00:00:00"))
                .build();

        when(articlesRepository.findById(eq(7L))).thenReturn(Optional.of(article));

        // act
        MvcResult response = mockMvc.perform(get("/api/articles?id=7"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(articlesRepository, times(1)).findById(eq(7L));
        String expectedJson = mapper.writeValueAsString(article);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

        // arrange

        when(articlesRepository.findById(eq(7L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(get("/api/articles?id=7"))
                .andExpect(status().isNotFound()).andReturn();

        // assert

        verify(articlesRepository, times(1)).findById(eq(7L));
        Map<String, Object> json = responseToJson(response);
        assertEquals("EntityNotFoundException", json.get("type"));
        assertEquals("Article with id 7 not found", json.get("message"));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_articles() throws Exception {

        // arrange

        Article article1 = Article.builder()
                .title("Using AI to automate testing")
                .url("https://example.org/article1")
                .explanation("An article about AI testing")
                .email("phtcon@ucsb.edu")
                .dateAdded(LocalDateTime.parse("2022-01-03T00:00:00"))
                .build();

        Article article2 = Article.builder()
                .title("LLMs for generating test cases")
                .url("https://example.org/article2")
                .explanation("An article about LLMs and testing")
                .email("phtcon@ucsb.edu")
                .dateAdded(LocalDateTime.parse("2022-03-11T00:00:00"))
                .build();

        ArrayList<Article> expectedArticles = new ArrayList<>();
        expectedArticles.addAll(Arrays.asList(article1, article2));

        when(articlesRepository.findAll()).thenReturn(expectedArticles);

        // act
        MvcResult response = mockMvc.perform(get("/api/articles/all"))
                .andExpect(status().isOk()).andReturn();

        // assert

        verify(articlesRepository, times(1)).findAll();
        String expectedJson = mapper.writeValueAsString(expectedArticles);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_article() throws Exception {
        // arrange

        Article article = Article.builder()
                .title("Using AI to automate testing")
                .url("https://example.org/article1")
                .explanation("An article about AI testing")
                .email("phtcon@ucsb.edu")
                .dateAdded(LocalDateTime.parse("2022-01-03T00:00:00"))
                .build();

        when(articlesRepository.save(eq(article))).thenReturn(article);

        // act
        MvcResult response = mockMvc.perform(
                post("/api/articles/post?title=Using AI to automate testing&url=https://example.org/article1&explanation=An article about AI testing&email=phtcon@ucsb.edu&dateAdded=2022-01-03T00:00:00")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(articlesRepository, times(1)).save(article);
        String expectedJson = mapper.writeValueAsString(article);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_article_with_date_only() throws Exception {
        // arrange
        LocalDateTime expectedDateTime = LocalDateTime.parse("2022-01-03T00:00:00");
        
        Article article = Article.builder()
                .title("Using AI to automate testing")
                .url("https://example.org/article1")
                .explanation("An article about AI testing")
                .email("phtcon@ucsb.edu")
                .dateAdded(expectedDateTime)
                .build();

        when(articlesRepository.save(any(Article.class))).thenReturn(article);

        // act
        MvcResult response = mockMvc.perform(
                post("/api/articles/post?title=Using AI to automate testing&url=https://example.org/article1&explanation=An article about AI testing&email=phtcon@ucsb.edu&dateAdded=2022-01-03")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(articlesRepository, times(1)).save(any(Article.class));
        String expectedJson = mapper.writeValueAsString(article);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_delete_an_article() throws Exception {
        // arrange

        Article article = Article.builder()
                .title("Using AI to automate testing")
                .url("https://example.org/article1")
                .explanation("An article about AI testing")
                .email("phtcon@ucsb.edu")
                .dateAdded(LocalDateTime.parse("2022-01-03T00:00:00"))
                .build();

        when(articlesRepository.findById(eq(15L))).thenReturn(Optional.of(article));

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/articles?id=15")
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(articlesRepository, times(1)).findById(15L);
        verify(articlesRepository, times(1)).delete(any());

        Map<String, Object> json = responseToJson(response);
        assertEquals("Article with id 15 deleted", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_tries_to_delete_non_existent_article_and_gets_right_error_message()
            throws Exception {
        // arrange

        when(articlesRepository.findById(eq(15L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                delete("/api/articles?id=15")
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(articlesRepository, times(1)).findById(15L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("Article with id 15 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_can_edit_an_existing_article() throws Exception {
        // arrange

        Article articleOrig = Article.builder()
                .title("Using AI to automate testing")
                .url("https://example.org/article1")
                .explanation("An article about AI testing")
                .email("phtcon@ucsb.edu")
                .dateAdded(LocalDateTime.parse("2022-01-03T00:00:00"))
                .build();

        Article articleEdited = Article.builder()
                .title("Using ML to automate testing")
                .url("https://example.org/article1-updated")
                .explanation("An updated article about ML testing")
                .email("phtcon@ucsb.edu")
                .dateAdded(LocalDateTime.parse("2022-01-04T00:00:00"))
                .build();

        String requestBody = mapper.writeValueAsString(articleEdited);

        when(articlesRepository.findById(eq(67L))).thenReturn(Optional.of(articleOrig));

        // act
        MvcResult response = mockMvc.perform(
                put("/api/articles?id=67")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isOk()).andReturn();

        // assert
        verify(articlesRepository, times(1)).findById(67L);
        verify(articlesRepository, times(1)).save(articleEdited); // should be saved with updated info
        String responseString = response.getResponse().getContentAsString();
        assertEquals(requestBody, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void admin_cannot_edit_article_that_does_not_exist() throws Exception {
        // arrange

        Article articleEdited = Article.builder()
                .title("Using ML to automate testing")
                .url("https://example.org/article1-updated")
                .explanation("An updated article about ML testing")
                .email("phtcon@ucsb.edu")
                .dateAdded(LocalDateTime.parse("2022-01-04T00:00:00"))
                .build();

        String requestBody = mapper.writeValueAsString(articleEdited);

        when(articlesRepository.findById(eq(67L))).thenReturn(Optional.empty());

        // act
        MvcResult response = mockMvc.perform(
                put("/api/articles?id=67")
                        .contentType(MediaType.APPLICATION_JSON)
                        .characterEncoding("utf-8")
                        .content(requestBody)
                        .with(csrf()))
                .andExpect(status().isNotFound()).andReturn();

        // assert
        verify(articlesRepository, times(1)).findById(67L);
        Map<String, Object> json = responseToJson(response);
        assertEquals("Article with id 67 not found", json.get("message"));
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void test_post_article_with_invalid_date_format() throws Exception {
        // arrange
        String invalidDateFormat = "2022/01/03"; // Using slashes instead of dashes
        
        // act & assert
        mockMvc.perform(
                post("/api/articles/post?title=Using AI to automate testing&url=https://example.org/article1&explanation=An article about AI testing&email=phtcon@ucsb.edu&dateAdded=" + invalidDateFormat)
                        .with(csrf()))
                .andExpect(status().isBadRequest()); // Expecting a 400 Bad Request
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_get_articles_by_date_range() throws Exception {
        // arrange
        LocalDateTime startDate = LocalDateTime.parse("2022-01-01T00:00:00");
        LocalDateTime endDate = LocalDateTime.parse("2022-01-31T23:59:59");
        
        Article article1 = Article.builder()
                .title("Article 1")
                .url("url1")
                .explanation("explanation1")
                .email("email1")
                .dateAdded(LocalDateTime.parse("2022-01-05T00:00:00"))
                .build();
                
        Article article2 = Article.builder()
                .title("Article 2")
                .url("url2")
                .explanation("explanation2")
                .email("email2")
                .dateAdded(LocalDateTime.parse("2022-01-15T00:00:00"))
                .build();
                
        ArrayList<Article> expectedArticles = new ArrayList<>();
        expectedArticles.addAll(Arrays.asList(article1, article2));
        
        when(articlesRepository.findByDateAddedBetween(eq(startDate), eq(endDate))).thenReturn(expectedArticles);
        
        // act & assert
        MvcResult response = mockMvc.perform(
                get("/api/articles/bydate?startDate=2022-01-01&endDate=2022-01-31"))
                .andExpect(status().isOk()).andReturn();
                
        // assert
        verify(articlesRepository, times(1)).findByDateAddedBetween(eq(startDate), eq(endDate));
        String expectedJson = mapper.writeValueAsString(expectedArticles);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }
    
    @WithMockUser(roles = { "USER" })
    @Test
    public void test_get_articles_by_date_range_with_time() throws Exception {
        // arrange
        LocalDateTime startDate = LocalDateTime.parse("2022-01-01T10:00:00");
        LocalDateTime endDate = LocalDateTime.parse("2022-01-31T14:00:00");
        
        Article article1 = Article.builder()
                .title("Article 1")
                .url("url1")
                .explanation("explanation1")
                .email("email1")
                .dateAdded(LocalDateTime.parse("2022-01-05T12:00:00"))
                .build();
                
        ArrayList<Article> expectedArticles = new ArrayList<>();
        expectedArticles.add(article1);
        
        when(articlesRepository.findByDateAddedBetween(eq(startDate), eq(endDate))).thenReturn(expectedArticles);
        
        // act & assert
        MvcResult response = mockMvc.perform(
                get("/api/articles/bydate?startDate=2022-01-01T10:00:00&endDate=2022-01-31T14:00:00"))
                .andExpect(status().isOk()).andReturn();
                
        // assert
        verify(articlesRepository, times(1)).findByDateAddedBetween(eq(startDate), eq(endDate));
        String expectedJson = mapper.writeValueAsString(expectedArticles);
        String responseString = response.getResponse().getContentAsString();
        assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_get_articles_by_date_range_invalid_date_format() throws Exception {
        // act & assert
        mockMvc.perform(
                get("/api/articles/bydate?startDate=01/01/2022&endDate=01/31/2022"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void logged_out_users_cannot_get_by_date_range() throws Exception {
        mockMvc.perform(get("/api/articles/bydate?startDate=2022-01-01&endDate=2022-01-31"))
                .andExpect(status().is(403)); // logged out users can't access
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void test_helper_method_parseDateTime() throws Exception {
        // This test indirectly tests the parseDateTime method through the API endpoints
        
        // Test with full datetime format
        mockMvc.perform(
                get("/api/articles/bydate?startDate=2022-01-01T00:00:00&endDate=2022-01-31T23:59:59"))
                .andExpect(status().isOk());
        
        // Test with date-only format
        mockMvc.perform(
                get("/api/articles/bydate?startDate=2022-01-01&endDate=2022-01-31"))
                .andExpect(status().isOk());
    }
}
