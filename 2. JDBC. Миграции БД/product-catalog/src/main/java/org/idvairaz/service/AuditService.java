package org.idvairaz.service;

import org.idvairaz.repository.AuditRepository;
import java.util.List;

/**
 * Сервис для ведения журнала аудита действий пользователей.
 * Использует репозиторий для сохранения записей в базу данных PostgreSQL.
 *
 * @author idvavraz
 * @version 2.0
 */
public class AuditService {

    /** Репозиторий для работы с записями аудита */
    private final AuditRepository auditRepository;

    /**
     * Конструктор сервиса аудита.
     *
     * @param auditRepository репозиторий для работы с записями аудита
     */
    public AuditService(AuditRepository auditRepository) {
        this.auditRepository = auditRepository;
        System.out.println("Сервис аудита инициализирован с репозиторием PostgreSQL");
    }

    /**
     * Записывает действие пользователя в журнал аудита.
     * Сохраняет запись в базу данных через репозиторий.
     * Также выводит запись в консоль для немедленного отображения.
     *
     * @param username имя пользователя, выполнившего действие
     * @param action тип выполненного действия
     * @param details дополнительные детали действия
     */
    public void logAction(String username, String action, String details) {
        auditRepository.save(username, action, details);

        String timestamp = java.time.LocalDateTime.now().toString();
        String logEntry = String.format("[%s] Пользователь: %s | Действие: %s | Детали: %s",
                timestamp, username, action, details);
        System.out.println(" - " + logEntry);
    }

    /**
     * Отображает весь журнал аудита из базы данных.
     * Записи отображаются в порядке от новых к старым.
     * Если журнал пуст, выводит соответствующее сообщение.
     */
    public void showAuditLog() {
        System.out.println("\n=== ЖУРНАЛ АУДИТА (ИЗ БАЗЫ ДАННЫХ) ===");
        List<String> auditLog = auditRepository.findAll();

        if (auditLog.isEmpty()) {
            System.out.println("Журнал аудита пуст");
        } else {
            System.out.println("Всего записей: " + auditLog.size());
            auditLog.forEach(System.out::println);
        }
    }

    /**
     * Очищает весь журнал аудита в базе данных.
     * Используется для обслуживания системы.
     */
    public void clearAuditLog() {
        auditRepository.clear();
        System.out.println("Журнал аудита очищен");
    }
}
