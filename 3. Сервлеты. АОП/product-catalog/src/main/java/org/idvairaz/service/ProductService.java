package org.idvairaz.service;

import lombok.AllArgsConstructor;
import org.idvairaz.aspect.Auditable;
import org.idvairaz.cache.ProductCacheService;
import org.idvairaz.model.Product;
import org.idvairaz.repository.ProductRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Сервис для управления товарами в каталоге.
 * Предоставляет бизнес-логику для операций с товарами, включая кэширование и метрики.
 *
 * @author idvavraz
 * @version 2.0
 */
@AllArgsConstructor
public class ProductService {

    /** Репозиторий для работы с товарами */
    private final ProductRepository productRepository;

    /** Сервис для сбора метрик производительности */
    private final MetricsService metricsService;

    /** Сервис для кэширования товаров */
    private final ProductCacheService cacheService;

    /**
     * Добавляет новый товар в каталог.
     * Очищает кэш после добавления.
     *
     * @param product товар для добавления
     * @return сохраненный товар с присвоенным ID
     */
    @Auditable("ДОБАВЛЕНИЕ_ТОВАРА")
    public Product addProduct(Product product) {
        LocalDateTime start = LocalDateTime.now();

        Product savedProduct = productRepository.save(product);
        cacheService.clear();
        metricsService.recordOperation("ДОБАВИТЬ_ТОВАР", Duration.between(start, LocalDateTime.now()));

        return savedProduct;
    }

    /**
     * Находит товар по его идентификатору.
     * Сначала проверяет кэш, если нет - обращается к базе данных.
     *
     * @param id идентификатор товара
     * @return Optional с найденным товаром или empty если товар не найден
     */
    @Auditable("ПОИСК_ТОВАРА_ПО_ID")
    public Optional<Product> getProductById(Long id) {
        LocalDateTime start = LocalDateTime.now();

        Optional<Product> cachedProduct = cacheService.getCachedProduct(id);
        if (cachedProduct.isPresent()) {
            System.out.println("Данные из кэша (ID: " + id + ")");
            metricsService.recordOperation("ПОИСК_ПО_ID", Duration.between(start, LocalDateTime.now()));

            return cachedProduct;
        }

        Optional<Product> product = productRepository.findById(id);
        product.ifPresent(cacheService::cacheProduct);

        return product;
    }

    /**
     * Удаляет товар из каталога.
     * Очищает кэш после удаления.
     *
     * @param id идентификатор товара для удаления
     */
    @Auditable("УДАЛЕНИЕ_ТОВАРА")
    public void deleteProduct(Long id) {
        LocalDateTime start = LocalDateTime.now();

        productRepository.delete(id);
        cacheService.invalidateProduct(id);
        metricsService.recordOperation("УДАЛИТЬ_ТОВАР", Duration.between(start, LocalDateTime.now()));

    }

    /**
     * Находит товар по точному совпадению названия.
     * Поиск выполняется без учета регистра.
     *
     * @param name название товара для поиска
     * @return Optional с найденным товаром или empty если товар не найден
     */
    @Auditable("ПОИСК_ТОВАРА_ПО_ИМЕНИ")
    public Optional<Product> getProductByName(String name) {
        LocalDateTime start = LocalDateTime.now();

        Optional<Product> product = productRepository.findByName(name);
        metricsService.recordOperation("ПОИСК_ПО_ИМЕНИ", Duration.between(start, LocalDateTime.now()));

        return product;
    }


    /**
     * Находит все товары указанной категории.
     * Использует кэширование для ускорения повторных запросов.
     *
     * @param category категория для поиска
     * @return список товаров в указанной категории
     */
    @Auditable("ПОИСК_ТОВАРОВ_ПО_КАТЕГОРИИ")
    public List<Product> getProductsByCategory(String category) {
        LocalDateTime start = LocalDateTime.now();

        Optional<List<Product>> cachedProducts = cacheService.getCachedProductsByCategory(category);
        if (cachedProducts.isPresent()) {
            System.out.println("Данные из кэша (категория: " + category + ")");
            metricsService.recordOperation("ПОИСК_ПО_КАТЕГОРИИ", Duration.between(start, LocalDateTime.now()));

            return new ArrayList<>(cachedProducts.get());
        }

        List<Product> products = productRepository.findByCategory(category);
        cacheService.cacheProductsByCategory(category, products);
        metricsService.recordOperation("ПОИСК_ПО_КАТЕГОРИИ", Duration.between(start, LocalDateTime.now()));

        return products;
    }

