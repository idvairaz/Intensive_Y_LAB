package org.idvairaz.config;

import org.idvairaz.cache.CacheService;
import org.idvairaz.cache.ProductCacheService;
import org.idvairaz.cache.impl.InMemoryCacheService;
import org.idvairaz.cache.impl.ProductCacheServiceImpl;
import org.idvairaz.model.Product;

import java.util.List;

/**
 * Конфигурационный класс для настройки системы кэширования.
 * Позволяет централизованно управлять параметрами кэша.
 *
 * @author idvavraz
 * @version 1.0
 */
public class CacheConfig {

    /** Время жизни товаров в кэше (30 минут) */
    private final long productTtlMillis = 30 * 60 * 1000;

    /** Время жизни категорий в кэше (15 минут) */
    private final long categoryTtlMillis = 15 * 60 * 1000;

    /** Время жизни брендов в кэше (15 минут) */
    private final long brandTtlMillis = 15 * 60 * 1000;

    /** Время жизни поисковых результатов в кэше (10 минут) */
    private final long searchTtlMillis = 10 * 60 * 1000;

    /**
     * Создает и настраивает сервис кэширования товаров.
     *
     * @return настроенный экземпляр ProductCacheService
     */
    public ProductCacheService createProductCacheService() {
        CacheService<Long, Product> productCache = new InMemoryCacheService<>(productTtlMillis);
        CacheService<String, List<Product>> categoryCache = new InMemoryCacheService<>(categoryTtlMillis);
        CacheService<String, List<Product>> brandCache = new InMemoryCacheService<>(brandTtlMillis);
        CacheService<String, List<Product>> searchCache = new InMemoryCacheService<>(searchTtlMillis);

        return new ProductCacheServiceImpl(productCache, categoryCache, brandCache, searchCache);
    }
}