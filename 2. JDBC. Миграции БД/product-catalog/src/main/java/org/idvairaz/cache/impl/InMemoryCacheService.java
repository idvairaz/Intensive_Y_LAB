package org.idvairaz.cache.impl;

import org.idvairaz.cache.CacheService;

import java.util.HashMap;
import java.util.Optional;
import java.util.Map;

/**
 * In-memory реализация сервиса кэширования.
 * @param <K> тип ключа
 * @param <V> тип значения
 * @author idvavraz
 * @version 1.0
 */
public class InMemoryCacheService<K, V> implements CacheService<K, V> {

    /** Основное хранилище кэшированных данных */
    private final Map<K, V> cache = new HashMap<>();

    /** Статистика использования кэша (попадания, промахи и т.д.) */
    private final Map<String, Integer> stats = new HashMap<>();

    /**
     * Конструктор инициализирует счетчики статистики.
     */
    public InMemoryCacheService() {
        stats.put("hits", 0);
        stats.put("misses", 0);
        stats.put("puts", 0);
        stats.put("removals", 0);
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
        stats.put("puts", stats.get("puts") + 1);
    }

    @Override
    public Optional<V> get(K key) {
        V value = cache.get(key);
        if (value != null) {
            stats.put("hits", stats.get("hits") + 1);
            return Optional.of(value);
        } else {
            stats.put("misses", stats.get("misses") + 1);
            return Optional.empty();
        }
    }

    @Override
    public void remove(K key) {
        cache.remove(key);
        stats.put("removals", stats.get("removals") + 1);
    }

    @Override
    public void clear() {
        cache.clear();
        stats.put("hits", 0);
        stats.put("misses", 0);
        stats.put("puts", 0);
        stats.put("removals", 0);
    }

    @Override
    public boolean containsKey(K key) {
        return cache.containsKey(key);
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public Map<String, Object> getStats() {
        int hits = stats.get("hits");
        int misses = stats.get("misses");
        int total = hits + misses;
        double hitRate = total > 0 ? (double) hits / total * 100 : 0;

        Map<String, Object> result = new HashMap<>();
        result.put("hits", hits);
        result.put("misses", misses);
        result.put("puts", stats.get("puts"));
        result.put("removals", stats.get("removals"));
        result.put("hitRate", hitRate);
        result.put("totalRequests", total);
        result.put("currentSize", size());
        return result;
    }
}
