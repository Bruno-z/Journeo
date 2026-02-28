package com.journeo.service;

import com.journeo.dto.GuideRequestDTO;
import com.journeo.model.Guide;
import com.journeo.model.User;
import com.journeo.repository.GuideRepository;
import com.journeo.repository.UserRepository;
import com.journeo.service.GuideService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit Test Suite for Guide Service
 *
 * Tests the business logic layer of the Guide API, including CRUD operations,
 * user-guide relationships, and DTO conversions. Uses Mockito for repository mocking.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Guide Service Tests")
public class GuideServiceTest {

    @Mock
    private GuideRepository guideRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private GuideService guideService;

    private Guide testGuide;
    private User testUser;

    @BeforeEach
    void setUp() {
        testGuide = new Guide(
            "Test Guide",
            "Test Description",
            3,
            Guide.Mobilite.A_PIED,
            Guide.Saison.ETE,
            Guide.PublicCible.FAMILLE
        );

        testUser = new User("user@test.com", "password", "Test", "User", User.Role.USER);
    }

    @Nested
    @DisplayName("save() - Create guide")
    class SaveTests {

        @Test
        @DisplayName("Should save guide and return saved entity")
        void shouldSaveGuide() {
            when(guideRepository.save(any(Guide.class))).thenReturn(testGuide);

            Guide result = guideService.save(testGuide);

            assertThat(result).isNotNull();
            assertThat(result.getTitre()).isEqualTo("Test Guide");
            assertThat(result.getJours()).isEqualTo(3);
            verify(guideRepository, times(1)).save(testGuide);
        }

        @Test
        @DisplayName("Should handle null guide gracefully")
        void shouldHandleNullGuide() {
            when(guideRepository.save(null)).thenThrow(new IllegalArgumentException("Guide cannot be null"));

            assertThatThrownBy(() -> guideService.save(null))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Should preserve all guide properties on save")
        void shouldPreserveAllProperties() {
            when(guideRepository.save(any(Guide.class))).thenReturn(testGuide);

            Guide result = guideService.save(testGuide);

            assertThat(result)
                .extracting("titre", "description", "jours", "mobilite", "saison", "pourQui")
                .containsExactly(
                    "Test Guide",
                    "Test Description",
                    3,
                    Guide.Mobilite.A_PIED,
                    Guide.Saison.ETE,
                    Guide.PublicCible.FAMILLE
                );
        }
    }

    @Nested
    @DisplayName("findById() - Get guide by ID")
    class FindByIdTests {

        @Test
        @DisplayName("Should return guide when found")
        void shouldReturnGuideWhenFound() {
            when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));

            Guide result = guideService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getTitre()).isEqualTo("Test Guide");
            verify(guideRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Should return null when guide not found")
        void shouldReturnNullWhenNotFound() {
            when(guideRepository.findById(9999L)).thenReturn(Optional.empty());

            Guide result = guideService.findById(9999L);

            assertThat(result).isNull();
            verify(guideRepository, times(1)).findById(9999L);
        }

        @Test
        @DisplayName("Should return correct guide for different IDs")
        void shouldReturnCorrectGuideForDifferentIds() {
            Guide guide2 = new Guide("Another Guide", "Description", 2, Guide.Mobilite.VOITURE, Guide.Saison.HIVER, Guide.PublicCible.SEUL);

            when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));
            when(guideRepository.findById(2L)).thenReturn(Optional.of(guide2));

