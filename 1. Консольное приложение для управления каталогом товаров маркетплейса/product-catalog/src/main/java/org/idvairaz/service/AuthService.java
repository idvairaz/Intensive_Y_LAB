package org.idvairaz.service;

import org.idvairaz.io.UserDataManager;
import org.idvairaz.model.User;
import org.idvairaz.model.User.UserRole;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис аутентификации и авторизации пользователей.
 * Управляет регистрацией, входом в систему и проверкой прав доступа.
 * Автоматически сохраняет данные пользователей в файл.
 *
 * @author idvavraz
 * @version 1.0
 */
public class AuthService {
    /**
     * Карта зарегистрированных пользователей, где ключ - имя пользователя,
     * значение - объект пользователя
     */
    private final Map<String, User> users = new HashMap<>();

    /**
     * Текущий аутентифицированный пользователь
     */
    private User currentUser;

    /**
     * Менеджер для работы с данными пользователей (загрузка/сохранение)
     */
    private final UserDataManager dataManager;

    /**
     * Конструктор сервиса аутентификации.
     * Автоматически загружает пользователей из файла или создает пользователей по умолчанию.
     */
    public AuthService() {
        this.dataManager = new UserDataManager();
        loadUsersFromFile();
    }

    /**
     * Загружает пользователей из файла.
     * Если файл не существует, создает пользователей по умолчанию.
     */
    private void loadUsersFromFile() {
        List<User> loadedUsers = dataManager.loadUsers();
        if (loadedUsers != null && !loadedUsers.isEmpty()) {
            for (User user : loadedUsers) {
                users.put(user.getUsername(), user);
            }
        } else {
            /** Создаем пользователей по умолчанию только если файла нет*/
            initializeDefaultUsers();
        }
    }

    /**
     * Сохраняет текущих пользователей в файл.
     */
    private void saveUsersToFile() {
        dataManager.saveUsers(new ArrayList<>(users.values()));
    }

    /**
     * Инициализирует пользователей по умолчанию.
     * Создает администратора, менеджера и обычного пользователя.
     */
    private void initializeDefaultUsers() {
        users.put("admin", new User("admin", "admin123", UserRole.ADMIN));
        users.put("manager", new User("manager", "manager123", UserRole.MANAGER));
        users.put("user", new User("user", "user123", UserRole.USER));
        saveUsersToFile();
    }

    /**
     * Выполняет вход пользователя в систему.
     *
     * @param username имя пользователя
     * @param password пароль
     * @return true если аутентификация успешна, false в противном случае
     */
    public boolean login(String username, String password) {
        User user = users.get(username);
        if (user != null && user.getPassword().equals(password)) {
            user.setLoggedIn(true);
            currentUser = user;
            return true;
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
    public boolean register(String username, String password, UserRole role) {
        if (users.containsKey(username) ||
                username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            return false;
        }

        User newUser = new User(username.trim(), password.trim(), role);
        users.put(username.trim(), newUser);
        saveUsersToFile();
        return true;
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
    public void logout() {
        if (currentUser != null) {
            currentUser.setLoggedIn(false);
        }
        currentUser = null;
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
        return new HashMap<>(users);
    }
}
