package org.idvairaz.cache.impl;


import org.idvairaz.cache.CacheService;
import org.idvairaz.cache.ProductCacheService;
import org.idvairaz.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Реализация сервиса кэширования товаров.
 * Использует три отдельных кэша для разных типов данных о товарах.
 *
 * @author idvavraz
 * @version 1.0
 */
public class ProductCacheServiceImpl implements ProductCacheService {

    /** Кэш товаров по идентификаторам */
    private final CacheService<Long, Product> productCache;

    /** Кэш товаров по категориям */
    private final CacheService<String, List<Product>> categoryCache;

    /** Кэш товаров по брендам */
    private final CacheService<String, List<Product>> brandCache;

    /**
     * Конструктор инициализирует три уровня кэширования.
     */
    public ProductCacheServiceImpl() {
        this.productCache = new InMemoryCacheService<>();
        this.categoryCache = new InMemoryCacheService<>();
        this.brandCache = new InMemoryCacheService<>();
        System.out.println("Сервис кэширования товаров инициализирован");
    }

    @Override
    public void cacheProduct(Product product) {
        if (product != null && product.getId() != null) {
            productCache.put(product.getId(), product);
        }
    }

    @Override
    public Optional<Product> getCachedProduct(Long id) {
        return productCache.get(id);
    }

    @Override
    public void cacheProductsByCategory(String category, List<Product> products) {
        if (category != null && products != null) {
            categoryCache.put(category.toLowerCase(), new ArrayList<>(products));
        }
    }

    @Override
    public Optional<List<Product>> getCachedProductsByCategory(String category) {
        return categoryCache.get(category.toLowerCase());
    }

    @Override
    public void cacheProductsByBrand(String brand, List<Product> products) {
        if (brand != null && products != null) {
            brandCache.put(brand.toLowerCase(), new ArrayList<>(products));
        }
    }

    @Override
    public Optional<List<Product>> getCachedProductsByBrand(String brand) {
        return brandCache.get(brand.toLowerCase());
    }

    @Override
    public void clear() {
        productCache.clear();
        categoryCache.clear();
        brandCache.clear();
        System.out.println("Все кэши товаров очищены");
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("productCache", productCache.getStats());
        stats.put("categoryCache", categoryCache.getStats());
        stats.put("brandCache", brandCache.getStats());
        stats.put("totalCachedProducts", productCache.size());
        stats.put("totalCachedCategories", categoryCache.size());
        stats.put("totalCachedBrands", brandCache.size());
        return stats;
    }

    @Override
    public void invalidateProduct(Long id) {
        productCache.remove(id);
        clearCategoryAndBrandCaches();
    }

    /**
     * Очищает кэши категорий и брендов.
     * Вызывается при изменении товаров, так как списки могли измениться.
     */
    private void clearCategoryAndBrandCaches() {
        categoryCache.clear();
        brandCache.clear();
    }
}
