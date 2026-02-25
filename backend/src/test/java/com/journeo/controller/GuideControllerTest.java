package com.journeo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.journeo.BackendApplication;
import com.journeo.dto.GuideRequestDTO;
import com.journeo.model.Guide;
import com.journeo.model.User;
import com.journeo.repository.GuideRepository;
import com.journeo.repository.UserRepository;
import com.journeo.service.GuideService;
import com.journeo.service.UserService;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test Suite for Guide API Endpoints
 *
 * This comprehensive test suite covers all CRUD operations and security validation
 * for the Guide REST API. It ensures proper HTTP status codes, response formats,
 * and business logic validation.
 */
@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Guide Controller Tests")
public class GuideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuideService guideService;

    @Autowired
    private UserService userService;

    private Guide testGuide;
    private User adminUser;
    private User regularUser;

    @BeforeEach
    void setUp() {
        guideRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        adminUser = new User("admin@test.com", "password", User.Role.ADMIN);
        regularUser = new User("user@test.com", "password", User.Role.USER);
        userRepository.saveAll(java.util.List.of(adminUser, regularUser));

        // Create test guide
        testGuide = new Guide(
            "Paris City Tour",
            "A beautiful tour of Paris landmarks",
            3,
            Guide.Mobilite.A_PIED,
            Guide.Saison.ETE,
            Guide.PublicCible.FAMILLE
        );
        testGuide = guideRepository.save(testGuide);
    }

    @Nested
    @DisplayName("GET /api/guides - List all guides")
    class GetAllGuidesTests {

        @Test
        @DisplayName("Should return all guides successfully")
        void shouldReturnAllGuides() throws Exception {
            mockMvc.perform(get("/api/guides"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titre", equalTo("Paris City Tour")))
                .andExpect(jsonPath("$[0].jours", equalTo(3)))
                .andExpect(jsonPath("$[0].mobilite", equalTo("A_PIED")))
                .andExpect(jsonPath("$[0].saison", equalTo("ETE")))
                .andExpect(jsonPath("$[0].pourQui", equalTo("FAMILLE")));
        }

        @Test
        @DisplayName("Should return empty list when no guides exist")
        void shouldReturnEmptyList() throws Exception {
            guideRepository.deleteAll();

            mockMvc.perform(get("/api/guides"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return multiple guides")
        void shouldReturnMultipleGuides() throws Exception {
            Guide guide2 = guideRepository.save(new Guide(
                "Lyon Gastronomy",
                "Food tour in Lyon",
                2,
                Guide.Mobilite.VOITURE,
                Guide.Saison.PRINTEMPS,
                Guide.PublicCible.EN_GROUPE
            ));

            mockMvc.perform(get("/api/guides"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].titre", containsInAnyOrder("Paris City Tour", "Lyon Gastronomy")));
        }

        @Test
        @DisplayName("Should be publicly accessible (no authentication required)")
        void shouldBePubliclyAccessible() throws Exception {
            mockMvc.perform(get("/api/guides")
                .with(csrf()))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/guides/{id} - Get single guide")
    class GetGuideByIdTests {

        @Test
        @DisplayName("Should return guide when found")
        void shouldReturnGuideWhenFound() throws Exception {
            mockMvc.perform(get("/api/guides/{id}", testGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", equalTo(testGuide.getId().intValue())))
                .andExpect(jsonPath("$.titre", equalTo("Paris City Tour")))
                .andExpect(jsonPath("$.description", equalTo("A beautiful tour of Paris landmarks")));
        }

        @Test
        @DisplayName("Should return 404 when guide not found")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/guides/9999"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should be publicly accessible")
        void shouldBePubliclyAccessible() throws Exception {
            mockMvc.perform(get("/api/guides/{id}", testGuide.getId())
                .with(csrf()))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/guides - Create guide")
    class CreateGuideTests {

        @Test
        @DisplayName("Should create guide successfully as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateGuideSuccessfully() throws Exception {
            GuideRequestDTO guideDTO = new GuideRequestDTO();
            guideDTO.setTitre("Rome Historic Tour");
            guideDTO.setDescription("Ancient Rome exploration");
            guideDTO.setJours(4);
            guideDTO.setMobilite("VELO");
            guideDTO.setSaison("AUTOMNE");
            guideDTO.setPourQui("SEUL");

            MvcResult result = mockMvc.perform(post("/api/guides")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guideDTO)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.titre", equalTo("Rome Historic Tour")))
                .andExpect(jsonPath("$.jours", equalTo(4)))
                .andReturn();

            String locationHeader = result.getResponse().getHeader("Location");
            assertThat(locationHeader).contains("/api/guides/");

            // Verify guide was saved to database
            assertThat(guideRepository.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("Should fail without ADMIN role")
        @WithMockUser(roles = "USER")
        void shouldFailWithoutAdminRole() throws Exception {
            GuideRequestDTO guideDTO = new GuideRequestDTO();
            guideDTO.setTitre("Test Guide");
            guideDTO.setJours(1);
            guideDTO.setMobilite("A_PIED");
            guideDTO.setSaison("ETE");
            guideDTO.setPourQui("FAMILLE");

            mockMvc.perform(post("/api/guides")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guideDTO)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should fail when unauthenticated")
        void shouldFailWhenUnauthenticated() throws Exception {
            GuideRequestDTO guideDTO = new GuideRequestDTO();
            guideDTO.setTitre("Test Guide");
            guideDTO.setJours(1);
            guideDTO.setMobilite("A_PIED");
            guideDTO.setSaison("ETE");
            guideDTO.setPourQui("FAMILLE");

            mockMvc.perform(post("/api/guides")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guideDTO)))
                .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should fail with missing required fields")
        @WithMockUser(roles = "ADMIN")
        void shouldFailWithMissingRequiredFields() throws Exception {
            GuideRequestDTO guideDTO = new GuideRequestDTO();
            guideDTO.setDescription("Missing title");
            // titre is missing (required)

            mockMvc.perform(post("/api/guides")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guideDTO)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail with invalid enum values")
        @WithMockUser(roles = "ADMIN")
        void shouldFailWithInvalidEnumValues() throws Exception {
            GuideRequestDTO guideDTO = new GuideRequestDTO();
            guideDTO.setTitre("Test Guide");
            guideDTO.setJours(1);
            guideDTO.setMobilite("INVALID_MOBILITE");
            guideDTO.setSaison("ETE");
            guideDTO.setPourQui("FAMILLE");

            mockMvc.perform(post("/api/guides")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guideDTO)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should accept null description")
        @WithMockUser(roles = "ADMIN")
        void shouldAcceptNullDescription() throws Exception {
            GuideRequestDTO guideDTO = new GuideRequestDTO();
            guideDTO.setTitre("Test Guide");
            guideDTO.setJours(2);
            guideDTO.setMobilite("VOITURE");
            guideDTO.setSaison("HIVER");
            guideDTO.setPourQui("EN_GROUPE");
            // description is null

            mockMvc.perform(post("/api/guides")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(guideDTO)))
                .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("PUT /api/guides/{id} - Update guide")
    class UpdateGuideTests {

        @Test
        @DisplayName("Should update guide successfully as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateGuideSuccessfully() throws Exception {
            GuideRequestDTO updateDTO = new GuideRequestDTO();
            updateDTO.setTitre("Updated Paris Tour");
            updateDTO.setDescription("Updated description");
            updateDTO.setJours(5);
            updateDTO.setMobilite("VELO");
            updateDTO.setSaison("PRINTEMPS");
            updateDTO.setPourQui("EN_GROUPE");

            mockMvc.perform(put("/api/guides/{id}", testGuide.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.titre", equalTo("Updated Paris Tour")))
                .andExpect(jsonPath("$.jours", equalTo(5)))
                .andExpect(jsonPath("$.mobilite", equalTo("VELO")));

            // Verify update in database
            Guide updated = guideRepository.findById(testGuide.getId()).orElse(null);
            assertThat(updated).isNotNull()
                .extracting(Guide::getTitre)
                .isEqualTo("Updated Paris Tour");
        }

        @Test
        @DisplayName("Should return 404 when guide not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            GuideRequestDTO updateDTO = new GuideRequestDTO();
            updateDTO.setTitre("Updated");
            updateDTO.setJours(1);
            updateDTO.setMobilite("A_PIED");
            updateDTO.setSaison("ETE");
            updateDTO.setPourQui("FAMILLE");

            mockMvc.perform(put("/api/guides/9999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should fail without ADMIN role")
        @WithMockUser(roles = "USER")
        void shouldFailWithoutAdminRole() throws Exception {
            GuideRequestDTO updateDTO = new GuideRequestDTO();
            updateDTO.setTitre("Updated");
            updateDTO.setJours(1);
            updateDTO.setMobilite("A_PIED");
            updateDTO.setSaison("ETE");
            updateDTO.setPourQui("FAMILLE");

            mockMvc.perform(put("/api/guides/{id}", testGuide.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/guides/{id} - Delete guide")
    class DeleteGuideTests {

        @Test
        @DisplayName("Should delete guide successfully as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteGuideSuccessfully() throws Exception {
            mockMvc.perform(delete("/api/guides/{id}", testGuide.getId())
                .with(csrf()))
                .andExpect(status().isOk());

            // Verify deletion in database
            assertThat(guideRepository.existsById(testGuide.getId())).isFalse();
        }

        @Test
        @DisplayName("Should return 404 when guide not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(delete("/api/guides/9999")
                .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should fail without ADMIN role")
        @WithMockUser(roles = "USER")
        void shouldFailWithoutAdminRole() throws Exception {
            mockMvc.perform(delete("/api/guides/{id}", testGuide.getId())
                .with(csrf()))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should fail when unauthenticated")
        void shouldFailWhenUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/guides/{id}", testGuide.getId())
                .with(csrf()))
                .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("POST /api/guides/{guideId}/users/{userId} - Add user to guide")
    class AddUserToGuideTests {

        @Test
        @DisplayName("Should add user to guide successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldAddUserToGuideSuccessfully() throws Exception {
            mockMvc.perform(post("/api/guides/{guideId}/users/{userId}", testGuide.getId(), regularUser.getId())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", equalTo(testGuide.getId().intValue())));

            // Verify relationship in database
            Guide updatedGuide = guideRepository.findById(testGuide.getId()).orElse(null);
            assertThat(updatedGuide).isNotNull();
            assertThat(updatedGuide.getUsers()).hasSize(1);
            assertThat(updatedGuide.getUsers()).contains(regularUser);
        }

        @Test
        @DisplayName("Should fail when guide not found")
        @WithMockUser(roles = "ADMIN")
        void shouldFailWhenGuideNotFound() throws Exception {
            mockMvc.perform(post("/api/guides/{guideId}/users/{userId}", 9999, regularUser.getId())
                .with(csrf()))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when user not found")
        @WithMockUser(roles = "ADMIN")
        void shouldFailWhenUserNotFound() throws Exception {
            mockMvc.perform(post("/api/guides/{guideId}/users/{userId}", testGuide.getId(), 9999)
                .with(csrf()))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail without ADMIN role")
        @WithMockUser(roles = "USER")
        void shouldFailWithoutAdminRole() throws Exception {
            mockMvc.perform(post("/api/guides/{guideId}/users/{userId}", testGuide.getId(), regularUser.getId())
                .with(csrf()))
                .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/guides/{guideId}/users/{userId} - Remove user from guide")
    class RemoveUserFromGuideTests {

        @BeforeEach
        void setupUserGuideRelationship() {
            testGuide.addUser(regularUser);
            guideRepository.save(testGuide);
        }

        @Test
        @DisplayName("Should remove user from guide successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldRemoveUserFromGuideSuccessfully() throws Exception {
            mockMvc.perform(delete("/api/guides/{guideId}/users/{userId}", testGuide.getId(), regularUser.getId())
                .with(csrf()))
                .andExpect(status().isOk());

            // Verify relationship removed in database
            Guide updatedGuide = guideRepository.findById(testGuide.getId()).orElse(null);
            assertThat(updatedGuide).isNotNull();
            assertThat(updatedGuide.getUsers()).isEmpty();
        }

        @Test
        @DisplayName("Should fail when guide not found")
        @WithMockUser(roles = "ADMIN")
        void shouldFailWhenGuideNotFound() throws Exception {
            mockMvc.perform(delete("/api/guides/{guideId}/users/{userId}", 9999, regularUser.getId())
                .with(csrf()))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail when user not found")
        @WithMockUser(roles = "ADMIN")
        void shouldFailWhenUserNotFound() throws Exception {
            mockMvc.perform(delete("/api/guides/{guideId}/users/{userId}", testGuide.getId(), 9999)
                .with(csrf()))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should fail without ADMIN role")
        @WithMockUser(roles = "USER")
        void shouldFailWithoutAdminRole() throws Exception {
            mockMvc.perform(delete("/api/guides/{guideId}/users/{userId}", testGuide.getId(), regularUser.getId())
                .with(csrf()))
                .andExpect(status().isForbidden());
        }
    }
}
