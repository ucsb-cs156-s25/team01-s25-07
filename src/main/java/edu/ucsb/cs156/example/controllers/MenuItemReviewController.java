package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

@Tag(name = "MenuItemReview")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

    @Autowired
    MenuItemReviewRepository menuItemReviewRepository;

    /**
     * List all MenuItemReview (MIR)
     * 
     * @return an iterable of MIR
     */
    @Operation(summary= "List all Menu Item Review")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<MenuItemReview> allMIRs() {
        Iterable<MenuItemReview> mirs = menuItemReviewRepository.findAll();
        return mirs;
    }

    /**
     * Create a new MIR
     * 
     * @param itemId            the reviewed item id
     * @param reviewerEmail     the email of the reviewer
     * @param stars             the number of stars
     * @param comments          the comments content
     * @return the created MIR
     */
    @Operation(summary= "Create a new Menu Item Review")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public MenuItemReview postMIR(
            @Parameter(name="itemId") @RequestParam long itemId,
            @Parameter(name="reviewerEmail") @RequestParam String reviewerEmail,
            @Parameter(name="stars") @RequestParam int stars,
            @Parameter(name="comments") @RequestParam String comments)
            throws JsonProcessingException {

        MenuItemReview mir = new MenuItemReview();
        mir.setItemId(itemId);
        mir.setReviewerEmail(reviewerEmail);
        mir.setStars(stars);
        mir.setComments(comments);
        MenuItemReview savedMIR = menuItemReviewRepository.save(mir);
        return savedMIR;
    }
}