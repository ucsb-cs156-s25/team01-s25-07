package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.HelpRequest;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.HelpRequestRepository;

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
 * This is a REST controller for UCSBDates
 */

 @Tag(name = "HelpRequest")
 @RequestMapping("/api/helprequest")
 @RestController
 @Slf4j

 public class HelpRequestController extends ApiController {

    @Autowired
    HelpRequestRepository helpRequestRepository;

    /**
     * List all help requests
     * 
     * @return an iterable of HelpRequest
     */
    @Operation(summary= "List all help requests")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<HelpRequest> allHelpRequests() {
        Iterable<HelpRequest> requests = helpRequestRepository.findAll();
        return requests;
    }

    /**
     * Create a new help request
     * 
     * @param requesterEmail       the email in typical email format
     * @param teamID               the number of the team
     * @param tableOrBreakoutRoom  the table/room the help requster is at
     * @param explanation          the explanation of the problem
     * @param solved               the solved status of the request
     * @param localDateTime        the date of the request
     * @return the saved helprequest
     */
    @Operation(summary= "Create a new help request")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public HelpRequest postHelpRequest(
            @Parameter(name="requesterEmail") @RequestParam String requesterEmail,
            @Parameter(name="teamID") @RequestParam String teamID,
            @Parameter(name="tableOrBreakoutRoom") @RequestParam String tableOrBreakoutRoom,
            @Parameter(name="explanation") @RequestParam String explanation,
            @Parameter(name="solved") @RequestParam boolean solved,
            @Parameter(name="localDateTime", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("localDateTime") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime localDateTime)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("localDateTime={}", localDateTime);

        HelpRequest helpRequest = new HelpRequest();
        helpRequest.setRequesterEmail(requesterEmail);
        helpRequest.setTeamID(teamID);
        helpRequest.setTableOrBreakoutRoom(tableOrBreakoutRoom);
        helpRequest.setExplanation(explanation);
        helpRequest.setSolved(solved);
        helpRequest.setLocalDateTime(localDateTime);

        HelpRequest savedHelpRequest = helpRequestRepository.save(helpRequest);

        return savedHelpRequest;
    }
}
