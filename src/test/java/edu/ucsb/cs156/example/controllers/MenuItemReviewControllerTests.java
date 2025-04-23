package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import java.time.LocalDateTime;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebMvcTest(controllers = MenuItemReviewController.class)
@Import(TestConfig.class)
public class MenuItemReviewControllerTests extends ControllerTestCase{
    
    @MockBean
    MenuItemReviewRepository MenuItemReviewRepository;

    @MockBean
    UserRepository userRepository;

    @Test
    public void logged_out_users_cannot_get_all() throws Exception {
            mockMvc.perform(get("/api/menuitemreviews/all"))
                            .andExpect(status().is(403)); // logged out users can't get all
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_users_can_get_all() throws Exception {
            mockMvc.perform(get("/api/menuitemreviews/all"))
                            .andExpect(status().is(200)); // logged
    }

    @Test
    public void logged_out_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/menuitemreviews/post"))
                            .andExpect(status().is(403));
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_regular_users_cannot_post() throws Exception {
            mockMvc.perform(post("/api/menuitemreviews/post"))
                            .andExpect(status().is(403)); // only admins can post
    }

    @WithMockUser(roles = { "USER" })
    @Test
    public void logged_in_user_can_get_all_ucsbdates() throws Exception {

            // arrange
            MenuItemReview MIR1 = MenuItemReview.builder()
                            .itemId(1)
                            .reviewerEmail("yy@ucsb.edu")
                            .stars(5)
                            .comments("Great food!")
                            .build();
            
            ArrayList<MenuItemReview> expectedMIRs = new ArrayList<>();
            expectedMIRs.addAll(Arrays.asList(MIR1));

            when(MenuItemReviewRepository.findAll()).thenReturn(expectedMIRs);

            // act
            MvcResult response = mockMvc.perform(get("/api/menuitemreviews/all"))
                            .andExpect(status().isOk()).andReturn();

            // assert

            verify(MenuItemReviewRepository, times(1)).findAll();
            String expectedJson = mapper.writeValueAsString(expectedMIRs);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }

    @WithMockUser(roles = { "ADMIN", "USER" })
    @Test
    public void an_admin_user_can_post_a_new_MIR() throws Exception {
            // arrange
            MenuItemReview MIR1 = MenuItemReview.builder()
                            .itemId(1)
                            .reviewerEmail("yy@ucsb.edu")
                            .stars(5)
                            .comments("Great food!")
                            .build();

            when(MenuItemReviewRepository.save(eq(MIR1))).thenReturn(MIR1);

            // act
            MvcResult response = mockMvc.perform(
                            post("/api/menuitemreviews/post?itemId=1&reviewerEmail=yy@ucsb.edu&stars=5&comments=Great food!")
                                            .with(csrf()))
                            .andExpect(status().isOk()).andReturn();

            // assert
            verify(MenuItemReviewRepository, times(1)).save(eq(MIR1));
            String expectedJson = mapper.writeValueAsString(MIR1);
            String responseString = response.getResponse().getContentAsString();
            assertEquals(expectedJson, responseString);
    }
}
