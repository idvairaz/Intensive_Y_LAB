//package org.idvairaz.cache.impl;
//
//
//import lombok.AllArgsConstructor;
//import org.idvairaz.cache.CacheService;
//import org.idvairaz.cache.ProductCacheService;
//import org.idvairaz.model.Product;
//
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//
///**
// * Реализация сервиса кэширования товаров.
// * Использует три отдельных кэша для разных типов данных о товарах.
// *
// * @author idvavraz
// * @version 1.0
// */
//public class ProductCacheServiceImpl implements ProductCacheService {
//
//    /** Кэш товаров по идентификаторам */
//    private final CacheService<Long, Product> productCache;
//
//    /** Кэш товаров по категориям */
//    private final CacheService<String, List<Product>> categoryCache;
//
//    /** Кэш товаров по брендам */
//    private final CacheService<String, List<Product>> brandCache;
//
//    /**
//     * Конструктор инициализирует три уровня кэширования.
//     */
//    public ProductCacheServiceImpl() {
//        this.productCache = new InMemoryCacheService<>();
//        this.categoryCache = new InMemoryCacheService<>();
//        this.brandCache = new InMemoryCacheService<>();
//        System.out.println("Сервис кэширования товаров инициализирован");
//    }
//
//    @Override
//    public void cacheProduct(Product product) {
//        if (product != null && product.getId() != null) {
//            productCache.put(product.getId(), product);
//        }
//    }
//
//    @Override
//    public Optional<Product> getCachedProduct(Long id) {
//        return productCache.get(id);
//    }
//
//    @Override
//    public void cacheProductsByCategory(String category, List<Product> products) {
//        if (category != null && products != null) {
//            categoryCache.put(category.toLowerCase(), new ArrayList<>(products));
//        }
//    }
//
//    @Override
//    public Optional<List<Product>> getCachedProductsByCategory(String category) {
//        return categoryCache.get(category.toLowerCase());
//    }
//
//    @Override
//    public void cacheProductsByBrand(String brand, List<Product> products) {
//        if (brand != null && products != null) {
//            brandCache.put(brand.toLowerCase(), new ArrayList<>(products));
//        }
//    }
//
//    @Override
//    public Optional<List<Product>> getCachedProductsByBrand(String brand) {
//        return brandCache.get(brand.toLowerCase());
//    }
//
//    @Override
//    public void clear() {
//        productCache.clear();
//        categoryCache.clear();
//        brandCache.clear();
//        System.out.println("Все кэши товаров очищены");
//    }
//
//    @Override
//    public Map<String, Object> getStats() {
//        Map<String, Object> stats = new HashMap<>();
//        stats.put("productCache", productCache.getStats());
//        stats.put("categoryCache", categoryCache.getStats());
//        stats.put("brandCache", brandCache.getStats());
//        stats.put("totalCachedProducts", productCache.size());
//        stats.put("totalCachedCategories", categoryCache.size());
//        stats.put("totalCachedBrands", brandCache.size());
//        return stats;
//    }
//
//    @Override
//    public void invalidateProduct(Long id) {
//        productCache.remove(id);
//        clearCategoryAndBrandCaches();
//    }
//
//    /**
//     * Очищает кэши категорий и брендов.
//     * Вызывается при изменении товаров, так как списки могли измениться.
//     */
//    private void clearCategoryAndBrandCaches() {
//        categoryCache.clear();
//        brandCache.clear();
//    }
//}

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
 * Реализация сервиса кэширования товаров с улучшенной логикой инвалидации.
 * Использует три отдельных кэша для разных типов данных о товарах с поддержкой TTL.
 * Обеспечивает умную инвалидацию кэша при изменениях товаров.
 *
 * @author idvavraz
 * @version 2.0
 */
public class ProductCacheServiceImpl implements ProductCacheService {

    /** Кэш товаров по идентификаторам */
    private final CacheService<Long, Product> productCache;

    /** Кэш товаров по категориям */
    private final CacheService<String, List<Product>> categoryCache;

    /** Кэш товаров по брендам */
    private final CacheService<String, List<Product>> brandCache;

