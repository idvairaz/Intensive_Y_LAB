package org.idvairaz.service;

import org.idvairaz.model.User;
import org.idvairaz.model.User.UserRole;
import org.idvairaz.repository.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления пользователями.
 * Содержит бизнес-логику работы с пользователями и взаимодействует с PostgreSQL.
 *
 * @author idvavraz
 * @version 1.0
 */

public class UserService {

    /** Репозиторий для работы с пользователями */
    private final UserRepository userRepository;

    /**
     * Конструктор сервиса пользователей.
     *
     * @param userRepository репозиторий для работы с пользователями
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        System.out.println("Сервис пользователей инициализирован. Пользователей в базе: " +
                userRepository.getTotalUsersCount());
    }

    /**
     * Создает нового пользователя в базе данных.
     * ID пользователя генерируется через sequence user_seq.
     *
     * @param username имя пользователя (должно быть уникальным)
     * @param password пароль пользователя
     * @param role роль пользователя в системе
     * @return созданный пользователь
     * @throws IllegalArgumentException если пользователь с таким именем уже существует или данные некорректны
     * @throws RuntimeException если произошла ошибка при сохранении в базу данных
     */
    public User createUser(String username, String password, UserRole role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь с именем '" + username + "' уже существует");
        }

        validateUserCredentials(username, password);

        User newUser = User.builder()
                .username(username.trim())
                .password(password.trim())
                .role(role)
                .isLoggedIn(false)
                .build();

        return userRepository.save(newUser);
    }

    /**
     * Обновляет роль пользователя в системе.
     *
     * @param username имя пользователя для обновления
     * @param newRole новая роль пользователя
     * @return обновленный пользователь
     * @throws IllegalArgumentException если пользователь не найден
     * @throws RuntimeException если произошла ошибка при обновлении в базе данных
     */
    public User updateUserRole(String username, UserRole newRole) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Пользователь '" + username + "' не найден");
        }

        User user = userOpt.get();
        user.setRole(newRole);
        return userRepository.save(user);
    }

    /**
     * Удаляет пользователя по имени пользователя.
     *
     * @param username имя пользователя для удаления
     * @param currentUsername имя текущего пользователя (для проверки самоссылки)
     * @throws IllegalArgumentException если пользователь не найден или попытка удалить самого себя
     * @throws RuntimeException если произошла ошибка при удалении из базы данных
     */
    public void deleteUserByName(String username, String currentUsername) {
        if (username.equals(currentUsername)) {
            throw new IllegalArgumentException("Нельзя удалить самого себя");
        }

        if (!userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Пользователь '" + username + "' не найден");
        }

        userRepository.deleteByUsername(username);
    }

    /**
     * Удаляет пользователя по ID.
     *
     * @param userId ID пользователя для удаления
     * @param currentUserId ID текущего пользователя
     * @throws IllegalArgumentException если пользователь не найден или попытка удалить самого себя
     * @throws RuntimeException если произошла ошибка при удалении из базы данных
     */
    public void deleteUserById(Long userId, Long currentUserId) {
        if (userId.equals(currentUserId)) {
            throw new IllegalArgumentException("Нельзя удалить самого себя");
        }

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Пользователь с ID '" + userId + "' не найден");
        }

        userRepository.deleteById(userId);
    }

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя для поиска
     * @return Optional с найденным пользователем или empty если не найден
     * @throws RuntimeException если произошла ошибка при поиске в базе данных
     */
    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Возвращает всех пользователей системы.
     *
     * @return список всех пользователей, отсортированный по имени
     * @throws RuntimeException если произошла ошибка при получении данных из базы
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Возвращает пользователей в виде карты для быстрого поиска по имени.
     *
     * @return карта пользователей (имя пользователя -> объект User)
     * @throws RuntimeException если произошла ошибка при получении данных из базы
     */
    public Map<String, User> getUsersMap() {
        return userRepository.findAll().stream()
                .collect(Collectors.toMap(User::getUsername, user -> user));
    }

    /**
     * Проверяет существование пользователя с указанным именем.
     *
     * @param username имя пользователя для проверки
     * @return true если пользователь существует, false в противном случае
     * @throws RuntimeException если произошла ошибка при проверке в базе данных
     */
    public boolean userExists(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Возвращает общее количество пользователей в системе.
     *
     * @return количество пользователей
     * @throws RuntimeException если произошла ошибка при подсчете в базе данных
     */
    public int getUserCount() {
        return userRepository.getTotalUsersCount();
    }

    /**
     * Обновляет информацию о пользователе в базе данных.
     *
     * @param user пользователь для обновления
     * @return обновленный пользователь
     * @throws RuntimeException если произошла ошибка при обновлении в базе данных
     */
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Валидирует учетные данные пользователя.
     *
     * @param username имя пользователя
     * @param password пароль
     * @throws IllegalArgumentException если данные некорректны
     */
    private void validateUserCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя пользователя не может быть пустым");
        }

        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Пароль не может быть пустым");
        }

        if (username.length() < 3) {
            throw new IllegalArgumentException("Имя пользователя должно содержать минимум 3 символа");
        }

        if (password.length() < 4) {
            throw new IllegalArgumentException("Пароль должен содержать минимум 4 символа");
        }
    }
}
