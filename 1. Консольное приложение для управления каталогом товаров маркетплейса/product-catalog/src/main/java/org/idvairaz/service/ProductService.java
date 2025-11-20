package org.idvairaz.service;

import org.idvairaz.model.Product;
import org.idvairaz.repository.InMemoryProductRepository;
import org.idvairaz.repository.ProductRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

/**
 * Сервис для управления товарами в каталоге.
 * Предоставляет бизнес-логику для операций с товарами, включая кэширование и метрики.
 *
 * @author idvavraz
 * @version 1.0
 */
public class ProductService {
    private final ProductRepository productRepository;
    private final MetricsService metricsService;


    /** Кэш для часто запрашиваемых данных */
    private final Map<Long, Product> productCache = new HashMap<>();
    private final Map<String, List<Product>> categoryCache = new HashMap<>();
    private final Map<String, List<Product>> brandCache = new HashMap<>();

    /** Счетчики для метрик кэша*/
    private int cacheHits = 0;
    private int cacheMisses = 0;

    /**
     * Конструктор сервиса товаров.
     *
     * @param productRepository репозиторий для доступа к данным товаров
     * @param metricsService сервис для сбора метрик производительности
     */
    public ProductService(ProductRepository productRepository, MetricsService metricsService) {
        this.productRepository = productRepository;
        this.metricsService = metricsService;
    }

    /**
     * Добавляет новый товар в каталог.
     * Выполняет валидацию данных и очищает кэш после добавления.
     *
     * @param product товар для добавления
     * @return сохраненный товар с присвоенным ID
     */
    public Product addProduct(Product product) {
        LocalDateTime start = LocalDateTime.now();

        try {
            Product savedProduct = productRepository.save(product);
            clearCache();
            metricsService.recordOperation("ДОБАВИТЬ_ТОВАР", Duration.between(start, LocalDateTime.now()));
            return savedProduct;
        } catch (Exception e) {
            metricsService.recordOperation("ОШИБКА_ДОБАВЛЕНИЯ_ТОВАРА", Duration.between(start, LocalDateTime.now()));
            throw e;
        }
    }

    /**
     * Находит товар по его идентификатору.
     * Использует кэширование для ускорения повторных запросов.
     *
     * @param id идентификатор товара
     * @return Optional с найденным товаром или empty если товар не найден
     */
    public Optional<Product> getProductById(Long id) {
        LocalDateTime start = LocalDateTime.now();

        if (productCache.containsKey(id)) {
            cacheHits++;
            System.out.println("Данные из кэша (ID: " + id + ")");
            metricsService.recordOperation("ПОИСК_ПО_ID", Duration.between(start, LocalDateTime.now()));
            return Optional.of(productCache.get(id));
        }
        cacheMisses++;
        Optional<Product> product = productRepository.findById(id);
        product.ifPresent(p -> productCache.put(id, p));

        metricsService.recordOperation("ПОИСК_ПО_ID", Duration.between(start, LocalDateTime.now()));
        return product;
    }

    /**
     * Находит товар по точному совпадению названия.
     * Поиск выполняется без учета регистра.
     *
     * @param name название товара для поиска
     * @return Optional с найденным товаром или empty если товар не найден
     */
    public Optional<Product> getProductByName(String name) {
        LocalDateTime start = LocalDateTime.now();
        Optional<Product> product = productRepository.findByName(name);

        metricsService.recordOperation("ПОИСК_ПО_ИМЕНИ", Duration.between(start, LocalDateTime.now()));
        return product;
    }

    /**
     * Возвращает список всех товаров в каталоге.
     *
     * @return список всех товаров
     */
    public List<Product> getAllProducts() {
        LocalDateTime start = LocalDateTime.now();
        List<Product> products = productRepository.findAll();

        metricsService.recordOperation("ПОЛУЧЕНИЕ_ВСЕХ_ТОВАРОВ", Duration.between(start, LocalDateTime.now()));
        return products;
    }

    /**
     * Удаляет товар из каталога по идентификатору.
     * Очищает кэш после удаления.
     *
     * @param id идентификатор товара для удаления
     */
    public void deleteProduct(Long id) {
        LocalDateTime start = LocalDateTime.now();

        productRepository.delete(id);
        productCache.remove(id);
        clearCache();

        metricsService.recordOperation("УДАЛИТЬ_ТОВАР", Duration.between(start, LocalDateTime.now()));
    }

