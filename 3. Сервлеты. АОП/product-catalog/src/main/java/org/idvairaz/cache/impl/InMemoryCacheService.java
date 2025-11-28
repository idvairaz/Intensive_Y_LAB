//package org.idvairaz.cache.impl;
//
//import org.idvairaz.cache.CacheService;
//
//import java.util.HashMap;
//import java.util.Optional;
//import java.util.Map;
//
///**
// * In-memory реализация сервиса кэширования.
// * @param <K> тип ключа
// * @param <V> тип значения
// * @author idvavraz
// * @version 1.0
// */
//public class InMemoryCacheService<K, V> implements CacheService<K, V> {
//
//    /** Основное хранилище кэшированных данных */
//    private final Map<K, V> cache = new HashMap<>();
//
//    /** Статистика использования кэша (попадания, промахи и т.д.) */
//    private final Map<String, Integer> stats = new HashMap<>();
//
//    /**
//     * Конструктор инициализирует счетчики статистики.
//     */
//    public InMemoryCacheService() {
//        stats.put("hits", 0);
//        stats.put("misses", 0);
//        stats.put("puts", 0);
//        stats.put("removals", 0);
//    }
//
//    @Override
//    public void put(K key, V value) {
//        cache.put(key, value);
//        stats.put("puts", stats.get("puts") + 1);
//    }
//
//    @Override
//    public Optional<V> get(K key) {
//        V value = cache.get(key);
//        if (value != null) {
//            stats.put("hits", stats.get("hits") + 1);
//            return Optional.of(value);
//        } else {
//            stats.put("misses", stats.get("misses") + 1);
//            return Optional.empty();
//        }
//    }
//
//    @Override
//    public void remove(K key) {
//        cache.remove(key);
//        stats.put("removals", stats.get("removals") + 1);
//    }
//
//    @Override
//    public void clear() {
//        cache.clear();
//        stats.put("hits", 0);
//        stats.put("misses", 0);
//        stats.put("puts", 0);
//        stats.put("removals", 0);
//    }
//
//    @Override
//    public boolean containsKey(K key) {
//        return cache.containsKey(key);
//    }
//
//    @Override
//    public int size() {
//        return cache.size();
//    }
//
//    @Override
//    public Map<String, Object> getStats() {
//        int hits = stats.get("hits");
//        int misses = stats.get("misses");
//        int total = hits + misses;
//        double hitRate = total > 0 ? (double) hits / total * 100 : 0;
//
//        Map<String, Object> result = new HashMap<>();
//        result.put("hits", hits);
//        result.put("misses", misses);
//        result.put("puts", stats.get("puts"));
//        result.put("removals", stats.get("removals"));
//        result.put("hitRate", hitRate);
//        result.put("totalRequests", total);
//        result.put("currentSize", size());
//        return result;
//    }
//}
package org.idvairaz.cache.impl;

import org.idvairaz.cache.CacheService;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * In-memory реализация сервиса кэширования с поддержкой TTL (Time To Live).
 * Автоматически удаляет устаревшие записи при обращении к ним.
 *
 * @param <K> тип ключа
 * @param <V> тип значения
 * @author idvavraz
 * @version 2.0
 */
public class InMemoryCacheService<K, V> implements CacheService<K, V> {

    /**
     * Внутренняя запись кэша, содержащая значение и время создания.
     * Используется для реализации TTL.
     *
     * @param <V> тип значения
     */
    private static class CacheEntry<V> {
        /**
         * Кэшированное значение.
         */
        private final V value;
        /**
         * Временная метка создания записи в миллисекундах.
         * Используется для проверки истечения TTL.
         */
        private final long timestamp;

        /**
         * Создает новую запись кэша с текущим временем создания.
         *
         * @param value значение для кэширования
         */
        CacheEntry(V value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        /**
         * Проверяет, истекло ли время жизни записи.
         *
         * @param ttlMillis время жизни в миллисекундах
         * @return true если запись устарела, false если еще действительна
         */
        boolean isExpired(long ttlMillis) {
            return System.currentTimeMillis() - timestamp > ttlMillis;
        }
    }

