package com.journeo.repository;

import com.journeo.model.Guide;
import com.journeo.model.User;
import com.journeo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration Test Suite for Guide Repository
 *
 * Tests database persistence, querying, and relationship management using Spring Data JPA.
 * Uses an in-memory test database for isolated testing.
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Guide Repository Tests")
public class GuideRepositoryTest {

    @Autowired
    private GuideRepository guideRepository;

    @Autowired
    private UserRepository userRepository;

    private Guide testGuide1;
    private Guide testGuide2;
    private User testUser;

    @BeforeEach
    void setUp() {
        guideRepository.deleteAll();
        userRepository.deleteAll();

        testGuide1 = new Guide(
            "Paris Tour",
            "Beautiful Paris tour",
            3,
            Guide.Mobilite.A_PIED,
            Guide.Saison.ETE,
            Guide.PublicCible.FAMILLE
        );

        testGuide2 = new Guide(
            "Lyon Gastronomy",
            "Food tour in Lyon",
            2,
            Guide.Mobilite.VOITURE,
            Guide.Saison.PRINTEMPS,
            Guide.PublicCible.EN_GROUPE
        );

        testUser = new User("user@test.com", "password", User.Role.USER);
    }

    @Nested
    @DisplayName("save() - Persist guide")
    class SaveTests {

        @Test
        @DisplayName("Should save guide to database")
        void shouldSaveGuide() {
            Guide saved = guideRepository.save(testGuide1);

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTitre()).isEqualTo("Paris Tour");
        }

        @Test
        @DisplayName("Should generate ID on save")
        void shouldGenerateIdOnSave() {
            assertThat(testGuide1.getId()).isNull();

            Guide saved = guideRepository.save(testGuide1);

            assertThat(saved.getId()).isNotNull().isPositive();
        }

        @Test
        @DisplayName("Should persist all guide properties")
        void shouldPersistAllProperties() {
            Guide saved = guideRepository.save(testGuide1);

            assertThat(saved)
                .extracting("titre", "description", "jours", "mobilite", "saison", "pourQui")
                .containsExactly(
                    "Paris Tour",
                    "Beautiful Paris tour",
                    3,
                    Guide.Mobilite.A_PIED,
                    Guide.Saison.ETE,
                    Guide.PublicCible.FAMILLE
                );
        }

        @Test
        @DisplayName("Should save multiple guides")
        void shouldSaveMultipleGuides() {
            guideRepository.save(testGuide1);
            guideRepository.save(testGuide2);

            List<Guide> guides = guideRepository.findAll();

            assertThat(guides).hasSize(2);
        }

