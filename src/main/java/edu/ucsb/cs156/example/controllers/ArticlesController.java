package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.ArticlesRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * This is a REST controller for Articles
 */

@Tag(name = "Articles")
@RequestMapping("/api/articles")
@RestController
@Slf4j
public class ArticlesController extends ApiController {

    @Autowired
    ArticlesRepository articlesRepository;

    /**
     * List all articles
     * 
     * @return an iterable of Articles
     */
    @Operation(summary= "List all articles")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<Article> allArticles() {
        return articlesRepository.findAll();
    }

    /**
     * Get a single article by id
     * 
     * @param id the id of the article
     * @return an Article
     */
    @Operation(summary= "Get a single article")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public Article getById(
            @Parameter(name="id") @RequestParam Long id) {
        Article article = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Article.class, id));

        return article;
    }

    /**
     * Create a new article
     * 
     * @param title the title of the article
     * @param url the url of the article
     * @param explanation the explanation of the article
     * @param email the email of the person who added the article
     * @param dateAdded the date the article was added
     * @return the saved article
     */
    @Operation(summary= "Create a new article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public Article postArticle(
            @Parameter(name="title") @RequestParam String title,
            @Parameter(name="url") @RequestParam String url,
            @Parameter(name="explanation") @RequestParam String explanation,
            @Parameter(name="email") @RequestParam String email,
            @Parameter(name="dateAdded") @RequestParam String dateAdded)
            throws JsonProcessingException {

        LocalDateTime dateTime;
        try {
            dateTime = parseDateTime(dateAdded);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use ISO date format (YYYY-MM-DD) or ISO date-time format (YYYY-MM-DDThh:mm:ss)");
        }

        log.info("dateAdded={}", dateTime);

        Article article = Article.builder()
                .title(title)
                .url(url)
                .explanation(explanation)
                .email(email)
                .dateAdded(dateTime)
                .build();

        Article savedArticle = articlesRepository.save(article);

        return savedArticle;
    }

    /**
     * Delete an Article
     * 
     * @param id the id of the article to delete
     * @return a message indicating the article was deleted
     */
    @Operation(summary= "Delete an Article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @DeleteMapping("")
    public Object deleteArticle(
            @Parameter(name="id") @RequestParam Long id) {
        Article article = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Article.class, id));

        articlesRepository.delete(article);
        return genericMessage("Article with id %s deleted".formatted(id));
    }

    /**
     * Update a single article
     * 
     * @param id the id of the article to update
     * @param incoming the new article data
     * @return the updated article
     */
    @Operation(summary= "Update a single article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public Article updateArticle(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid Article incoming) {

        Article article = articlesRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(Article.class, id));

        article.setTitle(incoming.getTitle());
        article.setUrl(incoming.getUrl());
        article.setExplanation(incoming.getExplanation());
        article.setEmail(incoming.getEmail());
        article.setDateAdded(incoming.getDateAdded());

        articlesRepository.save(article);

        return article;
    }

    /**
     * Get articles within a date range
     * 
     * @param startDate the start date in ISO format (yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss)
     * @param endDate the end date in ISO format (yyyy-MM-dd or yyyy-MM-ddTHH:mm:ss)
     * @return a list of Articles within the date range
     */
    @Operation(summary= "Get articles within a date range")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/bydate")
    public List<Article> getArticlesByDateRange(
            @Parameter(name="startDate") @RequestParam String startDate,
            @Parameter(name="endDate") @RequestParam String endDate) {
        
        LocalDateTime start, end;
        try {
            start = parseDateTime(startDate);
            end = parseDateTime(endDate);
        } catch (DateTimeParseException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid date format. Use ISO date format (YYYY-MM-DD) or ISO date-time format (YYYY-MM-DDThh:mm:ss)");
        }
        
        return articlesRepository.findByDateAddedBetween(start, end);
    }
    
    /**
     * Helper method to parse date strings in either date-only or date-time format
     */
    private LocalDateTime parseDateTime(String dateString) {
        if (dateString.contains("T")) {
            // Full date-time format
            return LocalDateTime.parse(dateString);
        } else {
            // Date-only format, append T00:00:00 for midnight
            return LocalDateTime.parse(dateString + "T00:00:00");
        }
    }
}
