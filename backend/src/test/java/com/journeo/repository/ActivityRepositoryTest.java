package com.journeo.repository;

import com.journeo.model.Activity;
import com.journeo.model.Guide;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Activity Repository Tests")
@SuppressWarnings("null")
public class ActivityRepositoryTest {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private GuideRepository guideRepository;

    private Guide testGuide;
    private Activity testActivity1;
    private Activity testActivity2;

    @BeforeEach
    void setUp() {
        activityRepository.deleteAll();
        guideRepository.deleteAll();

        testGuide = new Guide(
            "Paris Tour",
            "Beautiful Paris tour",
            3,
            Guide.Mobilite.A_PIED,
            Guide.Saison.ETE,
            Guide.PublicCible.FAMILLE
        );
        testGuide = guideRepository.save(testGuide);

        testActivity1 = new Activity();
        testActivity1.setTitre("Visite du Louvre");
        testActivity1.setDescription("Le plus grand musée du monde");
        testActivity1.setType(Activity.Type.MUSEE);
        testActivity1.setDuree(180);
        testActivity1.setOrdre(1);
        testActivity1.setJour(1);

        testActivity2 = new Activity();
        testActivity2.setTitre("Tour Eiffel");
        testActivity2.setDescription("Monument emblématique de Paris");
        testActivity2.setType(Activity.Type.ACTIVITE);
        testActivity2.setDuree(120);
        testActivity2.setOrdre(2);
        testActivity2.setJour(1);
    }

    @Nested
    @DisplayName("save() - Persist activity")
    class SaveTests {

        @Test
        @DisplayName("Should save activity to database")
        void shouldSaveActivity() {
            testGuide.addActivity(testActivity1);
            Activity saved = Objects.requireNonNull(activityRepository.save(testActivity1));

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getTitre()).isEqualTo("Visite du Louvre");
        }

        @Test
        @DisplayName("Should generate ID on save")
        void shouldGenerateIdOnSave() {
            testGuide.addActivity(testActivity1);
            Activity saved = Objects.requireNonNull(activityRepository.save(testActivity1));

            assertThat(saved.getId()).isNotNull().isPositive();
        }

        @Test
        @DisplayName("Should persist all activity properties")
        void shouldPersistAllProperties() {
            testActivity1.setAdresse("Rue de Rivoli, Paris");
            testActivity1.setTelephone("+33 1 40 20 50 50");
            testActivity1.setSiteInternet("https://www.louvre.fr");
            testActivity1.setHeureDebut("09:00");
            testGuide.addActivity(testActivity1);
            Activity saved = Objects.requireNonNull(activityRepository.save(testActivity1));

            assertThat(saved)
                .extracting("titre", "type", "duree", "ordre", "jour", "adresse", "heureDebut")
                .containsExactly("Visite du Louvre", Activity.Type.MUSEE, 180, 1, 1, "Rue de Rivoli, Paris", "09:00");
        }

