package org.idvairaz.service;

import org.idvairaz.model.Product;
import org.idvairaz.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import java.math.BigDecimal;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@DisplayName("Тесты сервиса товаров")
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MetricsService metricsService;

    private ProductService productService;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        productService = new ProductService(productRepository, metricsService);
        testProduct = new Product("Test Product", "Test Description", new BigDecimal("100.0"),
                "TestCategory", "TestBrand", 10);
        testProduct.setId(1L);
    }

    @Test
    @DisplayName("Должен успешно добавить товар и вернуть его с ID")
    void addProduct_WhenValidProduct_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.addProduct(testProduct);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(productRepository).save(any(Product.class));
        verify(metricsService).recordOperation(eq("ДОБАВИТЬ_ТОВАР"), any());
    }

    @Test
    @DisplayName("Должен очищать кэш брендов при добавлении нового товара")
    void addProduct_ShouldClearBrandCache() {
        List<Product> products = List.of(testProduct);
        when(productRepository.findByBrand("TestBrand")).thenReturn(products);
        productService.getProductsByBrand("TestBrand");

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        productService.addProduct(testProduct);

        productService.getProductsByBrand("TestBrand");
        verify(productRepository, times(2)).findByBrand("TestBrand");
    }

    @Test
    @DisplayName("Должен вернуть товар при поиске по существующему ID")
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getProductById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Product");
        verify(productRepository).findById(1L);
        verifyNoMoreInteractions(productRepository);
        verifyNoMoreInteractions(metricsService);
    }

    @Test
    @DisplayName("Должен вернуть пустой результат при поиске по несуществующему ID")
    void getProductById_WhenProductNotExists_ShouldReturnEmpty() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть товар при поиске по существующему имени")
    void getProductByName_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getProductByName("Test Product");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("Должен вернуть все товары из репозитория")
    void getAllProducts_ShouldReturnAllProducts() {
        List<Product> products = List.of(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Product");
    }

    @Test
    @DisplayName("Должен удалить товар и записать метрику")
    void deleteProduct_ShouldCallRepositoryDelete() {
        productService.deleteProduct(1L);

        verify(productRepository).delete(1L);
        verify(metricsService).recordOperation(eq("УДАЛИТЬ_ТОВАР"), any());
    }

    @Test
    @DisplayName("Должен вернуть товары по категории")
    void getProductsByCategory_ShouldReturnFilteredProducts() {
        List<Product> electronicsProducts = List.of(testProduct);
        when(productRepository.findByCategory("TestCategory")).thenReturn(electronicsProducts);

        List<Product> result = productService.getProductsByCategory("TestCategory");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("TestCategory");
    }

    @Test
    @DisplayName("Должен вернуть товары по бренду")
    void getProductsByBrand_ShouldReturnFilteredProducts() {
        List<Product> brandProducts = List.of(testProduct);
        when(productRepository.findByBrand("TestBrand")).thenReturn(brandProducts);

        List<Product> result = productService.getProductsByBrand("TestBrand");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrand()).isEqualTo("TestBrand");
    }

    @Test
    @DisplayName("Должен успешно обновить товар при валидных данных")
    void updateProduct_WhenValidData_ShouldUpdateProduct() {
        Product updatedProduct = new Product("Updated Product", "Updated Description",
                new BigDecimal("150.0"), "Electronics", "TestBrand", 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.findByName("Updated Product")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(1L, updatedProduct);

        assertThat(result.getName()).isEqualTo("Updated Product");
        assertThat(result.getPrice()).isEqualTo(new BigDecimal("150.0"));
        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении несуществующего товара")
    void updateProduct_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        Product updatedProduct = new Product("Updated", "Desc", new BigDecimal("150.0"), "Cat", "Brand", 5);

        assertThatThrownBy(() -> productService.updateProduct(999L, updatedProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    @DisplayName("Должен выбросить исключение при обновлении с неуникальным именем")
    void updateProduct_WhenNameNotUnique_ShouldThrowException() {
        Product existingProduct = new Product("Existing Product", "Desc", new BigDecimal("100.0"), "Cat", "Brand", 5);
        existingProduct.setId(1L);

        Product anotherProduct = new Product("Another Product", "Desc", new BigDecimal("200.0"), "Cat", "Brand", 10);
        anotherProduct.setId(2L);

        Product updatedProduct = new Product("Another Product", "Updated", new BigDecimal("150.0"), "Cat", "Brand", 8);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.findByName("Another Product")).thenReturn(Optional.of(anotherProduct));

        assertThatThrownBy(() -> productService.updateProduct(1L, updatedProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    @DisplayName("Должен использовать кэш при повторном поиске по ID")
    void getProductById_ShouldCallRepositoryOnlyOnce_WhenSameIdRequestedTwice() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result1 = productService.getProductById(1L);
        Optional<Product> result2 = productService.getProductById(1L);

        verify(productRepository, times(1)).findById(1L);
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
    }

    @Test
    @DisplayName("Должен использовать кэш при повторном поиске по категории")
    void getProductsByCategory_ShouldUseCacheOnSecondCall() {
        List<Product> products = List.of(testProduct);
        when(productRepository.findByCategory("TestCategory")).thenReturn(products);

        List<Product> result1 = productService.getProductsByCategory("TestCategory");
        List<Product> result2 = productService.getProductsByCategory("TestCategory");

        verify(productRepository, times(1)).findByCategory("TestCategory");
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1);
    }

    @Test
    @DisplayName("Должен использовать кэш при повторном поиске по бренду")
    void getProductsByBrand_ShouldUseCacheOnSecondCall() {
        List<Product> products = List.of(testProduct);
        when(productRepository.findByBrand("TestBrand")).thenReturn(products);

        List<Product> result1 = productService.getProductsByBrand("TestBrand");
        List<Product> result2 = productService.getProductsByBrand("TestBrand");

        verify(productRepository, times(1)).findByBrand("TestBrand");
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1);
    }
}