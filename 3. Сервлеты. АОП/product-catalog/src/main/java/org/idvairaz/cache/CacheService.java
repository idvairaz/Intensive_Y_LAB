package org.idvairaz.cache;

import java.util.Optional;
import java.util.Map;

/**
 * Интерфейс для сервиса кэширования.
 * Определяет базовые операции для работы с кэшем любого типа.
 *
 * @param <K> тип ключа
 * @param <V> тип значения
 * @author idvavraz
 * @version 1.0
 */
public interface CacheService<K, V> {

    /**
     * Сохраняет значение в кэше по указанному ключу.
     *
     * @param key ключ для сохранения
     * @param value значение для кэширования
     */
    void put(K key, V value);

    /**
     * Получает значение из кэша по ключу.
     *
     * @param key ключ для поиска
     * @return Optional с найденным значением или empty если не найден
     */
    Optional<V> get(K key);

    /**
     * Удаляет значение из кэша по ключу.
     *
     * @param key ключ для удаления
     */
    void remove(K key);

    /**
     * Очищает весь кэш.
     */
    void clear();

    /**
     * Проверяет наличие ключа в кэше.
     *
     * @param key ключ для проверки
     * @return true если ключ существует в кэше
     */
    boolean containsKey(K key);

    /**
     * Возвращает количество элементов в кэше.
     *
     * @return размер кэша
     */
    int size();

    /**
     * Возвращает статистику использования кэша.
     *
     * @return карта со статистическими данными
     */
    Map<String, Object> getStats();
}
