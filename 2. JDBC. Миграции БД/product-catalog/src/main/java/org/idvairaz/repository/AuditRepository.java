package org.idvairaz.repository;

import java.util.List;

/**
 * Интерфейс репозитория для работы с журналом аудита.
 * Отвечает за сохранение и получение записей о действиях пользователей.
 *
 * @author idvavraz
 * @version 1.0
 */
public interface AuditRepository {

    /**
     * Сохраняет запись аудита в базу данных.
     *
     * @param username имя пользователя
     * @param action действие пользователя
     * @param details детали действия
     */
    void save(String username, String action, String details);

    /**
     * Возвращает все записи аудита из базы данных.
     *
     * @return список записей аудита в формате строк
     */
    List<String> findAll();

    /**
     * Очищает все записи аудита.
     */
    void clear();
}
