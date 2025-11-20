package org.idvairaz.service;

import org.idvairaz.io.UserDataManager;
import org.idvairaz.model.User;
import org.idvairaz.model.User.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
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

@DisplayName("Тесты сервиса аутентификации и авторизации")
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

    @Test
    @DisplayName("Должен успешно аутентифицировать пользователя с правильными учетными данными")
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

    @Test
    @DisplayName("Должен вернуть false при неверном пароле")
    void login_WithInvalidPassword_ShouldReturnFalse() {
        authService.register("testuser", "password123", UserRole.USER);

        boolean result = authService.login("testuser", "wrongpassword");

        assertThat(result).isFalse();
        assertThat(authService.isAuthenticated()).isFalse();
        assertThat(authService.getCurrentUser()).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть false при входе несуществующего пользователя")
    void login_WithNonExistentUser_ShouldReturnFalse() {
        boolean result = authService.login("unknown", "password");

        assertThat(result).isFalse();
        assertThat(authService.isAuthenticated()).isFalse();
    }

    @Test
    @DisplayName("Должен успешно зарегистрировать нового пользователя")
    void register_WithValidData_ShouldCreateNewUser() {
        boolean result = authService.register("newuser", "newpassword123");

        assertThat(result).isTrue();

        Map<String, User> users = authService.getUsers();
        assertThat(users).containsKey("newuser");
        assertThat(users.get("newuser").getRole()).isEqualTo(UserRole.USER);

        verify(userDataManager, atLeastOnce()).saveUsers(anyList());
    }

    @Test
    @DisplayName("Должен вернуть false при регистрации существующего пользователя")
    void register_WithExistingUsername_ShouldReturnFalse() {
        authService.register("existinguser", "password123");

        boolean result = authService.register("existinguser", "newpassword");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Должен вернуть false при регистрации с некорректными данными")
    void register_WithInvalidData_ShouldReturnFalse() {
        assertThat(authService.register("", "password")).isFalse();
        assertThat(authService.register("user", "")).isFalse();
        assertThat(authService.register(null, "password")).isFalse();
        assertThat(authService.register("user", null)).isFalse();
        assertThat(authService.register("   ", "password")).isFalse();
    }

    @Test
    @DisplayName("Должен выйти из системы и сбросить состояние аутентификации")
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

    @Test
    @DisplayName("Должен корректно проверять права на управление товарами")
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

    @Test
    @DisplayName("Должен корректно проверять права на просмотр аудита")
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

    @Test
    @DisplayName("Должен корректно определять роль администратора")
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

    @Test
    @DisplayName("Должен возвращать копию карты пользователей")
    void getUsers_ShouldReturnCopyOfUsersMap() {
        authService.register("user1", "pass1", UserRole.USER);
        authService.register("user2", "pass2", UserRole.MANAGER);

        Map<String, User> users = authService.getUsers();

        assertThat(users).containsKeys("user1", "user2");

        int originalSize = users.size();
        users.put("temp", new User("temp", "temp", UserRole.USER));
        Map<String, User> usersAgain = authService.getUsers();
        assertThat(usersAgain).doesNotContainKey("temp");
        assertThat(usersAgain.size()).isEqualTo(originalSize);
    }

    @Test
    @DisplayName("Должен возвращать false для всех проверок прав при отсутствии аутентификации")
    void permissionMethods_WhenNotAuthenticated_ShouldReturnFalse() {
        assertThat(authService.canManageProducts()).isFalse();
        assertThat(authService.canViewAudit()).isFalse();
        assertThat(authService.isAdmin()).isFalse();
        assertThat(authService.getCurrentUser()).isEmpty();
        assertThat(authService.getCurrentUsername()).isEqualTo("Гость");
        assertThat(authService.getCurrentUserRole()).isNull();
    }

    @Test
    @DisplayName("Должен обрезать пробелы в логине и пароле при регистрации")
    void register_ShouldTrimUsernameAndPassword() {
        boolean result = authService.register("  trimmeduser  ", "  password  ");

        assertThat(result).isTrue();
        Map<String, User> users = authService.getUsers();
        assertThat(users).containsKey("trimmeduser");
        assertThat(users.get("trimmeduser").getPassword()).isEqualTo("password");
    }
}