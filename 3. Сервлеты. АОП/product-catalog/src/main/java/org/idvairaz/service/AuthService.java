package org.idvairaz.service;

import lombok.RequiredArgsConstructor;
import org.idvairaz.aspect.Auditable;
import org.idvairaz.model.User;
import org.idvairaz.model.User.UserRole;

import java.util.Map;
import java.util.Optional;

/**
 * Сервис аутентификации и авторизации пользователей.
 * Управляет регистрацией, входом в систему и проверкой прав доступа.
 *
 * @author idvavraz
 * @version 2.0
 */
@RequiredArgsConstructor
public class AuthService {

    /** Сервис для работы с пользователями */
    private final UserService userService;

    /** Текущий аутентифицированный пользователь */
    private User currentUser;

    /**
     * Выполняет вход пользователя в систему.
     * Проверяет учетные данные и обновляет статус входа.
     *
     * @param username имя пользователя
     * @param password пароль
     * @return true если аутентификация успешна, false в противном случае
     */
    @Auditable(value = "ВХОД_В_СИСТЕМУ")
    public boolean login(String username, String password) {
        Optional<User> userOpt = userService.getUserByUsername(username);

        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(password)) {
                user.setLoggedIn(true);
                userService.updateUser(user);
                currentUser = user;
                return true;
            }
        }
        return false;
    }

    /**
     * Регистрирует нового пользователя с указанной ролью.
     *
     * @param username имя пользователя
     * @param password пароль
     * @param role роль пользователя
     * @return true если регистрация успешна, false если пользователь уже существует или данные некорректны
     */
    @Auditable(value = "РЕГИСТРАЦИЯ_В_СИСТЕМЕ")
    public boolean register(String username, String password, UserRole role) {
        try {
            userService.createUser(username, password, role);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка регистрации: " + e.getMessage());
            return false;
        }
    }

    /**
     * Регистрирует нового пользователя с ролью USER по умолчанию.
     *
     * @param username имя пользователя
     * @param password пароль
     * @return true если регистрация успешна, false если пользователь уже существует или данные некорректны
     */
    public boolean register(String username, String password) {
        return register(username, password, UserRole.USER);
    }

    /**
     * Выполняет выход текущего пользователя из системы.
     */
    @Auditable(value = "ВЫХОД_ИЗ_СИСТЕМЫ")
    public void logout() {
        if (currentUser != null) {
            currentUser.setLoggedIn(false);
            userService.updateUser(currentUser);
            currentUser = null;
        }
    }

    /**
     * Проверяет, выполнен ли вход в систему.
     *
     * @return true если пользователь аутентифицирован, false в противном случае
     */
    public boolean isAuthenticated() {
        return currentUser != null;
    }

    /**
     * Возвращает текущего аутентифицированного пользователя.
     *
     * @return Optional с текущим пользователем или empty если пользователь не аутентифицирован
     */
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentUser);
    }

    /**
     * Возвращает имя текущего пользователя.
     *
     * @return имя аутентифицированного пользователя или "Гость" если пользователь не аутентифицирован
     */
    public String getCurrentUsername() {
        return currentUser != null ? currentUser.getUsername() : "Гость";
    }

    /**
     * Возвращает роль текущего пользователя.
     *
     * @return роль аутентифицированного пользователя или null если пользователь не аутентифицирован
     */
    public UserRole getCurrentUserRole() {
        return currentUser != null ? currentUser.getRole() : null;
    }

    /**
     * Проверяет, может ли текущий пользователь управлять товарами.
     *
     * @return true если пользователь имеет права ADMIN или MANAGER
     */
    public boolean canManageProducts() {
        return currentUser != null && currentUser.canManageProducts();
    }

    /**
     * Проверяет, может ли текущий пользователь просматривать журнал аудита.
     *
     * @return true если пользователь имеет права ADMIN или MANAGER
     */
    public boolean canViewAudit() {
        return currentUser != null && currentUser.canViewAudit();
    }

    /**
     * Проверяет, является ли текущий пользователь администратором.
     *
     * @return true если пользователь имеет роль ADMIN
     */
    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    /**
     * Возвращает копию карты всех зарегистрированных пользователей.
     *
     * @return карта пользователей (имя -> пользователь)
     */
    public Map<String, User> getUsers() {
        return userService.getUsersMap();
    }

    /**
     * Регистрирует нового пользователя с указанной ролью (только для администраторов).
     *
     * @param username имя пользователя
     * @param password пароль
     * @param role роль пользователя
     * @return true если регистрация успешна, false если пользователь уже существует или данные некорректны
     */
    public boolean registerWithRole(String username, String password, UserRole role) {
        if (!isAdmin()) {
            System.out.println("Ошибка: только администратор может создавать пользователей с заданной ролью");
            return false;
        }

        return register(username, password, role);
    }

    /**
     * Изменяет роль пользователя (только для администраторов).
     *
     * @param username имя пользователя
     * @param newRole новая роль пользователя
     * @return true если изменение успешно, false если пользователь не найден
     */
    public boolean changeUserRole(String username, UserRole newRole) {
        if (!isAdmin()) {
            System.out.println("Ошибка: только администратор может изменять роли пользователей");
            return false;
        }

        try {
            userService.updateUserRole(username, newRole);
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка изменения роли: " + e.getMessage());
            return false;
        }
    }

    /**
     * Удаляет пользователя (только для администраторов).
     *
     * @param username имя пользователя для удаления
     * @return true если удаление успешно, false если пользователь не найден
     */
    public boolean deleteUser(String username) {
        if (!isAdmin()) {
            System.out.println("Ошибка: только администратор может удалять пользователей");
            return false;
        }

        if (username.equals(getCurrentUsername())) {
            System.out.println("Ошибка: нельзя удалить самого себя");
            return false;
        }

        try {
            userService.deleteUserByName(username, getCurrentUsername());
            return true;
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка удаления пользователя: " + e.getMessage());
            return false;
        }
    }
}

