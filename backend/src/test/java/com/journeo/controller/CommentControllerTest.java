package com.journeo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.journeo.BackendApplication;
import com.journeo.dto.CommentRequestDTO;
import com.journeo.model.Comment;
import com.journeo.model.Guide;
import com.journeo.model.User;
import com.journeo.repository.CommentRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Comment Controller Tests")
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private UserRepository userRepository;

    private Guide testGuide;
    private User adminUser;
    private User regularUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        guideRepository.deleteAll();
        userRepository.deleteAll();

        adminUser  = userRepository.save(new User("admin@test.com",  "password", "Admin", "User", User.Role.ADMIN));
        regularUser = userRepository.save(new User("user@test.com",  "password", "Regular", "User", User.Role.USER));
        otherUser   = userRepository.save(new User("other@test.com", "password", "Other", "User", User.Role.USER));

        testGuide = guideRepository.save(new Guide(
            "Paris City Tour", "Beautiful tour", 3,
            Guide.Mobilite.A_PIED, Guide.Saison.ETE, Guide.PublicCible.FAMILLE
        ));
    }

    private CommentRequestDTO buildRequest(String content, Integer rating) {
        CommentRequestDTO dto = new CommentRequestDTO();
        dto.setContent(content);
        dto.setRating(rating);
        return dto;
    }

    private Comment saveComment(User author, int rating) {
        Comment c = new Comment();
        c.setContent("Great guide!");
        c.setRating(rating);
        c.setGuide(testGuide);
        c.setAuthor(author);
        return commentRepository.save(c);
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/guides/{guideId}/comments - Add comment")
    class AddCommentTests {

        @Test
        @DisplayName("ADMIN can add a comment")
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        void adminCanAddComment() throws Exception {
            mockMvc.perform(post("/api/guides/{id}/comments", testGuide.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildRequest("Super guide!", 5))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content", equalTo("Super guide!")))
                .andExpect(jsonPath("$.rating", equalTo(5)))
                .andExpect(jsonPath("$.authorEmail", equalTo("admin@test.com")))
                .andExpect(jsonPath("$.guideId", equalTo(testGuide.getId().intValue())));
        }

        @Test
        @DisplayName("USER can add a comment")
        @WithMockUser(username = "user@test.com", roles = "USER")
        void userCanAddComment() throws Exception {
            mockMvc.perform(post("/api/guides/{id}/comments", testGuide.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildRequest("Très bon!", 4))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.rating", equalTo(4)));
        }

        @Test
        @DisplayName("Rating 0 returns 400")
        @WithMockUser(username = "user@test.com", roles = "USER")
        void ratingZeroReturns400() throws Exception {
            mockMvc.perform(post("/api/guides/{id}/comments", testGuide.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildRequest("Bad rating", 0))))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Rating 6 returns 400")
        @WithMockUser(username = "user@test.com", roles = "USER")
        void ratingSixReturns400() throws Exception {
            mockMvc.perform(post("/api/guides/{id}/comments", testGuide.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildRequest("Too high", 6))))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Empty content returns 400")
        @WithMockUser(username = "user@test.com", roles = "USER")
        void emptyContentReturns400() throws Exception {
            mockMvc.perform(post("/api/guides/{id}/comments", testGuide.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildRequest("", 3))))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Missing rating returns 400")
        @WithMockUser(username = "user@test.com", roles = "USER")
        void missingRatingReturns400() throws Exception {
            mockMvc.perform(post("/api/guides/{id}/comments", testGuide.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildRequest("No rating", null))))
                .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Unknown guide returns 404")
        @WithMockUser(username = "user@test.com", roles = "USER")
        void unknownGuideReturns404() throws Exception {
            mockMvc.perform(post("/api/guides/9999/comments")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildRequest("?", 3))))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthenticated returns 401")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(post("/api/guides/{id}/comments", testGuide.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(buildRequest("Hello", 3))))
                .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/guides/{guideId}/comments - List comments")
    class GetCommentsTests {

        @Test
        @DisplayName("ADMIN sees all comments ordered by date desc")
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        void adminSeesAllComments() throws Exception {
            saveComment(regularUser, 3);
            saveComment(adminUser, 5);

            mockMvc.perform(get("/api/guides/{id}/comments", testGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        }

        @Test
        @DisplayName("USER can list comments")
        @WithMockUser(username = "user@test.com", roles = "USER")
        void userCanListComments() throws Exception {
            saveComment(regularUser, 4);

            mockMvc.perform(get("/api/guides/{id}/comments", testGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].content", equalTo("Great guide!")));
        }

        @Test
        @DisplayName("Empty list when no comments")
        @WithMockUser(roles = "ADMIN")
        void emptyListWhenNoComments() throws Exception {
            mockMvc.perform(get("/api/guides/{id}/comments", testGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Unknown guide returns 404")
        @WithMockUser(roles = "ADMIN")
        void unknownGuideReturns404() throws Exception {
            mockMvc.perform(get("/api/guides/9999/comments"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthenticated returns 401")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(get("/api/guides/{id}/comments", testGuide.getId()))
                .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/guides/{guideId}/comments/{commentId} - Delete comment")
    class DeleteCommentTests {

        @Test
        @DisplayName("ADMIN can delete any comment")
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        void adminCanDeleteAnyComment() throws Exception {
            Comment comment = saveComment(regularUser, 3);

            mockMvc.perform(delete("/api/guides/{gId}/comments/{cId}", testGuide.getId(), comment.getId())
                    .with(csrf()))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("USER can delete their own comment")
        @WithMockUser(username = "user@test.com", roles = "USER")
        void userCanDeleteOwnComment() throws Exception {
            Comment comment = saveComment(regularUser, 4);

            mockMvc.perform(delete("/api/guides/{gId}/comments/{cId}", testGuide.getId(), comment.getId())
                    .with(csrf()))
                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("USER cannot delete someone else's comment → 403")
        @WithMockUser(username = "user@test.com", roles = "USER")
        void userCannotDeleteOthersComment() throws Exception {
            Comment comment = saveComment(otherUser, 5);

            mockMvc.perform(delete("/api/guides/{gId}/comments/{cId}", testGuide.getId(), comment.getId())
                    .with(csrf()))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Non-existent comment returns 404")
        @WithMockUser(roles = "ADMIN")
        void nonExistentCommentReturns404() throws Exception {
            mockMvc.perform(delete("/api/guides/{gId}/comments/9999", testGuide.getId())
                    .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthenticated returns 401")
        void unauthenticatedReturns401() throws Exception {
            Comment comment = saveComment(regularUser, 3);

            mockMvc.perform(delete("/api/guides/{gId}/comments/{cId}", testGuide.getId(), comment.getId())
                    .with(csrf()))
                .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/guides/{id} - averageRating in guide response")
    class AverageRatingTests {

        @Test
        @DisplayName("averageRating is null when no comments")
        @WithMockUser(roles = "ADMIN")
        void averageRatingNullWhenNoComments() throws Exception {
            mockMvc.perform(get("/api/guides/{id}", testGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating").doesNotExist());
        }

        @Test
        @DisplayName("averageRating is computed correctly")
        @WithMockUser(roles = "ADMIN")
        void averageRatingComputedCorrectly() throws Exception {
            saveComment(regularUser, 4);
            saveComment(adminUser, 2);

            mockMvc.perform(get("/api/guides/{id}", testGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.averageRating", equalTo(3.0)));
        }
    }
}
