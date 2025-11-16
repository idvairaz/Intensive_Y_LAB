package org.idvairaz.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Сервис для ведения журнала аудита действий пользователей.
 * Записывает все значимые события в системе с временными метками.
 *
 * @author idvavraz
 * @version 1.0
 */
public class AuditService {

    /** Список для хранения записей журнала аудита */
    private final List<String> auditLog = new ArrayList<>();

    /**
     * Записывает действие пользователя в журнал аудита.
     * Автоматически добавляет временную метку и выводит запись в консоль.
     *
     * @param username имя пользователя, выполнившего действие
     * @param action тип выполненного действия
     * @param details дополнительные детали действия
     */
    public void logAction(String username, String action, String details) {
        String timestamp = LocalDateTime.now().toString();
        String logEntry = String.format("[%s] Пользователь: %s | Действие: %s | Детали: %s",
                timestamp, username, action, details);
        auditLog.add(logEntry);
        System.out.println(" - " + logEntry);
    }

    /**
     * Отображает весь журнал аудита в консоли.
     * Если журнал пуст, выводит соответствующее сообщение.
     */
    public void showAuditLog() {
        System.out.println("\n=== ЖУРНАЛ АУДИТА ===");
        if (auditLog.isEmpty()) {
            System.out.println("Журнал пуст");
        } else {
            auditLog.forEach(System.out::println);
        }
    }
}
