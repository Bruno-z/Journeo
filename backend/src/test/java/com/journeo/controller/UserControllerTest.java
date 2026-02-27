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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
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

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        guideRepository.deleteAll();
        userRepository.deleteAll();

        testUser = new User("user@test.com", passwordEncoder.encode("password123"), User.Role.USER);
        adminUser = new User("admin@test.com", passwordEncoder.encode("adminpass"), User.Role.ADMIN);
        userRepository.save(testUser);
        userRepository.save(adminUser);
    }

    private UserRequestDTO buildCreateDTO(String email, String password, String role) {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setRole(role);
        return dto;
    }

    private UserRequestDTO buildUpdateDTO(String email, String role, String password) {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setEmail(email);
        dto.setRole(role);
        dto.setPassword(password); // mot de passe optionnel pour update
        return dto;
    }

    // -------------------------- Ping Tests --------------------------
    @Nested
    @DisplayName("GET /api/users/ping")
    class PingTests {
        @Test
        @DisplayName("Should return pong without authentication")
        void shouldReturnPong() throws Exception {
            mockMvc.perform(get("/api/users/ping"))
                .andExpect(status().isOk());
        }
    }

    // -------------------------- List Users Tests --------------------------
    @Nested
    @DisplayName("GET /api/users - List all users")
    class GetAllUsersTests {
        @Test
        @DisplayName("Should return all users as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAllUsersAsAdmin() throws Exception {
            mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].email", containsInAnyOrder("user@test.com", "admin@test.com")));
        }

        @Test
        @DisplayName("Should return 403 for USER role")
        @WithMockUser(roles = "USER")
        void shouldReturn403ForUserRole() throws Exception {
            mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
        }
    }

    // -------------------------- Get User By ID Tests --------------------------
    @Nested
    @DisplayName("GET /api/users/{id} - Get user by ID")
    class GetUserByIdTests {
        @Test
        @DisplayName("Should return user when found as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnUserWhenFound() throws Exception {
            mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", equalTo("user@test.com")))
                .andExpect(jsonPath("$.role", equalTo("USER")));
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
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(get("/api/users/{id}", testUser.getId()))
                .andExpect(status().isUnauthorized());
        }
    }

    // -------------------------- Create User Tests --------------------------
    @Nested
    @DisplayName("POST /api/users - Create user")
    class CreateUserTests {
        @Test
        @DisplayName("Should create user successfully without authentication")
        void shouldCreateUserSuccessfully() throws Exception {
            UserRequestDTO dto = buildCreateDTO("newuser@test.com", "password123", "USER");

            MvcResult result = mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.email", equalTo("newuser@test.com")))
                .andExpect(jsonPath("$.role", equalTo("USER")))
                .andReturn();

            String location = result.getResponse().getHeader("Location");
            assertThat(location).contains("/api/users/");
            assertThat(userRepository.findByEmail("newuser@test.com")).isPresent();
        }

        @Test
        @DisplayName("Should create ADMIN user")
        void shouldCreateAdminUser() throws Exception {
            UserRequestDTO dto = buildCreateDTO("newadmin@test.com", "adminpass", "ADMIN");

            mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role", equalTo("ADMIN")));
        }

        @Test
        @DisplayName("Should return 409 when email already taken")
        void shouldReturn409WhenEmailAlreadyTaken() throws Exception {
            UserRequestDTO dto = buildCreateDTO("user@test.com", "password123", "USER");

            mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 when email is blank")
        void shouldReturn400WhenEmailIsBlank() throws Exception {
            UserRequestDTO dto = buildCreateDTO("", "password123", "USER");

            mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when role is blank")
        void shouldReturn400WhenRoleIsBlank() throws Exception {
            UserRequestDTO dto = buildCreateDTO("valid@test.com", "password123", "");

            mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when role is invalid")
        void shouldReturn400WhenRoleIsInvalid() throws Exception {
            String invalidJson = "{\"email\":\"valid@test.com\",\"password\":\"pass123\",\"role\":\"SUPERADMIN\"}";

            mockMvc.perform(post("/api/users")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
        }
    }

    // -------------------------- Update User Tests --------------------------
    @Nested
    @DisplayName("PUT /api/users/{id} - Update user")
    class UpdateUserTests {

        @Test
        @DisplayName("Should update user successfully as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateUserSuccessfully() throws Exception {
            UserRequestDTO dto = buildUpdateDTO("updated@test.com", "USER", null);

            mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", equalTo("updated@test.com")));

            assertThat(userRepository.findByEmail("updated@test.com")).isPresent();
        }

        @Test
        @DisplayName("Should update role successfully")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateRoleSuccessfully() throws Exception {
            UserRequestDTO dto = buildUpdateDTO("user@test.com", "ADMIN", null);

            mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role", equalTo("ADMIN")));
        }

        @Test
        @DisplayName("Should return 409 when email already taken")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn409WhenEmailAlreadyTaken() throws Exception {
            UserRequestDTO dto = buildUpdateDTO("admin@test.com", "USER", null);

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
            UserRequestDTO dto = buildUpdateDTO("updated@test.com", "USER", null);

            mockMvc.perform(put("/api/users/{id}", testUser.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenNotFound() throws Exception {
            UserRequestDTO dto = buildUpdateDTO("updated@test.com", "USER", null);

            mockMvc.perform(put("/api/users/9999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound());
        }
    }

    // -------------------------- Delete User Tests --------------------------
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

            assertThat(userRepository.findById(testUser.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should return 404 when user not found")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404WhenUserNotFound() throws Exception {
            mockMvc.perform(delete("/api/users/{id}", 9999)
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
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            mockMvc.perform(delete("/api/users/{id}", testUser.getId())
                .with(csrf()))
                .andExpect(status().isUnauthorized());
        }
    }

    // -------------------------- Get User Guides Tests --------------------------
    @Nested
    @DisplayName("GET /api/users/{userId}/guides - Get user guides")
    class GetUserGuidesTests {
        @Test
        @DisplayName("Should return guides assigned to user as ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnUserGuides() throws Exception {
            Guide guide = new Guide("Paris Tour", "Beautiful Paris", 3,
                    Guide.Mobilite.A_PIED, Guide.Saison.ETE, Guide.PublicCible.FAMILLE);
            guide.addUser(testUser);
            guideRepository.save(guide);

            mockMvc.perform(get("/api/users/{userId}/guides", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].titre", equalTo("Paris Tour")));
        }

        @Test
        @DisplayName("Should return empty list when user has no guides")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnEmptyListWhenNoGuides() throws Exception {
            mockMvc.perform(get("/api/users/{userId}/guides", testUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
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
            mockMvc.perform(get("/api/users/{userId}/guides", testUser.getId()))
                .andExpect(status().isForbidden());
        }
    }
}