            assertThat(guideService.findById(1L).getTitre()).isEqualTo("Test Guide");
            assertThat(guideService.findById(2L).getTitre()).isEqualTo("Another Guide");
        }
    }

    @Nested
    @DisplayName("findAll() - Get all guides")
    class FindAllTests {

        @Test
        @DisplayName("Should return all guides")
        void shouldReturnAllGuides() {
            Guide guide2 = new Guide("Guide 2", "Desc 2", 2, Guide.Mobilite.VELO, Guide.Saison.PRINTEMPS, Guide.PublicCible.EN_GROUPE);
            List<Guide> guides = List.of(testGuide, guide2);

            when(guideRepository.findAll()).thenReturn(guides);

            List<Guide> result = guideService.findAll();

            assertThat(result).hasSize(2);
            assertThat(result).contains(testGuide, guide2);
            verify(guideRepository, times(1)).findAll();
        }

        @Test
        @DisplayName("Should return empty list when no guides exist")
        void shouldReturnEmptyList() {
            when(guideRepository.findAll()).thenReturn(List.of());

            List<Guide> result = guideService.findAll();

            assertThat(result).isEmpty();
            verify(guideRepository, times(1)).findAll();
        }
    }

    @Nested
    @DisplayName("update() - Update guide")
    class UpdateTests {

        @Test
        @DisplayName("Should update guide with new values")
        void shouldUpdateGuide() {
            GuideRequestDTO updateDTO = new GuideRequestDTO();
            updateDTO.setTitre("Updated Title");
            updateDTO.setDescription("Updated Description");
            updateDTO.setJours(5);
            updateDTO.setMobilite("VELO");
            updateDTO.setSaison("AUTOMNE");
            updateDTO.setPourQui("SEUL");

            when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));
            when(guideRepository.save(any(Guide.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Guide result = guideService.update(1L, updateDTO);

            assertThat(result)
                .extracting("titre", "jours")
                .containsExactly("Updated Title", 5);
            verify(guideRepository, times(1)).findById(1L);
            verify(guideRepository, times(1)).save(any(Guide.class));
        }

        @Test
        @DisplayName("Should return null when guide not found")
        void shouldReturnNullWhenNotFound() {
            GuideRequestDTO updateDTO = new GuideRequestDTO();

            when(guideRepository.findById(9999L)).thenReturn(Optional.empty());

            Guide result = guideService.update(9999L, updateDTO);

            assertThat(result).isNull();
            verify(guideRepository, never()).save(any(Guide.class));
        }

        @Test
        @DisplayName("Should update enum values correctly")
        void shouldUpdateEnumValuesCorrectly() {
            GuideRequestDTO updateDTO = new GuideRequestDTO();
            updateDTO.setTitre("Test Guide");
            updateDTO.setJours(1);
            updateDTO.setMobilite("MOTO");
            updateDTO.setSaison("HIVER");
            updateDTO.setPourQui("EN_GROUPE");
            updateDTO.setDescription("Updated");

            when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));
            when(guideRepository.save(any(Guide.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Guide result = guideService.update(1L, updateDTO);

            assertThat(result)
                .extracting("mobilite", "saison", "pourQui")
                .containsExactly(
                    Guide.Mobilite.MOTO,
                    Guide.Saison.HIVER,
                    Guide.PublicCible.EN_GROUPE
                );
        }
    }

    @Nested
    @DisplayName("delete() - Delete guide")
    class DeleteTests {

        @Test
        @DisplayName("Should delete guide")
        void shouldDeleteGuide() {
            guideService.delete(testGuide);

            verify(guideRepository, times(1)).delete(testGuide);
        }

        @Test
        @DisplayName("Should delete specific guide instance")
        void shouldDeleteSpecificGuide() {
            Guide guide1 = new Guide("Guide 1", "Desc", 1, Guide.Mobilite.A_PIED, Guide.Saison.ETE, Guide.PublicCible.FAMILLE);
            Guide guide2 = new Guide("Guide 2", "Desc", 1, Guide.Mobilite.VOITURE, Guide.Saison.PRINTEMPS, Guide.PublicCible.SEUL);

            guideService.delete(guide1);

            verify(guideRepository, times(1)).delete(guide1);
            verify(guideRepository, never()).delete(guide2);
        }
    }

    @Nested
    @DisplayName("addUserToGuide() - Add user to guide")
    class AddUserToGuideTests {

        @Test
        @DisplayName("Should add user to guide successfully")
        void shouldAddUserToGuideSuccessfully() {
            when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(guideRepository.save(any(Guide.class))).thenReturn(testGuide);

            Guide result = guideService.addUserToGuide(1L, 1L);

            assertThat(result).isNotNull();
            assertThat(result.getUsers()).contains(testUser);
            verify(guideRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).findById(1L);
            verify(guideRepository, times(1)).save(testGuide);
        }

        @Test
        @DisplayName("Should return null when guide not found")
        void shouldReturnNullWhenGuideNotFound() {
            when(guideRepository.findById(9999L)).thenReturn(Optional.empty());

            Guide result = guideService.addUserToGuide(9999L, 1L);

            assertThat(result).isNull();
            verify(guideRepository, never()).save(any(Guide.class));
        }

        @Test
        @DisplayName("Should return null when user not found")
        void shouldReturnNullWhenUserNotFound() {
            when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));
            when(userRepository.findById(9999L)).thenReturn(Optional.empty());

            Guide result = guideService.addUserToGuide(1L, 9999L);

            assertThat(result).isNull();
            verify(guideRepository, never()).save(any(Guide.class));
        }

        @Test
        @DisplayName("Should add multiple users to same guide")
        void shouldAddMultipleUsersToSameGuide() {
            User user2 = new User("user2@test.com", "password", "User", "Two", User.Role.USER);
            user2.setId(2L);

            when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(guideRepository.save(any(Guide.class))).thenReturn(testGuide);

            guideService.addUserToGuide(1L, 1L);
            testGuide.addUser(testUser);

            when(userRepository.findById(2L)).thenReturn(Optional.of(user2));
            guideService.addUserToGuide(1L, 2L);
            testGuide.addUser(user2);

            assertThat(testGuide.getUsers()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("removeUserFromGuide() - Remove user from guide")
    class RemoveUserFromGuideTests {

        @BeforeEach
        void setupUserGuideRelationship() {
            testGuide.addUser(testUser);
        }

        @Test
        @DisplayName("Should remove user from guide successfully")
        void shouldRemoveUserFromGuideSuccessfully() {
            when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));
            when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
            when(guideRepository.save(any(Guide.class))).thenReturn(testGuide);

            Guide result = guideService.removeUserFromGuide(1L, 1L);

            assertThat(result).isNotNull();
            verify(guideRepository, times(1)).findById(1L);
            verify(userRepository, times(1)).findById(1L);
            verify(guideRepository, times(1)).save(testGuide);
        }

        @Test
        @DisplayName("Should return null when guide not found")
        void shouldReturnNullWhenGuideNotFound() {
            when(guideRepository.findById(9999L)).thenReturn(Optional.empty());

            Guide result = guideService.removeUserFromGuide(9999L, 1L);

            assertThat(result).isNull();
            verify(guideRepository, never()).save(any(Guide.class));
        }

        @Test
        @DisplayName("Should return null when user not found")
        void shouldReturnNullWhenUserNotFound() {
            when(guideRepository.findById(1L)).thenReturn(Optional.of(testGuide));
            when(userRepository.findById(9999L)).thenReturn(Optional.empty());

            Guide result = guideService.removeUserFromGuide(1L, 9999L);

            assertThat(result).isNull();
            verify(guideRepository, never()).save(any(Guide.class));
        }
    }

    @Nested
    @DisplayName("DTO Conversion Methods")
    class DTOConversionTests {

        @Test
        @DisplayName("Should convert guide to DTO")
        void shouldConvertGuideToDTO() {
            var dto = guideService.toDTO(testGuide);

            assertThat(dto).isNotNull();
            assertThat(dto.getTitre()).isEqualTo("Test Guide");
            assertThat(dto.getDescription()).isEqualTo("Test Description");
        }

        @Test
        @DisplayName("Should convert list of guides to DTOs")
        void shouldConvertListOfGuidesToDTOs() {
            Guide guide2 = new Guide("Guide 2", "Desc 2", 2, Guide.Mobilite.VELO, Guide.Saison.PRINTEMPS, Guide.PublicCible.EN_GROUPE);
            List<Guide> guides = List.of(testGuide, guide2);

            var dtos = guideService.toDTOList(guides);

            assertThat(dtos).hasSize(2);
            assertThat(dtos)
                .extracting("titre")
                .containsExactly("Test Guide", "Guide 2");
        }
    }
}