        @Test
        @DisplayName("Should save multiple activities")
        void shouldSaveMultipleActivities() {
            testGuide.addActivity(testActivity1);
            testGuide.addActivity(testActivity2);
            activityRepository.save(testActivity1);
            activityRepository.save(testActivity2);

            assertThat(activityRepository.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("Should accept null optional fields")
        void shouldAcceptNullOptionalFields() {
            testGuide.addActivity(testActivity1);
            Activity saved = Objects.requireNonNull(activityRepository.save(testActivity1));

            assertThat(saved.getAdresse()).isNull();
            assertThat(saved.getTelephone()).isNull();
            assertThat(saved.getSiteInternet()).isNull();
        }
    }

    @Nested
    @DisplayName("findById() - Query activities by ID")
    class FindByIdTests {

        @Test
        @DisplayName("Should find activity by ID")
        void shouldFindActivityById() {
            testGuide.addActivity(testActivity1);
            Activity saved = Objects.requireNonNull(activityRepository.save(testActivity1));

            Optional<Activity> found = activityRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTitre()).isEqualTo("Visite du Louvre");
        }

        @Test
        @DisplayName("Should return empty Optional when activity not found")
        void shouldReturnEmptyOptional() {
            Optional<Activity> found = activityRepository.findById(9999L);

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should retrieve correct activity among multiple")
        void shouldRetrieveCorrectActivity() {
            testGuide.addActivity(testActivity1);
            testGuide.addActivity(testActivity2);
            Activity saved1 = Objects.requireNonNull(activityRepository.save(testActivity1));
            Objects.requireNonNull(activityRepository.save(testActivity2));

            Optional<Activity> found = activityRepository.findById(saved1.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getTitre()).isEqualTo("Visite du Louvre");
        }
    }

    @Nested
    @DisplayName("findByGuide() - Query activities by guide")
    class FindByGuideTests {

        @Test
        @DisplayName("Should find activities by guide")
        void shouldFindActivitiesByGuide() {
            testGuide.addActivity(testActivity1);
            testGuide.addActivity(testActivity2);
            Objects.requireNonNull(activityRepository.save(testActivity1));
            Objects.requireNonNull(activityRepository.save(testActivity2));

            List<Activity> activities = activityRepository.findByGuide(testGuide);

            assertThat(activities).hasSize(2);
            assertThat(activities)
                .extracting(Activity::getTitre)
                .containsExactlyInAnyOrder("Visite du Louvre", "Tour Eiffel");
        }

        @Test
        @DisplayName("Should return empty list when guide has no activities")
        void shouldReturnEmptyListForGuideWithNoActivities() {
            List<Activity> activities = activityRepository.findByGuide(testGuide);

            assertThat(activities).isEmpty();
        }

        @Test
        @DisplayName("Should not return activities from another guide")
        void shouldNotReturnActivitiesFromAnotherGuide() {
            Guide otherGuide = guideRepository.save(new Guide(
                "Lyon Tour", "Lyon tour", 2,
                Guide.Mobilite.VOITURE, Guide.Saison.PRINTEMPS, Guide.PublicCible.EN_GROUPE
            ));

            testGuide.addActivity(testActivity1);
            Objects.requireNonNull(activityRepository.save(testActivity1));

            Activity otherActivity = new Activity();
            otherActivity.setTitre("Basilique de Fourvière");
            otherActivity.setType(Activity.Type.MUSEE);
            otherActivity.setDuree(60);
            otherActivity.setOrdre(1);
            otherActivity.setJour(1);
            otherGuide.addActivity(otherActivity);
            Objects.requireNonNull(activityRepository.save(otherActivity));

            List<Activity> activities = activityRepository.findByGuide(testGuide);

            assertThat(activities).hasSize(1);
            assertThat(activities.get(0).getTitre()).isEqualTo("Visite du Louvre");
        }
    }

    @Nested
    @DisplayName("findByGuideOrderByOrdreAsc() - Query activities ordered by ordre")
    class FindByGuideOrderByOrdreAscTests {

        @Test
        @DisplayName("Should return activities ordered by ordre")
        void shouldReturnActivitiesOrderedByOrdre() {
            testActivity1.setOrdre(2);
            testActivity2.setOrdre(1);
            testGuide.addActivity(testActivity1);
            testGuide.addActivity(testActivity2);
            Objects.requireNonNull(activityRepository.save(testActivity1));
            Objects.requireNonNull(activityRepository.save(testActivity2));

            List<Activity> activities = activityRepository.findByGuideOrderByOrdreAsc(testGuide);

            assertThat(activities).hasSize(2);
            assertThat(activities.get(0).getTitre()).isEqualTo("Tour Eiffel");
            assertThat(activities.get(1).getTitre()).isEqualTo("Visite du Louvre");
        }
    }

    @Nested
    @DisplayName("findByGuideAndOrdre() - Query activity by guide and ordre")
    class FindByGuideAndOrdreTests {

        @Test
        @DisplayName("Should find activity by guide and ordre")
        void shouldFindActivityByGuideAndOrdre() {
            testGuide.addActivity(testActivity1);
            Objects.requireNonNull(activityRepository.save(testActivity1));

            Activity found = activityRepository.findByGuideAndOrdre(testGuide, 1);

            assertThat(found).isNotNull();
            assertThat(found.getTitre()).isEqualTo("Visite du Louvre");
        }

        @Test
        @DisplayName("Should return null when no activity with that ordre")
        void shouldReturnNullWhenNotFound() {
            Activity found = activityRepository.findByGuideAndOrdre(testGuide, 99);

            assertThat(found).isNull();
        }
    }

    @Nested
    @DisplayName("deleteByGuide() - Delete activities by guide")
    class DeleteByGuideTests {

        @Test
        @DisplayName("Should delete all activities of a guide")
        void shouldDeleteAllActivitiesOfGuide() {
            testGuide.addActivity(testActivity1);
            testGuide.addActivity(testActivity2);
            Objects.requireNonNull(activityRepository.save(testActivity1));
            Objects.requireNonNull(activityRepository.save(testActivity2));

            activityRepository.deleteByGuide(testGuide);

            assertThat(activityRepository.findByGuide(testGuide)).isEmpty();
        }

        @Test
        @DisplayName("Should not delete activities from another guide")
        void shouldNotDeleteActivitiesFromAnotherGuide() {
            Guide otherGuide = guideRepository.save(new Guide(
                "Lyon Tour", "Lyon tour", 2,
                Guide.Mobilite.VOITURE, Guide.Saison.PRINTEMPS, Guide.PublicCible.EN_GROUPE
            ));

            testGuide.addActivity(testActivity1);
            Objects.requireNonNull(activityRepository.save(testActivity1));

            Activity otherActivity = new Activity();
            otherActivity.setTitre("Basilique de Fourvière");
            otherActivity.setType(Activity.Type.MUSEE);
            otherActivity.setDuree(60);
            otherActivity.setOrdre(1);
            otherActivity.setJour(1);
            otherGuide.addActivity(otherActivity);
            Objects.requireNonNull(activityRepository.save(otherActivity));

            activityRepository.deleteByGuide(testGuide);

            assertThat(activityRepository.findByGuide(otherGuide)).hasSize(1);
        }
    }

    @Nested
    @DisplayName("delete() - Remove activities")
    class DeleteTests {

        @Test
        @DisplayName("Should delete activity from database")
        void shouldDeleteActivity() {
            testGuide.addActivity(testActivity1);
            Activity saved = Objects.requireNonNull(activityRepository.save(testActivity1));

            activityRepository.delete(saved);

            assertThat(activityRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should delete only specified activity")
        void shouldDeleteOnlySpecifiedActivity() {
            testGuide.addActivity(testActivity1);
            testGuide.addActivity(testActivity2);
            Activity saved1 = Objects.requireNonNull(activityRepository.save(testActivity1));
            Activity saved2 = Objects.requireNonNull(activityRepository.save(testActivity2));

            activityRepository.delete(saved1);

            assertThat(activityRepository.findAll()).hasSize(1);
            assertThat(activityRepository.findById(saved2.getId())).isPresent();
        }
    }

    @Nested
    @DisplayName("update() - Modify existing activities")
    class UpdateTests {

        @Test
        @DisplayName("Should update activity properties")
        void shouldUpdateActivityProperties() {
            testGuide.addActivity(testActivity1);
            Activity saved = Objects.requireNonNull(activityRepository.save(testActivity1));

            saved.setTitre("Musée d'Orsay");
            saved.setDuree(150);
            activityRepository.save(saved);

            Optional<Activity> updated = activityRepository.findById(saved.getId());

            assertThat(updated).isPresent();
            assertThat(updated.get())
                .extracting("titre", "duree")
                .containsExactly("Musée d'Orsay", 150);
        }

        @Test
        @DisplayName("Should update type enum")
        void shouldUpdateTypeEnum() {
            testGuide.addActivity(testActivity1);
            Activity saved = Objects.requireNonNull(activityRepository.save(testActivity1));

            saved.setType(Activity.Type.CHATEAU);
            activityRepository.save(saved);

            Optional<Activity> updated = activityRepository.findById(saved.getId());

            assertThat(updated).isPresent();
            assertThat(updated.get().getType()).isEqualTo(Activity.Type.CHATEAU);
        }
    }
}
