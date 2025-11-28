package org.idvairaz.repository.impl;

import org.idvairaz.config.DatabaseConfig;
import org.idvairaz.model.User;
import org.idvairaz.repository.UserRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория пользователей для работы с PostgreSQL.
 * Обеспечивает сохранение, поиск и управление пользователями в базе данных.
 * Использует JDBC для прямого взаимодействия с PostgreSQL.
 *
 * @author idvavraz
 * @version 1.0
 */
public class PostgresUserRepository implements UserRepository {

    /** Имя схемы базы данных, используемой приложением */
    private final String schema = DatabaseConfig.getSchema();

    /**
     * Сохраняет или обновляет пользователя в базе данных.
     * Для новых пользователей генерирует идентификатор через sequence user_seq.
     * Для существующих пользователей обновляет все поля.
     *
     * @param user пользователь для сохранения или обновления
     * @return сохраненный пользователь
     * @throws RuntimeException если произошла ошибка SQL или пользователь для обновления не найден
     *
     *  Для новых пользователей:
     * 1. Получает следующий ID из sequence user_seq
     * 2. Выполняет INSERT с указанием всех полей
     * 3. Возвращает пользователя без изменения объекта
     *
     *  Для существующих пользователей:
     * 1. Выполняет UPDATE всех полей по username
     * 2. Генерирует исключение если пользователь не найден
     */

    @Override
    public User save(User user) {
        boolean isNew = user.getUsername() == null || !existsByUsername(user.getUsername());
        String sql;

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (isNew) {
                long newId = getNextIdFromSequence(conn);
                user.setId(newId);

                sql = "INSERT INTO " + schema + ".users (id, username, password, role, is_logged_in) " +
                        "VALUES (?, ?, ?, ?, ?)";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setLong(1, user.getId());
                    stmt.setString(2, user.getUsername());
                    stmt.setString(3, user.getPassword());
                    stmt.setString(4, user.getRole().name());
                    stmt.setBoolean(5, user.isLoggedIn());

                    stmt.executeUpdate();
                    System.out.println("Создан пользователь: " + user.getUsername() + " (ID: " + user.getId() + ")");
                    return user;
                }
            } else {
                Optional<User> existingUser = findByUsername(user.getUsername());
                if (existingUser.isEmpty()) {
                    throw new RuntimeException("Пользователь " + user.getUsername() + " не найден для обновления");
                }

                sql = "UPDATE " + schema + ".users SET password = ?, role = ?, is_logged_in = ? " +
                        "WHERE username = ?";

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, user.getPassword());
                    stmt.setString(2, user.getRole().name());
                    stmt.setBoolean(3, user.isLoggedIn());
                    stmt.setString(4, user.getUsername());

