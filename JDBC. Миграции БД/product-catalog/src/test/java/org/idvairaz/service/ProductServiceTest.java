package org.idvairaz.service;

import org.idvairaz.model.Product;
import org.idvairaz.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
        testProduct = new Product("Test Product", "Test Description", 100.0,
                "TestCategory", "TestBrand", 10);
        testProduct.setId(1L);
    }

    /**
     * Тестирует успешное добавление товара.
     * Проверяет что товар сохраняется в репозитории и записывается метрика.
     * Примечание: очистка кэша происходит внутри, но не проверяется напрямую.
     */
    @Test
    void addProduct_WhenValidProduct_ShouldSaveAndReturnProduct() {
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        Product result = productService.addProduct(testProduct);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(productRepository).save(any(Product.class));
        verify(metricsService).recordOperation(eq("ДОБАВИТЬ_ТОВАР"), any());

    }

    /**
     * Тестирует что кэш сбрасывается при добавлении нового товара.
     */
    @Test
    void addProduct_ShouldClearBrandCache() {
        List<Product> products = List.of(testProduct);
        when(productRepository.findByBrand("TestBrand")).thenReturn(products);
        productService.getProductsByBrand("TestBrand");

        when(productRepository.save(any(Product.class))).thenReturn(testProduct);
        productService.addProduct(testProduct);

        productService.getProductsByBrand("TestBrand");
        /** кэш очищен, происходит второй вызов к репозиторию */
        verify(productRepository, times(2)).findByBrand("TestBrand");
    }

    /**
     * Тестирует поиск товара по ID когда товар существует.
     * Проверяет что возвращается непустой Optional с правильным товаром.
     */
    @Test
    void getProductById_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getProductById(1L);

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Product");
        verify(productRepository).findById(1L);
    }

    /**
     * Тестирует поиск товара по ID когда товар не существует.
     * Проверяет что возвращается пустой Optional.
     */
    @Test
    void getProductById_WhenProductNotExists_ShouldReturnEmpty() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Product> result = productService.getProductById(999L);

        assertThat(result).isEmpty();
    }

    /**
     * Тестирует поиск товара по имени когда товар существует.
     * Проверяет корректность поиска по точному совпадению имени.
     */
    @Test
    void getProductByName_WhenProductExists_ShouldReturnProduct() {
        when(productRepository.findByName("Test Product")).thenReturn(Optional.of(testProduct));

        Optional<Product> result = productService.getProductByName("Test Product");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Product");
    }

    /**
     * Тестирует получение всех товаров.
     * Проверяет что возвращается полный список товаров из репозитория.
     */
    @Test
    void getAllProducts_ShouldReturnAllProducts() {
        List<Product> products = List.of(testProduct);
        when(productRepository.findAll()).thenReturn(products);

        List<Product> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Test Product");
    }

    /**
     * Тестирует удаление товара.
     * Проверяет что вызывается метод удаления репозитория и записывается метрика.
     */
    @Test
    void deleteProduct_ShouldCallRepositoryDelete() {
        productService.deleteProduct(1L);

        verify(productRepository).delete(1L);
        verify(metricsService).recordOperation(eq("УДАЛИТЬ_ТОВАР"), any());
    }

    /**
     * Тестирует поиск товаров по категории.
     * Проверяет что возвращаются только товары указанной категории.
     */
    @Test
    void getProductsByCategory_ShouldReturnFilteredProducts() {
        List<Product> electronicsProducts = List.of(testProduct);
        when(productRepository.findByCategory("TestCategory")).thenReturn(electronicsProducts);

        List<Product> result = productService.getProductsByCategory("TestCategory");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("TestCategory");
    }

    /**
     * Тестирует поиск товаров по бренду.
     * Проверяет что возвращаются только товары указанного бренда.
     */
    @Test
    void getProductsByBrand_ShouldReturnFilteredProducts() {
        List<Product> brandProducts = List.of(testProduct);
        when(productRepository.findByBrand("TestBrand")).thenReturn(brandProducts);

        List<Product> result = productService.getProductsByBrand("TestBrand");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrand()).isEqualTo("TestBrand");
    }

    /**
     * Тестирует успешное обновление товара.
     * Проверяет что товар обновляется при валидных данных.
     */
    @Test
    void updateProduct_WhenValidData_ShouldUpdateProduct() {
        Product updatedProduct = new Product("Updated Product", "Updated Description",
                150.0, "Electronics", "TestBrand", 5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.findByName("Updated Product")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        Product result = productService.updateProduct(1L, updatedProduct);

        assertThat(result.getName()).isEqualTo("Updated Product");
        assertThat(result.getPrice()).isEqualTo(150.0);
        verify(productRepository).save(any(Product.class));
    }

    /**
     * Тестирует обновление товара когда товар не найден.
     * Проверяет что выбрасывается исключение с правильным сообщением.
     */
    @Test
    void updateProduct_WhenProductNotFound_ShouldThrowException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());
        Product updatedProduct = new Product("Updated", "Desc", 150.0, "Cat", "Brand", 5);

        assertThatThrownBy(() -> productService.updateProduct(999L, updatedProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("не найден");
    }

    /**
     * Тестирует обновление товара с неуникальным именем.
     * Проверяет что выбрасывается исключение при попытке использовать существующее имя.
     */
    @Test
    void updateProduct_WhenNameNotUnique_ShouldThrowException() {
        /** Given - настраиваем моки для существующего товара и конфликтующего имени */
        Product existingProduct = new Product("Existing Product", "Desc", 100.0, "Cat", "Brand", 5);
        existingProduct.setId(1L);

        Product anotherProduct = new Product("Another Product", "Desc", 200.0, "Cat", "Brand", 10);
        anotherProduct.setId(2L);

        Product updatedProduct = new Product("Another Product", "Updated", 150.0, "Cat", "Brand", 8);

        when(productRepository.findById(1L)).thenReturn(Optional.of(existingProduct));
        when(productRepository.findByName("Another Product")).thenReturn(Optional.of(anotherProduct));

        /**  When & Then - проверяем что выбрасывается исключение из-за неуникального имени */
        assertThatThrownBy(() -> productService.updateProduct(1L, updatedProduct))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("уже существует");
    }

    /**
     * Тестирует кэширование при поиске по ID.
     * Проверяет что репозиторий вызывается только один раз при повторных запросах.
     */
    @Test
    void getProductById_ShouldCallRepositoryOnlyOnce_WhenSameIdRequestedTwice() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        Optional<Product> result1 = productService.getProductById(1L);
        Optional<Product> result2 = productService.getProductById(1L);

        verify(productRepository, times(1)).findById(1L);
        assertThat(result1).isPresent();
        assertThat(result2).isPresent();
    }

    /**
     * Тестирует кэширование при поиске по категории.
     * Проверяет что репозиторий вызывается только один раз при повторных запросах.
     */
    @Test
    void getProductsByCategory_ShouldUseCacheOnSecondCall() {
        List<Product> products = List.of(testProduct);
        when(productRepository.findByCategory("TestCategory")).thenReturn(products);

        List<Product> result1 = productService.getProductsByCategory("TestCategory");
        List<Product> result2 = productService.getProductsByCategory("TestCategory");

        verify(productRepository, times(1)).findByCategory("TestCategory");
        assertThat(result1).hasSize(1);
        assertThat(result2).hasSize(1);
    }

    /**
     * Тестирует кэширование при поиске по бренду с проверкой вызовов репозитория.
     * Проверяет что репозиторий вызывается только один раз при повторных запросах.
     */
    @Test
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