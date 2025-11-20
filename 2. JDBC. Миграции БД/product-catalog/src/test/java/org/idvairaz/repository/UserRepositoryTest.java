package org.idvairaz.repository;

import org.idvairaz.BaseRepositoryTest;
import org.idvairaz.model.User;
import org.idvairaz.repository.impl.PostgresUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для PostgresUserRepository с использованием Testcontainers.
 *
 * @author idvavraz
 * @version 1.0
 */
class UserRepositoryTest extends BaseRepositoryTest {

    private PostgresUserRepository userRepository;

    @BeforeEach
    void setUp() {

        userRepository = new PostgresUserRepository();
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM users");
            System.out.println("Таблица users очищена перед тестом");
        } catch (SQLException e) {
            if (e.getMessage().contains("relation \"users\" does not exist")) {
                System.out.println("Таблица 'users' еще не существует, пропускаем очистку");
                return;
            }
            throw new RuntimeException("Ошибка при очистке таблицы users", e);
        }
    }

    @Test
    void shouldSaveAndFindUserByUsername() {
        User user = User.builder()
                .username("testuser")
                .password("testpass")
                .role(User.UserRole.USER)
                .isLoggedIn(false)
                .build();

        userRepository.save(user);
        Optional<User> foundUser = userRepository.findByUsername("testuser");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isNotNull();
        assertThat(foundUser.get().getUsername()).isEqualTo("testuser");
        assertThat(foundUser.get().getRole()).isEqualTo(User.UserRole.USER);
    }

    @Test
    void shouldCheckIfUserExists() {
        User user = User.builder()
                .username("existinguser")
                .password("password")
                .role(User.UserRole.USER)
                .isLoggedIn(false)
                .build();
        userRepository.save(user);

        boolean exists = userRepository.existsByUsername("existinguser");
        boolean notExists = userRepository.existsByUsername("nonexistentuser");

        assertThat(exists).isTrue();
        assertThat(notExists).isFalse();
    }

    @Test
    void shouldUpdateUser() {
        User user = User.builder()
                .username("updateuser")
                .password("oldpass")
                .role(User.UserRole.USER)
                .isLoggedIn(false)
                .build();
        User savedUser = userRepository.save(user);

        Long originalId = savedUser.getId();

        savedUser.setPassword("newpass");
        savedUser.setRole(User.UserRole.MANAGER);
        userRepository.save(savedUser);

        Optional<User> foundUser = userRepository.findByUsername("updateuser");
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getId()).isEqualTo(originalId);
        assertThat(foundUser.get().getPassword()).isEqualTo("newpass");
        assertThat(foundUser.get().getRole()).isEqualTo(User.UserRole.MANAGER);
    }

    @Test
    void shouldDeleteUser() {
        User user = User.builder()
                .username("deleteme")
                .password("password")
                .role(User.UserRole.USER)
                .isLoggedIn(false)
                .build();
        userRepository.save(user);

        userRepository.deleteByUsername("deleteme");

        Optional<User> foundUser = userRepository.findByUsername("deleteme");
        assertThat(foundUser).isEmpty();
    }

    @Test
    void shouldFindAllUsers() {
        User user1 = User.builder()
                .username("user1")
                .password("pass1")
                .role(User.UserRole.USER)
                .isLoggedIn(false)
                .build();

        User user2 = User.builder()
                .username("user2")
                .password("pass2")
                .role(User.UserRole.MANAGER)
                .isLoggedIn(true)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        var users = userRepository.findAll();

        assertThat(users).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void shouldGenerateUniqueIdsForUsers() {
        User user1 = User.builder()
                .username("user1")
                .password("pass1")
                .role(User.UserRole.USER)
                .build();

        User user2 = User.builder()
                .username("user2")
                .password("pass2")
                .role(User.UserRole.USER)
                .build();

        User saved1 = userRepository.save(user1);
        User saved2 = userRepository.save(user2);

        assertThat(saved1.getId()).isNotNull();
        assertThat(saved2.getId()).isNotNull();
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
    }
}