    /**
     * Находит все товары указанного бренда.
     * Использует кэширование для ускорения повторных запросов.
     *
     * @param brand бренд для поиска
     * @return список товаров указанного бренда
     */
    @Auditable("ПОИСК_ТОВАРОВ_ПО_БРЕНДУ")
    public List<Product> getProductsByBrand(String brand) {
        LocalDateTime start = LocalDateTime.now();

        Optional<List<Product>> cachedProducts = cacheService.getCachedProductsByBrand(brand);
        if (cachedProducts.isPresent()) {
            System.out.println("Данные из кэша (бренд: " + brand + ")");
            metricsService.recordOperation("ПОИСК_ПО_БРЕНДУ", Duration.between(start, LocalDateTime.now()));
            return new ArrayList<>(cachedProducts.get());
        }

        List<Product> products = productRepository.findByBrand(brand);
        cacheService.cacheProductsByBrand(brand, products);

        return products;
    }

    /**
     * Обновляет существующий товар.
     * Очищает кэш после обновления.
     *
     * @param id идентификатор товара для обновления
     * @param updatedProduct обновленные данные товара
     * @return обновленный товар
     */
    @Auditable("ОБНОВЛЕНИЕ_ТОВАРА")
    public Product updateProduct(Long id, Product updatedProduct) {
        LocalDateTime start = LocalDateTime.now();

        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isEmpty()) {
            throw new IllegalArgumentException("Товар с ID " + id + " не найден");
        }

        if (!existingProduct.get().getName().equals(updatedProduct.getName())) {
            Optional<Product> productWithSameName = productRepository.findByName(updatedProduct.getName());
            if (productWithSameName.isPresent()) {
                throw new IllegalArgumentException("Товар с именем '" + updatedProduct.getName() + "' уже существует");
            }
        }

        updatedProduct.setId(id);
        updatedProduct.setUpdatedAt(LocalDateTime.now());

        Product savedProduct = productRepository.save(updatedProduct);
        cacheService.invalidateProduct(id);
        metricsService.recordOperation("ОБНОВИТЬ_ТОВАР", Duration.between(start, LocalDateTime.now()));

        return savedProduct;
    }

    /**
     * Возвращает список всех товаров в каталоге.
     *
     * @return список всех товаров
     */
    @Auditable("ПОЛУЧЕНИЕ_ВСЕХ_ТОВАРОВ")
    public List<Product> getAllProducts() {
        LocalDateTime start = LocalDateTime.now();

        List<Product> products = productRepository.findAll();
        metricsService.recordOperation("ПОЛУЧЕНИЕ_ВСЕХ_ТОВАРОВ", Duration.between(start, LocalDateTime.now()));

        return products;
    }

    /**
     * Выводит статистику кэширования в консоль.
     * Включает информацию о размерах кэшей, эффективности попаданий и детали по категориям и брендам.
     */
    public void printCacheStats() {
        System.out.println("\n=== СТАТИСТИКА КЭША ===");
        Map<String, Object> stats = cacheService.getStats();

        System.out.printf("""
            Основные метрики:
               - Товаров в кэше: %d
               - Категорий в кэше: %d
               - Брендов в кэше: %d
            """,
                stats.get("totalCachedProducts"),
                stats.get("totalCachedCategories"),
                stats.get("totalCachedBrands"));

        Map<String, Object> productStats = (Map<String, Object>) stats.get("productCache");
        int hits = (Integer) productStats.get("hits");
        int misses = (Integer) productStats.get("misses");
        int totalRequests = hits + misses;
        if (totalRequests > 0) {
            double hitRate = (double) hits / totalRequests * 100;
            System.out.printf("""
                    Эффективность кэширования:
                       - Попадания в кэш: %d
                       - Промахи кэша: %d
                       - Всего запросов: %d
                       - Эффективность: %.1f%%
                    """,  hits, misses, totalRequests, hitRate);
        } else {
            System.out.println("   - Запросов к кэшу еще не было");
        }
    }

    /**
     * Выводит общую статистику товаров в консоль.
     */
    public void printProductStats() {
        System.out.printf("""
            === СТАТИСТИКА ТОВАРОВ ===
            Общая статистика:
             - Всего товаров: %d
             - Категорий: %d
             - Брендов: %d
            """,
                productRepository.getTotalProductsCount(),
                productRepository.getTotalCategoriesCount(),
                productRepository.getTotalBrandsCount());
    }
}