    /**
     * Находит все товары указанной категории.
     * Использует кэширование для ускорения повторных запросов по той же категории.
     *
     * @param category категория для поиска
     * @return список товаров в указанной категории
     */
    public List<Product> getProductsByCategory(String category) {
        LocalDateTime start = LocalDateTime.now();

        if (categoryCache.containsKey(category)) {
            cacheHits++;
            System.out.println("Данные из кэша (категория: " + category + ")");
            metricsService.recordOperation("ПОИСК_ПО_КАТЕГОРИИ", Duration.between(start, LocalDateTime.now()));

            return new ArrayList<>(categoryCache.get(category));
        }

        cacheMisses++;
        List<Product> products = productRepository.findByCategory(category);
        categoryCache.put(category, new ArrayList<>(products));
        metricsService.recordOperation("ПОИСК_ПО_КАТЕГОРИИ", Duration.between(start, LocalDateTime.now()));

        return products;
    }

    /**
     * Находит все товары указанного бренда.
     * Использует кэширование для ускорения повторных запросов по тому же бренду.
     *
     * @param brand бренд для поиска
     * @return список товаров указанного бренда
     */
    public List<Product> getProductsByBrand(String brand) {
        LocalDateTime start = LocalDateTime.now();

        if (brandCache.containsKey(brand)) {
            cacheHits++;
            System.out.println("Данные из кэша (бренд: " + brand + ")");
            metricsService.recordOperation("ПОИСК_ПО_БРЕНДУ", Duration.between(start, LocalDateTime.now()));

            return new ArrayList<>(brandCache.get(brand));
        }

        cacheMisses++;
        List<Product> products = productRepository.findByBrand(brand);
        brandCache.put(brand, new ArrayList<>(products));
        metricsService.recordOperation("ПОИСК_ПО_БРЕНДУ", Duration.between(start, LocalDateTime.now()));

        return products;
    }

    /**
     * Обновляет существующий товар.
     * Проверяет уникальность названия и обновляет временные метки.
     *
     * @param id идентификатор товара для обновления
     * @param updatedProduct обновленные данные товара
     * @return обновленный товар
     * @throws IllegalArgumentException если товар не найден или название не уникально
     */
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
        clearCache();
        metricsService.recordOperation("ОБНОВИТЬ_ТОВАР", Duration.between(start, LocalDateTime.now()));

        return savedProduct;
    }

    /**
     * Выводит статистику кэширования в консоль.
     * Включает информацию о размерах кэшей, эффективности попаданий и детали по категориям и брендам.
     */
    public void printCacheStats() {
        System.out.println("\n=== СТАТИСТИКА КЭША ===");
        System.out.println("Основные метрики:");
        System.out.println("   - Товаров в кэше: " + productCache.size());
        System.out.println("   - Категорий в кэше: " + categoryCache.size());
        System.out.println("   - Брендов в кэше: " + brandCache.size());

        System.out.println("Эффективность кэширования:");
        int totalRequests = cacheHits + cacheMisses;
        if (totalRequests > 0) {
            double hitRate = (double) cacheHits / totalRequests * 100;
            System.out.println("   - Попадания в кэш: " + cacheHits);
            System.out.println("   - Промахи кэша: " + cacheMisses);
            System.out.println("   - Эффективность: " + String.format("%.1f", hitRate) + "%");
        } else {
            System.out.println("   - Запросов к кэшу еще не было");
        }

        if (!categoryCache.isEmpty()) {
            System.out.println("Закэшированные категории:");
            categoryCache.forEach((category, products) -> {
                System.out.println("   - " + category + ": " + products.size() + " товаров");
            });
        }

        if (!brandCache.isEmpty()) {
            System.out.println("Закэшированные бренды:");
            brandCache.forEach((brand, products) -> {
                System.out.println("   - " + brand + ": " + products.size() + " товаров");
            });
        }
    }

    /**
     * Очищает все кэши при изменениях данных.
     * Вызывается автоматически при добавлении, обновлении или удалении товаров.
     */
    private void clearCache() {
        productCache.clear();
        categoryCache.clear();
        brandCache.clear();
        System.out.println("Кэш очищен");
    }

    /**
     * Выводит общую статистику товаров в консоль.
     * Включает информацию о количестве товаров, категорий и брендов.
     */
    public void printProductStats() {
        Map<String, Integer> stats = ((InMemoryProductRepository) productRepository).getProductStats();
        System.out.println("\n=== СТАТИСТИКА ТОВАРОВ ===");
        System.out.println("Общая статистика:");
        System.out.println(" - Всего товаров: " + stats.get("totalProducts"));
        System.out.println(" - Категорий: " + stats.get("totalCategories"));
        System.out.println(" - Брендов: " + stats.get("totalBrands"));
    }
}
