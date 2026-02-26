package com.journeo.repository;

import com.journeo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("User Repository Tests")
@SuppressWarnings("null")
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        testUser1 = new User("user1@test.com", "password1", User.Role.USER);
        testUser2 = new User("user2@test.com", "password2", User.Role.ADMIN);
    }

    @Nested
    @DisplayName("save() - Persist user")
    class SaveTests {

        @Test
        @DisplayName("Should save user to database")
        void shouldSaveUser() {
            User saved = Objects.requireNonNull(userRepository.save(testUser1));

            assertThat(saved).isNotNull();
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getEmail()).isEqualTo("user1@test.com");
        }

        @Test
        @DisplayName("Should generate ID on save")
        void shouldGenerateIdOnSave() {
            assertThat(testUser1.getId()).isNull();
            User saved = Objects.requireNonNull(userRepository.save(testUser1));

            assertThat(saved.getId()).isNotNull().isPositive();
        }

        @Test
        @DisplayName("Should persist all user properties")
        void shouldPersistAllProperties() {
            User saved = Objects.requireNonNull(userRepository.save(testUser1));

            assertThat(saved)
                .extracting("email", "role")
                .containsExactly("user1@test.com", User.Role.USER);
        }

        @Test
        @DisplayName("Should save multiple users")
        void shouldSaveMultipleUsers() {
            Objects.requireNonNull(userRepository.save(testUser1));
            Objects.requireNonNull(userRepository.save(testUser2));

            assertThat(userRepository.findAll()).hasSize(2);
        }

        @Test
        @DisplayName("Should throw exception on duplicate email")
        void shouldThrowExceptionOnDuplicateEmail() {
            Objects.requireNonNull(userRepository.save(testUser1));

            User duplicate = new User("user1@test.com", "otherpassword", User.Role.ADMIN);

            assertThatThrownBy(() -> {
                userRepository.save(duplicate);
                userRepository.flush();
            }).isInstanceOf(DataIntegrityViolationException.class);
        }
    }

    @Nested
    @DisplayName("findById() - Query users by ID")
    class FindByIdTests {

        @Test
        @DisplayName("Should find user by ID")
        void shouldFindUserById() {
            User saved = Objects.requireNonNull(userRepository.save(testUser1));

            Optional<User> found = userRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("user1@test.com");
        }

        @Test
        @DisplayName("Should return empty Optional when user not found")
        void shouldReturnEmptyOptional() {
            Optional<User> found = userRepository.findById(9999L);

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should retrieve correct user among multiple")
        void shouldRetrieveCorrectUser() {
            User saved1 = Objects.requireNonNull(userRepository.save(testUser1));
            Objects.requireNonNull(userRepository.save(testUser2));

            Optional<User> found = userRepository.findById(saved1.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("user1@test.com");
        }
    }

    @Nested
    @DisplayName("findByEmail() - Query users by email")
    class FindByEmailTests {

        @Test
        @DisplayName("Should find user by email")
        void shouldFindUserByEmail() {
            Objects.requireNonNull(userRepository.save(testUser1));

            Optional<User> found = userRepository.findByEmail("user1@test.com");

            assertThat(found).isPresent();
            assertThat(found.get().getEmail()).isEqualTo("user1@test.com");
            assertThat(found.get().getRole()).isEqualTo(User.Role.USER);
        }

        @Test
        @DisplayName("Should return empty Optional when email not found")
        void shouldReturnEmptyOptionalWhenNotFound() {
            Optional<User> found = userRepository.findByEmail("nobody@test.com");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("Should find correct user by email among multiple")
        void shouldFindCorrectUserByEmail() {
            Objects.requireNonNull(userRepository.save(testUser1));
            Objects.requireNonNull(userRepository.save(testUser2));

            Optional<User> found = userRepository.findByEmail("user2@test.com");

            assertThat(found).isPresent();
            assertThat(found.get().getRole()).isEqualTo(User.Role.ADMIN);
        }

        @Test
        @DisplayName("Should be case-sensitive on email lookup")
        void shouldBeCaseSensitiveOnEmailLookup() {
            Objects.requireNonNull(userRepository.save(testUser1));

            Optional<User> found = userRepository.findByEmail("USER1@TEST.COM");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll() - Retrieve all users")
    class FindAllTests {

        @Test
        @DisplayName("Should retrieve all users")
        void shouldRetrieveAllUsers() {
            Objects.requireNonNull(userRepository.save(testUser1));
            Objects.requireNonNull(userRepository.save(testUser2));

            List<User> users = userRepository.findAll();

            assertThat(users).hasSize(2);
            assertThat(users)
                .extracting(User::getEmail)
                .containsExactlyInAnyOrder("user1@test.com", "user2@test.com");
        }

        @Test
        @DisplayName("Should return empty list when no users exist")
        void shouldReturnEmptyList() {
            List<User> users = userRepository.findAll();

            assertThat(users).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete() - Remove users")
    class DeleteTests {

        @Test
        @DisplayName("Should delete user from database")
        void shouldDeleteUser() {
            User saved = Objects.requireNonNull(userRepository.save(testUser1));

            userRepository.delete(saved);

            assertThat(userRepository.findById(saved.getId())).isEmpty();
        }

        @Test
        @DisplayName("Should delete only specified user")
        void shouldDeleteOnlySpecifiedUser() {
            User saved1 = Objects.requireNonNull(userRepository.save(testUser1));
            User saved2 = Objects.requireNonNull(userRepository.save(testUser2));

            userRepository.delete(saved1);

            assertThat(userRepository.findAll()).hasSize(1);
            assertThat(userRepository.findById(saved2.getId())).isPresent();
        }

        @Test
        @DisplayName("Should delete by ID")
        void shouldDeleteById() {
            User saved = Objects.requireNonNull(userRepository.save(testUser1));

            userRepository.deleteById(saved.getId());

            assertThat(userRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("update() - Modify existing users")
    class UpdateTests {

        @Test
        @DisplayName("Should update user email")
        void shouldUpdateUserEmail() {
            User saved = Objects.requireNonNull(userRepository.save(testUser1));

            saved.setEmail("newemail@test.com");
            userRepository.save(saved);

            Optional<User> updated = userRepository.findById(saved.getId());

            assertThat(updated).isPresent();
            assertThat(updated.get().getEmail()).isEqualTo("newemail@test.com");
        }

        @Test
        @DisplayName("Should update user role")
        void shouldUpdateUserRole() {
            User saved = Objects.requireNonNull(userRepository.save(testUser1));

            saved.setRole(User.Role.ADMIN);
            userRepository.save(saved);

            Optional<User> updated = userRepository.findById(saved.getId());

            assertThat(updated).isPresent();
            assertThat(updated.get().getRole()).isEqualTo(User.Role.ADMIN);
        }

        @Test
        @DisplayName("Should update password")
        void shouldUpdatePassword() {
            User saved = Objects.requireNonNull(userRepository.save(testUser1));

            saved.setPassword("newhashedpassword");
            userRepository.save(saved);

            Optional<User> updated = userRepository.findById(saved.getId());

            assertThat(updated).isPresent();
            assertThat(updated.get().getPassword()).isEqualTo("newhashedpassword");
        }
    }

    @Nested
    @DisplayName("Data Integrity")
    class DataIntegrityTests {

        @Test
        @DisplayName("Should update entity instead of creating duplicate on repeated save")
        void shouldUpdateExistingUserOnRepeatedSave() {
            User saved1 = Objects.requireNonNull(userRepository.save(testUser1));
            saved1.setEmail("updated@test.com");
            User saved2 = Objects.requireNonNull(userRepository.save(saved1));

            assertThat(saved2.getId()).isEqualTo(saved1.getId());
            assertThat(saved2.getEmail()).isEqualTo("updated@test.com");
            assertThat(userRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("Should maintain data consistency across operations")
        void shouldMaintainDataConsistency() {
            User saved1 = Objects.requireNonNull(userRepository.save(testUser1));
            User saved2 = Objects.requireNonNull(userRepository.save(testUser2));

            userRepository.deleteById(saved1.getId());

            List<User> remaining = userRepository.findAll();

            assertThat(remaining).hasSize(1);
            assertThat(remaining.get(0).getId()).isEqualTo(saved2.getId());
        }
    }
}