    /** Кэш для результатов поисковых запросов */
    private final CacheService<String, List<Product>> searchCache;

    /**
     * Создает сервис кэширования с указанными кэшами.
     *
     * @param productCache кэш для товаров по ID
     * @param categoryCache кэш для товаров по категориям
     * @param brandCache кэш для товаров по брендам
     * @param searchCache кэш для результатов поиска
     * @throws IllegalArgumentException если любой из кэшей равен null
     */
    public ProductCacheServiceImpl(
            CacheService<Long, Product> productCache,
            CacheService<String, List<Product>> categoryCache,
            CacheService<String, List<Product>> brandCache,
            CacheService<String, List<Product>> searchCache) {

        if (productCache == null || categoryCache == null || brandCache == null || searchCache == null) {
            throw new IllegalArgumentException("Должны быть предоставлены все виды кэширования");
        }

        this.productCache = productCache;
        this.categoryCache = categoryCache;
        this.brandCache = brandCache;
        this.searchCache = searchCache;
    }

    @Override
    public void cacheProduct(Product product) {
        if (product != null && product.getId() != null) {
            productCache.put(product.getId(), product);
            updateCategoryAndBrandCaches(product);
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
            products.forEach(this::cacheProduct);
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
            products.forEach(this::cacheProduct);
        }
    }

    @Override
    public Optional<List<Product>> getCachedProductsByBrand(String brand) {
        return brandCache.get(brand.toLowerCase());
    }

    @Override
    public void cacheSearchResults(String searchKey, List<Product> products) {
        if (searchKey != null && products != null) {
            searchCache.put(searchKey.toLowerCase(), new ArrayList<>(products));
            products.forEach(this::cacheProduct);
        }
    }

    @Override
    public Optional<List<Product>> getCachedSearchResults(String searchKey) {
        return searchCache.get(searchKey.toLowerCase());
    }

    @Override
    public void clear() {
        productCache.clear();
        categoryCache.clear();
        brandCache.clear();
        searchCache.clear();
        System.out.println("Все кэши товаров очищены");
    }

    @Override
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("productCache", productCache.getStats());
        stats.put("categoryCache", categoryCache.getStats());
        stats.put("brandCache", brandCache.getStats());
        stats.put("searchCache", searchCache.getStats());
        stats.put("totalCachedProducts", getTotalCachedProducts());
        stats.put("totalCachedCategories", getTotalCachedCategories());
        stats.put("totalCachedBrands", getTotalCachedBrands());
        stats.put("totalCachedSearches", getTotalCachedSearches());
        return stats;
    }

    @Override
    public void invalidateProduct(Long id) {
        if (id == null) {
            return;
        }

        Optional<Product> productOpt = productCache.get(id);

        productCache.remove(id);

        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            categoryCache.remove(product.getCategory().toLowerCase());
            brandCache.remove(product.getBrand().toLowerCase());

            searchCache.clear();
        }
    }

    /**
     * Обновляет кэши категорий и брендов при добавлении товара.
     *
     * @param product товар для обновления кэшей
     */
    private void updateCategoryAndBrandCaches(Product product) {

        if (product.getCategory() != null) {
            categoryCache.remove(product.getCategory().toLowerCase());
        }
        if (product.getBrand() != null) {
            brandCache.remove(product.getBrand().toLowerCase());
        }
    }

    /**
     * Возвращает общее количество уникальных товаров в кэше.
     *
     * @return количество товаров в кэше
     */
    private int getTotalCachedProducts() {
        return productCache.size();
    }

    /**
     * Возвращает количество закэшированных категорий.
     *
     * @return количество категорий в кэше
     */
    private int getTotalCachedCategories() {
        return categoryCache.size();
    }

    /**
     * Возвращает количество закэшированных брендов.
     *
     * @return количество брендов в кэше
     */
    private int getTotalCachedBrands() {
        return brandCache.size();
    }

    /**
     * Возвращает количество закэшированных поисковых запросов.
     *
     * @return количество поисковых запросов в кэше
     */
    private int getTotalCachedSearches() {
        return searchCache.size();
    }
}