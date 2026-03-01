package com.journeo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.journeo.BackendApplication;
import com.journeo.dto.ActivityRequestDTO;
import com.journeo.model.Activity;
import com.journeo.model.Guide;
import com.journeo.model.User;
import com.journeo.repository.ActivityRepository;
import com.journeo.repository.GuideRepository;
import com.journeo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Activity Controller Tests")
public class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private UserRepository userRepository;

    private Guide testGuide;
    private Activity testActivity;

    @BeforeEach
    void setUp() {
        activityRepository.deleteAll();
        guideRepository.deleteAll();
        userRepository.deleteAll();

        testGuide = new Guide(
            "Paris City Tour",
            "A beautiful tour of Paris landmarks",
            3,
            Guide.Mobilite.A_PIED,
            Guide.Saison.ETE,
            Guide.PublicCible.FAMILLE
        );
        testGuide = guideRepository.save(testGuide);

        testActivity = new Activity();
        testActivity.setTitre("Visite du Louvre");
        testActivity.setDescription("Le plus grand musée du monde");
        testActivity.setType(Activity.Type.MUSEE);
        testActivity.setDuree(180);
        testActivity.setOrdre(1);
        testActivity.setJour(1);
        testGuide.addActivity(testActivity);
        testGuide = guideRepository.save(testGuide);
        testActivity = activityRepository.findByGuide(testGuide).get(0);
    }

    private ActivityRequestDTO buildValidDTO() {
        ActivityRequestDTO dto = new ActivityRequestDTO();
        dto.setTitre("Tour Eiffel");
        dto.setDescription("Monument emblématique");
        dto.setType(Activity.Type.ACTIVITE);
        dto.setDuree(120);
        dto.setOrdre(2);
        dto.setJour(1);
        return dto;
    }

    @Nested
    @DisplayName("GET /api/activities/guide/{guideId} - List activities of a guide")
    class GetActivitiesTests {

        @Test
        @DisplayName("Should return activities of a guide as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnActivitiesAsAdmin() throws Exception {
            mockMvc.perform(get("/api/activities/guide/{guideId}", testGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[*].titre", hasItem("Visite du Louvre")));
        }

        @Test
        @DisplayName("Should return activities of a guide as USER")
        @WithMockUser(roles = "USER")
        void shouldReturnActivitiesAsUser() throws Exception {
            mockMvc.perform(get("/api/activities/guide/{guideId}", testGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
        }

        @Test
        @DisplayName("Should return 404 when guide not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenGuideNotFound() throws Exception {
            mockMvc.perform(get("/api/activities/guide/9999"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/activities/guide/{guideId}", testGuide.getId()))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return empty set when guide has no activities")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnEmptySetWhenNoActivities() throws Exception {
            Guide emptyGuide = guideRepository.save(new Guide(
                "Empty Guide", null, 1,
                Guide.Mobilite.VOITURE, Guide.Saison.HIVER, Guide.PublicCible.SEUL
            ));

            mockMvc.perform(get("/api/activities/guide/{guideId}", emptyGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }
    }

    @Nested
    @DisplayName("POST /api/activities/guide/{guideId} - Add activity to guide")
    class AddActivityTests {

        @Test
        @DisplayName("Should add activity to guide successfully as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldAddActivitySuccessfully() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();

            mockMvc.perform(post("/api/activities/guide/{guideId}", testGuide.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", equalTo("Tour Eiffel")))
                .andExpect(jsonPath("$.type", equalTo("ACTIVITE")))
                .andExpect(jsonPath("$.duree", equalTo(120)))
                .andExpect(jsonPath("$.ordre", equalTo(2)))
                .andExpect(jsonPath("$.jour", equalTo(1)));

            assertThat(activityRepository.findByGuide(testGuide)).hasSize(2);
        }

        @Test
        @DisplayName("Should return 404 when guide not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenGuideNotFound() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();

            mockMvc.perform(post("/api/activities/guide/9999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 without ADMIN role")
        @WithMockUser(roles = "USER")
        void shouldReturn403WithoutAdminRole() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();

            mockMvc.perform(post("/api/activities/guide/{guideId}", testGuide.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();

            mockMvc.perform(post("/api/activities/guide/{guideId}", testGuide.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 when titre is blank")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenTitreIsBlank() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();
            dto.setTitre("");

            mockMvc.perform(post("/api/activities/guide/{guideId}", testGuide.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when type is null")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenTypeIsNull() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();
            dto.setType(null);

            mockMvc.perform(post("/api/activities/guide/{guideId}", testGuide.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should persist all optional fields")
        @WithMockUser(roles = "ADMIN")
        void shouldPersistOptionalFields() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();
            dto.setAdresse("Champ de Mars, Paris");
            dto.setTelephone("+33 1 44 11 23 23");
            dto.setSiteInternet("https://www.toureiffel.paris");
            dto.setHeureDebut("10:00");

            mockMvc.perform(post("/api/activities/guide/{guideId}", testGuide.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adresse", equalTo("Champ de Mars, Paris")))
                .andExpect(jsonPath("$.telephone", equalTo("+33 1 44 11 23 23")))
                .andExpect(jsonPath("$.heureDebut", equalTo("10:00")));
        }
    }

    @Nested
    @DisplayName("PUT /api/activities/{activityId} - Update activity")
    class UpdateActivityTests {

        @Test
        @DisplayName("Should update activity successfully as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateActivitySuccessfully() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();
            dto.setTitre("Louvre mis à jour");
            dto.setOrdre(1);

            mockMvc.perform(put("/api/activities/{activityId}", testActivity.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", equalTo("Louvre mis à jour")));
        }

        @Test
        @DisplayName("Should return 404 when activity not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenActivityNotFound() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();

            mockMvc.perform(put("/api/activities/9999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 without ADMIN role")
        @WithMockUser(roles = "USER")
        void shouldReturn403WithoutAdminRole() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();

            mockMvc.perform(put("/api/activities/{activityId}", testActivity.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 400 when validation fails on update")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn400WhenValidationFailsOnUpdate() throws Exception {
            ActivityRequestDTO dto = buildValidDTO();
            dto.setTitre("");

            mockMvc.perform(put("/api/activities/{activityId}", testActivity.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("DELETE /api/activities/{activityId} - Delete activity")
    class DeleteActivityTests {

        @Test
        @DisplayName("Should delete activity successfully as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteActivitySuccessfully() throws Exception {
            mockMvc.perform(delete("/api/activities/{activityId}", testActivity.getId())
                .with(csrf()))
                .andExpect(status().isOk());

            assertThat(activityRepository.findByGuide(testGuide)).isEmpty();
        }

        @Test
        @DisplayName("Should return 404 when activity not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenActivityNotFound() throws Exception {
            mockMvc.perform(delete("/api/activities/9999")
                .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 without ADMIN role")
        @WithMockUser(roles = "USER")
        void shouldReturn403WithoutAdminRole() throws Exception {
            mockMvc.perform(delete("/api/activities/{activityId}", testActivity.getId())
                .with(csrf()))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/activities/{activityId}", testActivity.getId())
                .with(csrf()))
                .andExpect(status().isUnauthorized());
        }
    }
}