                    int affectedRows = stmt.executeUpdate();
                    if (affectedRows == 0) {
                        throw new RuntimeException("Пользователь " + user.getUsername() + " не найден для обновления");
                    }
                    user.setId(existingUser.get().getId());
                    return user;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения пользователя '" + user.getUsername() + "'", e);
        }
    }

    /**
     * Находит пользователя по идентификатору.
     * Поиск выполняется по точному совпадению числового идентификатора.
     *
     * @param id идентификатор пользователя для поиска
     * @return Optional с найденным пользователем, или empty если пользователь не найден
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     */
    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM " + schema + ".users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска пользователя по ID: " + id, e);
        }

        return Optional.empty();
    }

    /**
     * Находит пользователя по имени пользователя.
     * Поиск выполняется по точному совпадению имени (case-sensitive).
     *
     * @param username имя пользователя для поиска
     * @return Optional с найденным пользователем, или empty если пользователь не найден
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     */
    @Override
    public Optional<User> findByUsername(String username) {
        String sql = "SELECT * FROM " + schema + ".users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска пользователя по имени: " + username, e);
        }

        return Optional.empty();
    }

    /**
     * Возвращает всех пользователей из базы данных.
     * Пользователи возвращаются в алфавитном порядке их имен.
     *
     * @return список всех пользователей, может быть пустым если пользователей нет
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     */
    @Override
    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".users ORDER BY username";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения всех пользователей", e);
        }

        return users;
    }

    /**
     * Удаляет пользователя по идентификатору.
     * Если пользователь с указанным ID не существует, операция считается успешной
     * и выводится информационное сообщение.
     *
     * @param id идентификатор пользователя для удаления
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     *
     *  Метод не генерирует исключение если пользователь не найден,
     * только выводит сообщение в консоль
     */
    @Override
    public void deleteById(Long id) {
        String sql = "DELETE FROM " + schema + ".users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("Пользователь с ID " + id + " не найден для удаления");
            } else {
                System.out.println("Пользователь с ID " + id + " удален");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления пользователя по ID: " + id, e);
        }
    }

    /**
     * Удаляет пользователя по имени пользователя.
     * Если пользователь с указанным именем не существует, операция считается успешной
     * и выводится информационное сообщение.
     *
     * @param username имя пользователя для удаления
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     *
     *  Метод не генерирует исключение если пользователь не найден,
     * только выводит сообщение в консоль
     */
    @Override
    public void deleteByUsername(String username) {
        String sql = "DELETE FROM " + schema + ".users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("Пользователь " + username + " не найден для удаления");
            } else {
                System.out.println("Пользователь " + username + " удален");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления пользователя: " + username, e);
        }
    }

    /**
     * Проверяет существование пользователя с указанным именем.
     *
     * @param username имя пользователя для проверки
     * @return true если пользователь существует, false в противном случае
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     */
    @Override
    public boolean existsByUsername(String username) {
        String sql = "SELECT COUNT(*) FROM " + schema + ".users WHERE username = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки существования пользователя: " + username, e);
        }

        return false;
    }

    /**
     * Проверяет существование пользователя с указанным идентификатором.
     *
     * @param id идентификатор пользователя для проверки
     * @return true если пользователь существует, false в противном случае
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     */
    public boolean existsById(Long id) {
        String sql = "SELECT COUNT(*) FROM " + schema + ".users WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка проверки существования пользователя по ID: " + id, e);
        }

        return false;
    }

    /**
     * Возвращает общее количество пользователей в системе.
     *
     * @return количество пользователей (0 если пользователей нет)
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     */
    @Override
    public int getTotalUsersCount() {
        String sql = "SELECT COUNT(*) FROM " + schema + ".users";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения количества пользователей", e);
        }

        return 0;
    }

    /**
     * Получает следующий идентификатор из sequence user_seq.
     * Используется для генерации уникальных ID новых пользователей.
     *
     * @param conn активное соединение с базой данных
     * @return следующий уникальный идентификатор из sequence
     * @throws SQLException если произошла ошибка при обращении к sequence
     *
     *  Sequence должна быть создана в той же схеме, что и таблица users
     * @see #save(User)
     */
    private long getNextIdFromSequence(Connection conn) throws SQLException {
        String sequenceSql = "SELECT nextval('" + schema + ".user_seq')";

        try (PreparedStatement seqStmt = conn.prepareStatement(sequenceSql);
             ResultSet rs = seqStmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("Не удалось получить значение из sequence user_seq");
            }
        }
    }

    /**
     * Преобразует ResultSet в объект User.
     * Вспомогательный метод для маппинга данных из базы в Java-объект.
     * Предполагает, что ResultSet позиционирован на корректной строке.
     *
     * @param rs ResultSet с данными пользователя, позиционированный на нужной строке
     * @return объект User с данными из ResultSet
     * @throws SQLException если произошла ошибка при чтении данных из ResultSet
     *
     *  Метод не перемещает курсор ResultSet, вызывающий код должен
     * убедиться что rs.next() был вызван и вернул true
     *  Поле id не устанавливается, так как не используется в бизнес-логике User
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setRole(User.UserRole.valueOf(rs.getString("role")));
        user.setLoggedIn(rs.getBoolean("is_logged_in"));

        return user;
    }
}