        @Test
        @DisplayName("Should accept null description")
        void shouldAcceptNullDescription() {
            testGuide1.setDescription(null);

            Guide saved = guideRepository.save(testGuide1);

            assertThat(saved.getDescription()).isNull();
        }
    }

    @Nested
    @DisplayName("findById() - Query guides by ID")
    class FindByIdTests {

        @Test
        @DisplayName("Should find guide by ID")
        void shouldFindGuideById() {
            Guide saved = guideRepository.save(testGuide1);

            Optional<Guide> found = guideRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTitre()).isEqualTo("Paris Tour");
        }

        @Test
        @DisplayName("Should return empty Optional when guide not found")
        void shouldReturnEmptyOptional() {
            Optional<Guide> found = guideRepository.findById(9999L);

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should retrieve correct guide among multiple")
        void shouldRetrieveCorrectGuide() {
            Guide saved1 = guideRepository.save(testGuide1);
            Guide saved2 = guideRepository.save(testGuide2);

            Optional<Guide> found = guideRepository.findById(saved1.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTitre()).isEqualTo("Paris Tour");
            assertThat(found.get().getTitre()).isNotEqualTo("Lyon Gastronomy");
        }
    }

    @Nested
    @DisplayName("findAll() - Retrieve all guides")
    class FindAllTests {

        @Test
        @DisplayName("Should retrieve all guides")
        void shouldRetrieveAllGuides() {
            guideRepository.save(testGuide1);
            guideRepository.save(testGuide2);

            List<Guide> guides = guideRepository.findAll();

            assertThat(guides).hasSize(2);
            assertThat(guides)
                .extracting(Guide::getTitre)
                .containsExactly("Paris Tour", "Lyon Gastronomy");
        }

        @Test
        @DisplayName("Should return empty list when no guides exist")
        void shouldReturnEmptyList() {
            List<Guide> guides = guideRepository.findAll();

            assertThat(guides).isEmpty();
        }

        @Test
        @DisplayName("Should preserve all properties in findAll")
        void shouldPreserveAllProperties() {
            guideRepository.save(testGuide1);

            List<Guide> guides = guideRepository.findAll();

            assertThat(guides.get(0))
                .extracting("titre", "mobilite", "saison", "pourQui")
                .containsExactly(
                    "Paris Tour",
                    Guide.Mobilite.A_PIED,
                    Guide.Saison.ETE,
                    Guide.PublicCible.FAMILLE
                );
        }
    }

    @Nested
    @DisplayName("delete() - Remove guides")
    class DeleteTests {

        @Test
        @DisplayName("Should delete guide from database")
        void shouldDeleteGuide() {
            Guide saved = guideRepository.save(testGuide1);

            guideRepository.delete(saved);

            assertThat(guideRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should delete only specified guide")
        void shouldDeleteOnlySpecifiedGuide() {
            Guide saved1 = guideRepository.save(testGuide1);
            Guide saved2 = guideRepository.save(testGuide2);

            guideRepository.delete(saved1);

            assertThat(guideRepository.findAll()).hasSize(1);
            assertThat(guideRepository.findById(saved2.getId())).isPresent();
        }

        @Test
        @DisplayName("Should delete by ID")
        void shouldDeleteById() {
            Guide saved = guideRepository.save(testGuide1);

            guideRepository.deleteById(saved.getId());

            assertThat(guideRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("update() - Modify existing guides")
    class UpdateTests {

        @Test
        @DisplayName("Should update guide properties")
        void shouldUpdateGuideProperties() {
            Guide saved = guideRepository.save(testGuide1);

            saved.setTitre("Updated Paris Tour");
            saved.setJours(5);
            guideRepository.save(saved);

            Optional<Guide> updated = guideRepository.findById(saved.getId());

            assertThat(updated.get())
                .extracting("titre", "jours")
                .containsExactly("Updated Paris Tour", 5);
        }

        @Test
        @DisplayName("Should update enum fields")
        void shouldUpdateEnumFields() {
            Guide saved = guideRepository.save(testGuide1);

            saved.setMobilite(Guide.Mobilite.VELO);
            saved.setSaison(Guide.Saison.AUTOMNE);
            saved.setPourQui(Guide.PublicCible.SEUL);
            guideRepository.save(saved);

            Optional<Guide> updated = guideRepository.findById(saved.getId());

            assertThat(updated.get())
                .extracting("mobilite", "saison", "pourQui")
                .containsExactly(
                    Guide.Mobilite.VELO,
                    Guide.Saison.AUTOMNE,
                    Guide.PublicCible.SEUL
                );
        }

        @Test
        @DisplayName("Should preserve other fields on update")
        void shouldPreserveOtherFields() {
            Guide saved = guideRepository.save(testGuide1);
            Long originalId = saved.getId();

            saved.setTitre("New Title");
            guideRepository.save(saved);

            Optional<Guide> updated = guideRepository.findById(originalId);

            assertThat(updated.get())
                .extracting("id", "description")
                .containsExactly(originalId, "Beautiful Paris tour");
        }
    }

    @Nested
    @DisplayName("Many-to-Many Relationships - Guide <-> User")
    class RelationshipTests {

        @Test
        @DisplayName("Should add user to guide")
        void shouldAddUserToGuide() {
            Guide savedGuide = guideRepository.save(testGuide1);
            User savedUser = userRepository.save(testUser);

            savedGuide.addUser(savedUser);
            savedGuide = guideRepository.save(savedGuide);

            Optional<Guide> retrieved = guideRepository.findById(savedGuide.getId());

            assertThat(retrieved.get().getUsers()).hasSize(1);
            assertThat(retrieved.get().getUsers()).contains(savedUser);
        }

        @Test
        @DisplayName("Should add multiple users to guide")
        void shouldAddMultipleUsersToGuide() {
            Guide savedGuide = guideRepository.save(testGuide1);
            User user1 = userRepository.save(new User("user1@test.com", "pass", User.Role.USER));
            User user2 = userRepository.save(new User("user2@test.com", "pass", User.Role.USER));

            savedGuide.addUser(user1);
            savedGuide.addUser(user2);
            guideRepository.save(savedGuide);

            Optional<Guide> retrieved = guideRepository.findById(savedGuide.getId());

            assertThat(retrieved.get().getUsers()).hasSize(2);
        }

        @Test
        @DisplayName("Should remove user from guide")
        void shouldRemoveUserFromGuide() {
            Guide savedGuide = guideRepository.save(testGuide1);
            User savedUser = userRepository.save(testUser);
            savedGuide.addUser(savedUser);
            guideRepository.save(savedGuide);

            savedGuide.removeUser(savedUser);
            guideRepository.save(savedGuide);

            Optional<Guide> retrieved = guideRepository.findById(savedGuide.getId());

            assertThat(retrieved.get().getUsers()).isEmpty();
        }

        @Test
        @DisplayName("Should maintain relationship on guide retrieval")
        void shouldMaintainRelationshipOnRetrieval() {
            Guide savedGuide = guideRepository.save(testGuide1);
            User savedUser = userRepository.save(testUser);
            savedGuide.addUser(savedUser);
            guideRepository.save(savedGuide);

            Optional<Guide> retrieved = guideRepository.findById(savedGuide.getId());

            assertThat(retrieved.get().getUsers())
                .extracting(User::getEmail)
                .contains("user@test.com");
        }
    }

    @Nested
    @DisplayName("Cascade Operations")
    class CascadeTests {

        @Test
        @DisplayName("Should preserve guide relationships on update")
        void shouldPreserveGuideRelationships() {
            Guide savedGuide = guideRepository.save(testGuide1);
            User savedUser = userRepository.save(testUser);
            savedGuide.addUser(savedUser);
            guideRepository.save(savedGuide);

            savedGuide.setTitre("Updated Title");
            guideRepository.save(savedGuide);

            Optional<Guide> retrieved = guideRepository.findById(savedGuide.getId());

            assertThat(retrieved.get().getUsers()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Data Integrity")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should update entity instead of creating duplicate on repeated save")
        void shouldUpdateExistingGuideOnRepeatedSave() {
            Guide saved1 = guideRepository.save(testGuide1);
            saved1.setTitre("Updated Title");
            Guide saved2 = guideRepository.save(saved1);

            // Both should reference the same entity
            assertThat(saved2.getId()).isEqualTo(saved1.getId());
            assertThat(saved2.getTitre()).isEqualTo("Updated Title");
            assertThat(guideRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("Should maintain data consistency across operations")
        void shouldMaintainDataConsistency() {
            Guide saved1 = guideRepository.save(testGuide1);
            Guide saved2 = guideRepository.save(testGuide2);

            guideRepository.deleteById(saved1.getId());

            List<Guide> remaining = guideRepository.findAll();

            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getId()).isEqualTo(saved2.getId());
        }
    }
}
