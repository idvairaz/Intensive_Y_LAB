package org.idvairaz.service;

import org.idvairaz.io.UserDataManager;
import org.idvairaz.model.User;
import org.idvairaz.model.User.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Тесты для сервиса аутентификации и авторизации.
 * Проверяет функциональность входа, регистрации, управления правами доступа.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private AuthService authService;
    private UserDataManager userDataManager;
    private List<User> mockUsers;

    @BeforeEach
    void setUp() {
        MockedConstruction<UserDataManager> mocked = mockConstruction(UserDataManager.class,
                (mock, context) -> {
                    userDataManager = mock;
                    mockUsers = new ArrayList<>();
                    when(mock.loadUsers()).thenReturn(mockUsers);
                });

        authService = new AuthService();

        mocked.close();
    }

    /**
     * Тестирует успешный вход пользователя с правильными учетными данными.
     * Проверяет установку статуса аутентификации и текущего пользователя.
     */
    @Test
    void login_WithValidCredentials_ShouldAuthenticateUser() {
        User testUser = new User("testuser", "password123", UserRole.USER);
        authService.register("testuser", "password123", UserRole.USER);

        boolean result = authService.login("testuser", "password123");

        assertThat(result).isTrue();
        assertThat(authService.isAuthenticated()).isTrue();
        assertThat(authService.getCurrentUsername()).isEqualTo("testuser");
        assertThat(authService.getCurrentUserRole()).isEqualTo(UserRole.USER);

        Optional<User> currentUser = authService.getCurrentUser();
        assertThat(currentUser).isPresent();
        assertThat(currentUser.get().getUsername()).isEqualTo("testuser");
        assertThat(currentUser.get().isLoggedIn()).isTrue();
    }

    /**
     * Тестирует неудачный вход с неверным паролем.
     * Проверяет что пользователь не аутентифицируется.
     */
    @Test
    void login_WithInvalidPassword_ShouldReturnFalse() {
        authService.register("testuser", "password123", UserRole.USER);

        boolean result = authService.login("testuser", "wrongpassword");

        assertThat(result).isFalse();
        assertThat(authService.isAuthenticated()).isFalse();
        assertThat(authService.getCurrentUser()).isEmpty();
    }

    /**
     * Тестирует неудачный вход с несуществующим пользователем.
     * Проверяет что система не аутентифицирует неизвестного пользователя.
     */
    @Test
    void login_WithNonExistentUser_ShouldReturnFalse() {
        boolean result = authService.login("unknown", "password");

        assertThat(result).isFalse();
        assertThat(authService.isAuthenticated()).isFalse();
    }

    /**
     * Тестирует успешную регистрацию нового пользователя.
     * Проверяет создание пользователя и сохранение в файл.
     */
    @Test
    void register_WithValidData_ShouldCreateNewUser() {
        boolean result = authService.register("newuser", "newpassword123");

        assertThat(result).isTrue();

        Map<String, User> users = authService.getUsers();
        assertThat(users).containsKey("newuser");
        assertThat(users.get("newuser").getRole()).isEqualTo(UserRole.USER);

        verify(userDataManager, atLeastOnce()).saveUsers(anyList());
    }

    /**
     * Тестирует неудачную регистрацию при существующем пользователе.
     * Проверяет что система не позволяет дублировать имена пользователей.
     */
    @Test
    void register_WithExistingUsername_ShouldReturnFalse() {
        authService.register("existinguser", "password123");

        boolean result = authService.register("existinguser", "newpassword");

        assertThat(result).isFalse();
    }

    /**
     * Тестирует регистрацию с некорректными данными.
     * Проверяет валидацию пустых и null значений.
     */
    @Test
    void register_WithInvalidData_ShouldReturnFalse() {
        assertThat(authService.register("", "password")).isFalse();

        assertThat(authService.register("user", "")).isFalse();

        assertThat(authService.register(null, "password")).isFalse();

        assertThat(authService.register("user", null)).isFalse();

        assertThat(authService.register("   ", "password")).isFalse();
    }

    /**
     * Тестирует выход пользователя из системы.
     * Проверяет сброс состояния аутентификации.
     */
    @Test
    void logout_ShouldClearCurrentUser() {
        authService.register("testuser", "password123", UserRole.USER);
        authService.login("testuser", "password123");
        assertThat(authService.isAuthenticated()).isTrue();

        authService.logout();

        assertThat(authService.isAuthenticated()).isFalse();
        assertThat(authService.getCurrentUser()).isEmpty();
        assertThat(authService.getCurrentUsername()).isEqualTo("Гость");
        assertThat(authService.getCurrentUserRole()).isNull();
    }

    /**
     * Тестирует проверку прав для управления товарами.
     * Проверяет что ADMIN и MANAGER могут управлять товарами, а USER - нет.
     */
    @Test
    void canManageProducts_ShouldReturnCorrectPermissions() {
        authService.register("admin", "admin123", UserRole.ADMIN);
        authService.login("admin", "admin123");
        assertThat(authService.canManageProducts()).isTrue();
        authService.logout();

        authService.register("manager", "manager123", UserRole.MANAGER);
        authService.login("manager", "manager123");
        assertThat(authService.canManageProducts()).isTrue();
        authService.logout();

        authService.register("user", "user123", UserRole.USER);
        authService.login("user", "user123");
        assertThat(authService.canManageProducts()).isFalse();
    }

    /**
     * Тестирует проверку прав для просмотра аудита.
     * Проверяет что только ADMIN и MANAGER могут просматривать аудит.
     */
    @Test
    void canViewAudit_ShouldReturnCorrectPermissions() {
        authService.register("admin", "admin123", UserRole.ADMIN);
        authService.login("admin", "admin123");
        assertThat(authService.canViewAudit()).isTrue();
        authService.logout();

        authService.register("manager", "manager123", UserRole.MANAGER);
        authService.login("manager", "manager123");
        assertThat(authService.canViewAudit()).isTrue();
        authService.logout();

        authService.register("user", "user123", UserRole.USER);
        authService.login("user", "user123");
        assertThat(authService.canViewAudit()).isFalse();
    }

    /**
     * Тестирует проверку роли администратора.
     * Проверяет что только пользователи с ролью ADMIN определяются как администраторы.
     */
    @Test
    void isAdmin_ShouldReturnTrueOnlyForAdminRole() {
        authService.register("admin", "admin123", UserRole.ADMIN);
        authService.login("admin", "admin123");
        assertThat(authService.isAdmin()).isTrue();
        authService.logout();

        authService.register("manager", "manager123", UserRole.MANAGER);
        authService.login("manager", "manager123");
        assertThat(authService.isAdmin()).isFalse();
        authService.logout();

        authService.register("user", "user123", UserRole.USER);
        authService.login("user", "user123");
        assertThat(authService.isAdmin()).isFalse();
    }

    /**
     * Тестирует получение списка пользователей.
     * Проверяет что возвращается копия карты пользователей.
     */
    @Test
    void getUsers_ShouldReturnCopyOfUsersMap() {
        // Given - добавляем нескольких пользователей
        authService.register("user1", "pass1", UserRole.USER);
        authService.register("user2", "pass2", UserRole.MANAGER);

        Map<String, User> users = authService.getUsers();

        assertThat(users).containsKeys("user1", "user2");

        /** Проверяем что возвращается копия (изменения не влияют на оригинал)*/
        int originalSize = users.size();
        users.put("temp", new User("temp", "temp", UserRole.USER));
        Map<String, User> usersAgain = authService.getUsers();
        assertThat(usersAgain).doesNotContainKey("temp");
        assertThat(usersAgain.size()).isEqualTo(originalSize);
    }

    /**
     * Тестирует поведение при отсутствии аутентификации.
     * Проверяет что все методы прав доступа возвращают false для неаутентифицированного пользователя.
     */
    @Test
    void permissionMethods_WhenNotAuthenticated_ShouldReturnFalse() {

        assertThat(authService.canManageProducts()).isFalse();
        assertThat(authService.canViewAudit()).isFalse();
        assertThat(authService.isAdmin()).isFalse();
        assertThat(authService.getCurrentUser()).isEmpty();
        assertThat(authService.getCurrentUsername()).isEqualTo("Гость");
        assertThat(authService.getCurrentUserRole()).isNull();
    }

    /**
     * Тестирует обрезку пробелов при регистрации.
     * Проверяет что пробелы в начале и конце логина/пароля обрезаются.
     */
    @Test
    void register_ShouldTrimUsernameAndPassword() {
        boolean result = authService.register("  trimmeduser  ", "  password  ");

        assertThat(result).isTrue();
        Map<String, User> users = authService.getUsers();
        assertThat(users).containsKey("trimmeduser");
        assertThat(users.get("trimmeduser").getPassword()).isEqualTo("password");
    }
}