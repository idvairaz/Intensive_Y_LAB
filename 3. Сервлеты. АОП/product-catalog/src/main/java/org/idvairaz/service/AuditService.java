package org.idvairaz.service;

import lombok.AllArgsConstructor;
import org.idvairaz.repository.AuditRepository;

import java.time.LocalDateTime;



/**
 * Сервис для ведения журнала аудита действий пользователей.
 * Использует репозиторий для сохранения записей в базу данных PostgreSQL.
 *
 * @author idvavraz
 * @version 2.0
 */
@AllArgsConstructor
public class AuditService {

    /** Репозиторий для работы с записями аудита */
    private final AuditRepository auditRepository;


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

        String timestamp = LocalDateTime.now().toString();
        String logEntry = String.format("[%s] Пользователь: %s | Действие: %s | Детали: %s",
                timestamp, username, action, details);
        System.out.println(" - " + logEntry);
    }

}
