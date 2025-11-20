package org.idvairaz.repository.impl;

import org.idvairaz.config.DatabaseConfig;
import org.idvairaz.repository.AuditRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Реализация репозитория аудита для работы с PostgreSQL.
 * Сохраняет записи аудита в отдельной таблице в схеме audit.
 *
 * @author idvavraz
 * @version 1.0
 */
public class PostgresAuditRepository implements AuditRepository {

    /** Имя схемы базы данных для таблиц аудита */
    private final String schema = "audit";

    /**
     * Сохраняет запись аудита в таблицу audit.logs.
     * Автоматически устанавливает временную метку создания.
     *
     * @param username имя пользователя
     * @param action действие пользователя
     * @param details детали действия
     * @throws RuntimeException если произошла ошибка при сохранении
     */
    @Override
    public void save(String username, String action, String details) {
        String sql = "INSERT INTO " + schema + ".logs (id, username, action, details, created_at) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConfig.getConnection()) {
            long nextId = getNextIdFromSequence(conn);

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setLong(1, nextId);
                stmt.setString(2, username);
                stmt.setString(3, action);
                stmt.setString(4, details);
                stmt.setTimestamp(5, Timestamp.valueOf(java.time.LocalDateTime.now()));

                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения записи аудита для пользователя: " + username, e);
        }
    }

    /**
     * Возвращает все записи аудита, отсортированные по дате создания (новые сначала).
     * Форматирует записи в читаемый вид.
     *
     * @return список отформатированных записей аудита
     * @throws RuntimeException если произошла ошибка при чтении данных
     */
    @Override
    public List<String> findAll() {
        List<String> logs = new ArrayList<>();
        String sql = "SELECT username, action, details, created_at FROM " + schema + ".logs ORDER BY created_at DESC";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String log = String.format("[%s] Пользователь: %s | Действие: %s | Детали: %s",
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("username"),
                        rs.getString("action"),
                        rs.getString("details"));
                logs.add(log);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения записей аудита", e);
        }

        return logs;
    }

    /**
     * Удаляет все записи из таблицы аудита.
     *
     * @throws RuntimeException если произошла ошибка при очистке
     */
    @Override
    public void clear() {
        String sql = "DELETE FROM " + schema + ".logs";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка очистки журнала аудита", e);
        }
    }

    /**
     * Получает следующий идентификатор из sequence audit_log_seq.
     *
     * @param conn активное соединение с базой данных
     * @return следующий уникальный идентификатор из sequence
     * @throws SQLException если произошла ошибка при обращении к sequence
     */
    private long getNextIdFromSequence(Connection conn) throws SQLException {
        String sequenceSql = "SELECT nextval('audit.audit_log_seq')";
        try (PreparedStatement seqStmt = conn.prepareStatement(sequenceSql);
             ResultSet rs = seqStmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("Не удалось получить значение из sequence audit_log_seq");
            }
        }
    }
}
