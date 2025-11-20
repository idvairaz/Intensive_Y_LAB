package org.idvairaz.repository;

import org.idvairaz.BaseRepositoryTest;
import org.idvairaz.model.Product;
import org.idvairaz.repository.impl.PostgresProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Интеграционные тесты для PostgresProductRepository с использованием Testcontainers.
 * Проверяет основные операции с товарами в реальной базе данных.
 *
 * @author idvavraz
 * @version 1.0
 */
class ProductRepositoryTest extends BaseRepositoryTest {

    private PostgresProductRepository productRepository;

    @BeforeEach
    void setUp() {
        productRepository = new PostgresProductRepository();
        cleanDatabase();
    }

    private void cleanDatabase() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("DELETE FROM products");
            System.out.println("Таблица products очищена перед тестом");
        } catch (SQLException e) {
            if (e.getMessage().contains("relation \"products\" does not exist")) {
                System.out.println("Таблица 'products' еще не существует, пропускаем очистку\"");
                return;
            }
            throw new RuntimeException("Ошибка при очистке базы данных", e);
        }
    }

    @Test
    void shouldSaveAndFindProduct() {
        Product product = Product.builder()
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .category("Electronics")
                .brand("TestBrand")
                .stockQuantity(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Product savedProduct = productRepository.save(product);
        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());

        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Test Product");
        assertThat(foundProduct.get().getPrice()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    void shouldFindProductsByCategory() {
        String uniqueCategory = "Books_" + System.currentTimeMillis();

        Product product1 = Product.builder()
                .name("Product 1")
                .description("Desc 1")
                .price(new BigDecimal("100.00"))
                .category(uniqueCategory)
                .brand("Brand A")
                .stockQuantity(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Product product2 = Product.builder()
                .name("Product 2")
                .description("Desc 2")
                .price(new BigDecimal("200.00"))
                .category(uniqueCategory)
                .brand("Brand B")
                .stockQuantity(10)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRepository.save(product1);
        productRepository.save(product2);

        List<Product> books = productRepository.findByCategory(uniqueCategory);

        assertThat(books).hasSize(2);
        assertThat(books).extracting(Product::getCategory).containsOnly(uniqueCategory);
    }

    @Test
    void shouldFindProductByName() {
        Product product = Product.builder()
                .name("Unique Product Name")
                .description("Description")
                .price(new BigDecimal("50.00"))
                .category("Category")
                .brand("Brand")
                .stockQuantity(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRepository.save(product);

        Optional<Product> foundProduct = productRepository.findByName("Unique Product Name");

        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Unique Product Name");
    }

    @Test
    void shouldUpdateProduct() {
        Product product = Product.builder()
                .name("Old Name")
                .description("Old Description")
                .price(new BigDecimal("50.00"))
                .category("Old Category")
                .brand("Old Brand")
                .stockQuantity(5)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Product savedProduct = productRepository.save(product);

        savedProduct.setName("New Name");
        savedProduct.setPrice(new BigDecimal("75.00"));
        productRepository.save(savedProduct);

        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("New Name");
        assertThat(foundProduct.get().getPrice()).isEqualTo(new BigDecimal("75.00"));
    }

    @Test
    void shouldDeleteProduct() {
        Product product = Product.builder()
                .name("Product to Delete")
                .description("Description")
                .price(new BigDecimal("25.00"))
                .category("Category")
                .brand("Brand")
                .stockQuantity(3)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        Product savedProduct = productRepository.save(product);

        productRepository.delete(savedProduct.getId());

        Optional<Product> foundProduct = productRepository.findById(savedProduct.getId());
        assertThat(foundProduct).isEmpty();
    }
    @Test
    void shouldReturnAllProducts() {
        Product product1 = Product.builder()
                .name("Product A")
                .description("Desc A")
                .price(new BigDecimal("10.00"))
                .category("Category A")
                .brand("Brand A")
                .stockQuantity(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Product product2 = Product.builder()
                .name("Product B")
                .description("Desc B")
                .price(new BigDecimal("20.00"))
                .category("Category B")
                .brand("Brand B")
                .stockQuantity(2)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        productRepository.save(product1);
        productRepository.save(product2);

        List<Product> allProducts = productRepository.findAll();

        assertThat(allProducts).hasSizeGreaterThanOrEqualTo(2);
    }
    @Test
    void shouldGetTotalProductsCount() {
        Product product = Product.builder()
                .name("Count Test Product")
                .description("Description")
                .price(new BigDecimal("15.00"))
                .category("Category")
                .brand("Brand")
                .stockQuantity(1)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        productRepository.save(product);

        int count = productRepository.getTotalProductsCount();

        assertThat(count).isGreaterThan(0);
    }
}
