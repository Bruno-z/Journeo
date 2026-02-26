package com.journeo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.journeo.BackendApplication;
import com.journeo.dto.UserRequestDTO;
import com.journeo.model.Guide;
import com.journeo.model.User;
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
@DisplayName("User Controller Tests")
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuideRepository guideRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        guideRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("existing@example.com", "password", User.Role.USER);
        userRepository.save(testUser);
    }

    // -------------------------------------------------------------------------
    // GET /api/users/ping
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/users/ping - Health check")
    class PingTests {

        @Test
        @DisplayName("Should return pong without authentication")
        void shouldReturnPong() throws Exception {
            mockMvc.perform(get("/api/users/ping"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("pong"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/users - Create user
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/users - Create user")
    class CreateUserTests {

        @Test
        @DisplayName("Should create user without authentication (public endpoint)")
        void shouldCreateUserPublicly() throws Exception {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setEmail("new@example.com");
            dto.setPassword("password123");
            dto.setRole("USER");

            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated())
                    .andExpect(header().exists("Location"))
                    .andExpect(jsonPath("$.email").value("new@example.com"))
                    .andExpect(jsonPath("$.role").value("USER"))
                    .andExpect(jsonPath("$.id").isNumber());
        }

        @Test
        @DisplayName("Should return 409 when email is already in use")
        void shouldReturn409OnDuplicateEmail() throws Exception {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setEmail("existing@example.com");
            dto.setPassword("password123");
            dto.setRole("USER");

            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 when email is missing")
        void shouldReturn400WhenEmailMissing() throws Exception {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setPassword("password123");
            dto.setRole("USER");

            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when password is missing")
        void shouldReturn400WhenPasswordMissing() throws Exception {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setEmail("new@example.com");
            dto.setRole("USER");

            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when role is invalid")
        void shouldReturn400WhenRoleIsInvalid() throws Exception {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setEmail("new@example.com");
            dto.setPassword("password123");
            dto.setRole("INVALID_ROLE");

            mockMvc.perform(post("/api/users")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/users - List all users
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/users - List all users")
    class GetAllUsersTests {

        @Test
        @DisplayName("Should return all users as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAllUsersAsAdmin() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].email").value("existing@example.com"));
        }

        @Test
        @DisplayName("Should return 403 for USER role")
        @WithMockUser(roles = "USER")
        void shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isUnauthorized());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/users/{id} - Get user by ID
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/users/{id} - Get user by ID")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return user when found as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnUserWhenFound() throws Exception {
            mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(testUser.getId()))
                    .andExpect(jsonPath("$.email").value("existing@example.com"))
                    .andExpect(jsonPath("$.role").value("USER"));
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(get("/api/users/9999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 for USER role")
        @WithMockUser(roles = "USER")
        void shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/users/{id} - Update user
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/users/{id} - Update user")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update email successfully as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateUserSuccessfully() throws Exception {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setEmail("updated@example.com");
            dto.setRole("ADMIN");

            mockMvc.perform(put("/api/users/{id}", testUser.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("updated@example.com"))
                    .andExpect(jsonPath("$.role").value("ADMIN"));
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setEmail("updated@example.com");
            dto.setRole("USER");

            mockMvc.perform(put("/api/users/9999")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 409 when new email is already taken")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn409WhenEmailAlreadyTaken() throws Exception {
            User otherUser = new User("other@example.com", "password", User.Role.USER);
            userRepository.save(otherUser);

            UserRequestDTO dto = new UserRequestDTO();
            dto.setEmail("other@example.com"); // already used by otherUser
            dto.setRole("USER");

            mockMvc.perform(put("/api/users/{id}", testUser.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 403 for USER role")
        @WithMockUser(roles = "USER")
        void shouldReturn403ForUserRole() throws Exception {
            UserRequestDTO dto = new UserRequestDTO();
            dto.setEmail("updated@example.com");
            dto.setRole("USER");

            mockMvc.perform(put("/api/users/{id}", testUser.getId())
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/users/{id} - Delete user
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/users/{id} - Delete user")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user successfully as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteUserSuccessfully() throws Exception {
            mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                            .with(csrf()))
                    .andExpect(status().isOk());

            assertThat(userRepository.existsById(testUser.getId())).isFalse();
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            mockMvc.perform(delete("/api/users/9999")
                            .with(csrf()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 for USER role")
        @WithMockUser(roles = "USER")
        void shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                            .with(csrf()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when unauthenticated")
        void shouldReturn401WhenUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                            .with(csrf()))
                    .andExpect(status().isUnauthorized());
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/users/{id}/guides - Get guides of a user
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/users/{id}/guides - Get assigned guides")
    class GetUserGuidesTests {

        @Test
        @DisplayName("Should return empty list when user has no assigned guides")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnEmptyListWhenNoGuides() throws Exception {
            mockMvc.perform(get("/api/users/{id}/guides", testUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Should return assigned guides for user")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAssignedGuides() throws Exception {
            Guide guide = new Guide("Paris Tour", "desc", 3,
                    Guide.Mobilite.A_PIED, Guide.Saison.ETE, Guide.PublicCible.FAMILLE);
            guide.addUser(testUser);
            guideRepository.save(guide);

            mockMvc.perform(get("/api/users/{id}/guides", testUser.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].titre").value("Paris Tour"));
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenUserNotFound() throws Exception {
            mockMvc.perform(get("/api/users/9999/guides"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should return 403 for USER role")
        @WithMockUser(roles = "USER")
        void shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(get("/api/users/{id}/guides", testUser.getId()))
                    .andExpect(status().isForbidden());
        }
    }
}
