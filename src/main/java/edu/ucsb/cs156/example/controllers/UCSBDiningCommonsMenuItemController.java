package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
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
/**
 * This is a REST controller for UCSBDiningCommonsMenuItem
 */
@Tag(name = "MenuItems")
@RequestMapping("/api/menuitems")
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController{
    @Autowired
    UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

    /**
     * List all UCSB Dining Commons Menu Items
     * 
     * @return an iterable of UCSBDiningCommonsMenuItem
     */
    @Operation(summary= "List all ucsb dining commons menu items")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
        Iterable<UCSBDiningCommonsMenuItem> items = ucsbDiningCommonsMenuItemRepository.findAll();
        return items;
    }

    /**
     * Create a new item
     * 
     * @param diningCommonsCode  the Dining Common's name
     * @param name          the name of the item
     * @param station       the station
     * @return the saved item
     */
    @Operation(summary= "Create a new item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItems(
            @Parameter(name="diningCommonsCode") @RequestParam String diningCommonsCode,
            @Parameter(name="name") @RequestParam String name,
            @Parameter(name="station") @RequestParam String station)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        UCSBDiningCommonsMenuItem item = new UCSBDiningCommonsMenuItem();
        item.setDiningCommonsCode(diningCommonsCode);
        item.setName(name);
        item.setStation(station);

        UCSBDiningCommonsMenuItem savedItem = ucsbDiningCommonsMenuItemRepository.save(item);

        return savedItem;
    }
    /**
     * Get a single menu item by id
     * 
     * @param id the id of the item
     * @return a UCSBDiningCommonsMenuItem
     */
    @Operation(summary= "Get a single item")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("")
    public UCSBDiningCommonsMenuItem getById(
            @Parameter(name="id") @RequestParam Long id) {
                UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        return ucsbDiningCommonsMenuItem;
    }

    /**
     * Update a single menu item
     * 
     * @param id       id of the item to update
     * @param incoming the new item
     * @return the updated item object
     */
    @Operation(summary= "Update a single item")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PutMapping("")
    public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
            @Parameter(name="id") @RequestParam Long id,
            @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

                UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = ucsbDiningCommonsMenuItemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

        ucsbDiningCommonsMenuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
        ucsbDiningCommonsMenuItem.setName(incoming.getName());
        ucsbDiningCommonsMenuItem.setStation(incoming.getStation());

        ucsbDiningCommonsMenuItemRepository.save(ucsbDiningCommonsMenuItem);

        return ucsbDiningCommonsMenuItem;
    }
}
