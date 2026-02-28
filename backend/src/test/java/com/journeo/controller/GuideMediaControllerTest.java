package com.journeo.controller;

import com.journeo.BackendApplication;
import com.journeo.config.MediaStorageService;
import com.journeo.model.Guide;
import com.journeo.model.GuideMedia;
import com.journeo.model.User;
import com.journeo.repository.GuideMediaRepository;
import com.journeo.repository.GuideRepository;
import com.journeo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(classes = BackendApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Guide Media Controller Tests")
public class GuideMediaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GuideMediaRepository mediaRepository;

    @MockBean
    private MediaStorageService mediaStorageService;

    private Guide testGuide;

    @BeforeEach
    void setUp() {
        mediaRepository.deleteAll();
        guideRepository.deleteAll();
        userRepository.deleteAll();

        userRepository.save(new User("admin@test.com", "password", "Admin", "User", User.Role.ADMIN));
        userRepository.save(new User("user@test.com",  "password", "Regular", "User", User.Role.USER));

        testGuide = guideRepository.save(new Guide(
            "Paris City Tour", "Beautiful tour", 3,
            Guide.Mobilite.A_PIED, Guide.Saison.ETE, Guide.PublicCible.FAMILLE
        ));

        // Default mock behaviour
        when(mediaStorageService.store(any())).thenReturn("uuid-test.jpg");
        when(mediaStorageService.detectFileType(any())).thenReturn(GuideMedia.FileType.IMAGE);
    }

    private MockMultipartFile fakeImage() {
        return new MockMultipartFile(
            "file", "photo.jpg", "image/jpeg", "fake-image-bytes".getBytes()
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/guides/{guideId}/media - Upload media")
    class UploadMediaTests {

        @Test
        @DisplayName("ADMIN can upload an image")
        @WithMockUser(username = "admin@test.com", roles = "ADMIN")
        void adminCanUpload() throws Exception {
            mockMvc.perform(multipart("/api/guides/{id}/media", testGuide.getId())
                    .file(fakeImage())
                    .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName", equalTo("uuid-test.jpg")))
                .andExpect(jsonPath("$.originalName", equalTo("photo.jpg")))
                .andExpect(jsonPath("$.fileType", equalTo("IMAGE")))
                .andExpect(jsonPath("$.guideId", equalTo(testGuide.getId().intValue())))
                .andExpect(jsonPath("$.url", containsString("/api/media/files/uuid-test.jpg")));

            verify(mediaStorageService).store(any());
        }

        @Test
        @DisplayName("USER cannot upload → 403")
        @WithMockUser(username = "user@test.com", roles = "USER")
        void userCannotUpload() throws Exception {
            mockMvc.perform(multipart("/api/guides/{id}/media", testGuide.getId())
                    .file(fakeImage())
                    .with(csrf()))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Unknown guide returns 404")
        @WithMockUser(roles = "ADMIN")
        void unknownGuideReturns404() throws Exception {
            mockMvc.perform(multipart("/api/guides/9999/media")
                    .file(fakeImage())
                    .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthenticated returns 401")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(multipart("/api/guides/{id}/media", testGuide.getId())
                    .file(fakeImage())
                    .with(csrf()))
                .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/guides/{guideId}/media - List media")
    class GetMediaTests {

        @Test
        @DisplayName("ADMIN can list media")
        @WithMockUser(roles = "ADMIN")
        void adminCanList() throws Exception {
            // Upload one via service to have a real DB entry
            GuideMedia media = new GuideMedia();
            media.setFileName("uuid-test.jpg");
            media.setOriginalName("photo.jpg");
            media.setFileType(GuideMedia.FileType.IMAGE);
            media.setContentType("image/jpeg");
            media.setSize(100L);
            media.setGuide(testGuide);
            mediaRepository.save(media);

            mockMvc.perform(get("/api/guides/{id}/media", testGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].fileName", equalTo("uuid-test.jpg")));
        }

        @Test
        @DisplayName("USER can list media")
        @WithMockUser(roles = "USER")
        void userCanList() throws Exception {
            mockMvc.perform(get("/api/guides/{id}/media", testGuide.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        }

        @Test
        @DisplayName("Unknown guide returns 404")
        @WithMockUser(roles = "ADMIN")
        void unknownGuideReturns404() throws Exception {
            mockMvc.perform(get("/api/guides/9999/media"))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthenticated returns 401")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(get("/api/guides/{id}/media", testGuide.getId()))
                .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/guides/{guideId}/media/{mediaId} - Delete media")
    class DeleteMediaTests {

        private GuideMedia savedMedia;

        @BeforeEach
        void saveMedia() {
            GuideMedia media = new GuideMedia();
            media.setFileName("uuid-test.jpg");
            media.setOriginalName("photo.jpg");
            media.setFileType(GuideMedia.FileType.IMAGE);
            media.setContentType(MediaType.IMAGE_JPEG_VALUE);
            media.setSize(100L);
            media.setGuide(testGuide);
            savedMedia = mediaRepository.save(media);
        }

        @Test
        @DisplayName("ADMIN can delete media")
        @WithMockUser(roles = "ADMIN")
        void adminCanDelete() throws Exception {
            doNothing().when(mediaStorageService).delete(any());

            mockMvc.perform(delete("/api/guides/{gId}/media/{mId}", testGuide.getId(), savedMedia.getId())
                    .with(csrf()))
                .andExpect(status().isOk());

            verify(mediaStorageService).delete("uuid-test.jpg");
        }

        @Test
        @DisplayName("USER cannot delete → 403")
        @WithMockUser(roles = "USER")
        void userCannotDelete() throws Exception {
            mockMvc.perform(delete("/api/guides/{gId}/media/{mId}", testGuide.getId(), savedMedia.getId())
                    .with(csrf()))
                .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Non-existent media returns 404")
        @WithMockUser(roles = "ADMIN")
        void nonExistentMediaReturns404() throws Exception {
            mockMvc.perform(delete("/api/guides/{gId}/media/9999", testGuide.getId())
                    .with(csrf()))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Unauthenticated returns 401")
        void unauthenticatedReturns401() throws Exception {
            mockMvc.perform(delete("/api/guides/{gId}/media/{mId}", testGuide.getId(), savedMedia.getId())
                    .with(csrf()))
                .andExpect(status().isUnauthorized());
        }
    }
}
