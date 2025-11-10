package org.idvairaz.repository;

import org.idvairaz.io.ProductDataManager;
import org.idvairaz.model.Product;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * In-memory реализация репозитория товаров.
 * Хранит данные в HashMap с автоматической сериализацией в файл.
 *
 * @author idvavraz
 * @version 1.0
 */
public class InMemoryProductRepository implements ProductRepository {
    private final Map<Long, Product> products = new HashMap<>();
    private Long nextId = 1L;
    private final ProductDataManager dataManager;

    /**
     * Конструктор репозитория.
     * Автоматически загружает данные из файла при создании.
     */
    public InMemoryProductRepository() {
        this.dataManager = new ProductDataManager();
        loadProductsFromFile();
    }

    /**
     * Загружает товары из файла при инициализации.
     */
    private void loadProductsFromFile() {
        List<Product> loadedProducts = dataManager.loadProducts();
        if (!loadedProducts.isEmpty()) {
            for (Product product : loadedProducts) {
                products.put(product.getId(), product);
                if (product.getId() >= nextId) {
                    nextId = product.getId() + 1;
                }
            }
        }
    }

    /**
     * Сохраняет текущее состояние товаров в файл.
     */
    private void saveProductsToFile() {
        dataManager.saveProducts(new ArrayList<>(products.values()));
    }

    /**
     * Возвращает статистику по товарам.
     * Включает общее количество товаров, категорий и брендов.
     *
     * @return карта со статистическими данными
     */
    public Map<String, Integer> getProductStats() {
        Map<String, Integer> stats = new HashMap<>();
        stats.put("totalProducts", products.size());

        /** Статистика по категориям */
        Map<String, Long> categoryCount = products.values().stream()
                .collect(Collectors.groupingBy(Product::getCategory, Collectors.counting()));
        stats.put("totalCategories", categoryCount.size());

        /**Статистика по брендам */
        Map<String, Long> brandCount = products.values().stream()
                .collect(Collectors.groupingBy(Product::getBrand, Collectors.counting()));
        stats.put("totalBrands", brandCount.size());

        return stats;
    }

    /** Все методы интерфейса уже описаны в интерфейсе */
    @Override
    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(nextId++);
            product.setCreatedAt(java.time.LocalDateTime.now());
        }
        product.setUpdatedAt(java.time.LocalDateTime.now());
        products.put(product.getId(), product);

        saveProductsToFile();

        return product;
    }

    @Override
    public Optional<Product> findById(Long id) {
        return Optional.ofNullable(products.get(id));
    }

    @Override
    public Optional<Product> findByName(String name) {
        return products.values().stream()
                .filter(product -> product.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    @Override
    public List<Product> findAll() {
        return new ArrayList<>(products.values());
    }

    @Override
    public void delete(Long id) {
        products.remove(id);
    }

    @Override
    public List<Product> findByCategory(String category) {
        return products.values().stream()
                .filter(product -> category.equalsIgnoreCase(product.getCategory()))
                .collect(Collectors.toList());
    }

    @Override
    public List<Product> findByBrand(String brand) {
        return products.values().stream()
                .filter(product -> brand.equalsIgnoreCase(product.getBrand()))
                .collect(Collectors.toList());
    }
}
