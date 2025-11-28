package org.idvairaz.repository;

import org.idvairaz.model.User;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для работы с пользователями.
 * Определяет контракт для операций хранения и поиска пользователей.
 *
 * @author idvavraz
 * @version 1.0
 */
public interface UserRepository {

    /**
     * Сохраняет пользователя в репозитории.
     * Если пользователь новый, присваивает ему идентификатор через sequence.
     *
     * @param user пользователь для сохранения
     * @return сохраненный пользователь с присвоенным ID (для новых пользователей)
     */
    User save(User user);

    /**
     * Находит пользователя по идентификатору.
     *
     * @param id идентификатор пользователя
     * @return Optional с найденным пользователем или empty если не найден
     */
    Optional<User> findById(Long id);

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя для поиска
     * @return Optional с найденным пользователем или empty если не найден
     */
    Optional<User> findByUsername(String username);

    /**
     * Возвращает всех пользователей из репозитория.
     *
     * @return список всех пользователей
     */
    List<User> findAll();

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id идентификатор пользователя для удаления
     */
    void deleteById(Long id);

    /**
     * Удаляет пользователя по имени пользователя.
     *
     * @param username имя пользователя для удаления
     */
    void deleteByUsername(String username);

    /**
     * Проверяет существование пользователя с указанным именем.
     *
     * @param username имя пользователя для проверки
     * @return true если пользователь существует, false в противном случае
     */
    boolean existsByUsername(String username);

    /**
     * Проверяет существование пользователя с указанным именем.
     *
     * @param id ID пользователя для проверки
     * @return true если пользователь существует, false в противном случае
     */
    boolean existsById(Long id);

    /**
     * Возвращает общее количество пользователей в системе.
     *
     * @return количество пользователей
     */
    int getTotalUsersCount();
}