    /** Основное хранилище кэшированных данных с поддержкой TTL */
    private final Map<K, CacheEntry<V>> cache = new HashMap<>();

    /** Время жизни записей в кэше в миллисекундах */
    private final long ttlMillis;

    /** Статистика использования кэша */
    private final Map<String, Integer> stats = new HashMap<>();

    /**
     * Создает кэш с указанным временем жизни записей.
     *
     * @param ttlMillis время жизни записей в миллисекундах
     * @throws IllegalArgumentException если ttlMillis меньше или равно 0
     */
    public InMemoryCacheService(long ttlMillis) {
        if (ttlMillis <= 0) {
            throw new IllegalArgumentException("TTL must be positive");
        }
        this.ttlMillis = ttlMillis;
        initializeStats();
    }

    /**
     * Создает кэш с временем жизни по умолчанию (30 минут).
     */
    public InMemoryCacheService() {
        this(30 * 60 * 1000);
    }

    /**
     * Инициализирует счетчики статистики.
     */
    private void initializeStats() {
        stats.put("hits", 0);
        stats.put("misses", 0);
        stats.put("puts", 0);
        stats.put("removals", 0);
        stats.put("expired", 0);
    }

    @Override
    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        cache.put(key, new CacheEntry<>(value));
        stats.put("puts", stats.get("puts") + 1);
    }

    @Override
    public Optional<V> get(K key) {
        if (key == null) {
            return Optional.empty();
        }

        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            stats.put("misses", stats.get("misses") + 1);
            return Optional.empty();
        }

        if (entry.isExpired(ttlMillis)) {
            cache.remove(key);
            stats.put("misses", stats.get("misses") + 1);
            stats.put("expired", stats.get("expired") + 1);
            return Optional.empty();
        }

        stats.put("hits", stats.get("hits") + 1);
        return Optional.of(entry.value);
    }

    @Override
    public void remove(K key) {
        if (key != null) {
            cache.remove(key);
            stats.put("removals", stats.get("removals") + 1);
        }
    }

    @Override
    public void clear() {
        cache.clear();
        initializeStats();
    }

    @Override
    public boolean containsKey(K key) {
        if (key == null) {
            return false;
        }

        CacheEntry<V> entry = cache.get(key);
        if (entry == null) {
            return false;
        }

        if (entry.isExpired(ttlMillis)) {
            cache.remove(key);
            stats.put("expired", stats.get("expired") + 1);
            return false;
        }

        return true;
    }

    @Override
    public int size() {
        // Очищаем устаревшие записи перед подсчетом размера
        cleanExpiredEntries();
        return cache.size();
    }

    @Override
    public Map<String, Object> getStats() {
        cleanExpiredEntries(); // Очищаем устаревшие перед сбором статистики

        int hits = stats.get("hits");
        int misses = stats.get("misses");
        int total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("hits", hits);
        result.put("misses", misses);
        result.put("puts", stats.get("puts"));
        result.put("removals", stats.get("removals"));
        result.put("expired", stats.get("expired"));
        result.put("hitRate", hitRate);
        result.put("totalRequests", total);
        result.put("currentSize", cache.size());
        result.put("ttlMillis", ttlMillis);
        return result;
    }

    /**
     * Очищает все устаревшие записи из кэша.
     * Вызывается автоматически при операциях, но может быть вызван вручную.
     */
    public void cleanExpiredEntries() {
        int expiredCount = 0;
        var iterator = cache.entrySet().iterator();

        while (iterator.hasNext()) {
            var entry = iterator.next();
            if (entry.getValue().isExpired(ttlMillis)) {
                iterator.remove();
                expiredCount++;
            }
        }

        if (expiredCount > 0) {
            stats.put("expired", stats.get("expired") + expiredCount);
        }
    }

    /**
     * Возвращает время жизни записей в кэше.
     *
     * @return TTL в миллисекундах
     */
    public long getTtlMillis() {
        return ttlMillis;
    }
}