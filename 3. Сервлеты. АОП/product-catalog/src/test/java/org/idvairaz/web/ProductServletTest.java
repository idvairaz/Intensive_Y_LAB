package org.idvairaz.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.idvairaz.dto.CreateProductDTO;
import org.idvairaz.dto.ProductDTO;
import org.idvairaz.mapper.ProductMapper;
import org.idvairaz.model.Product;
import org.idvairaz.service.ProductService;
import org.instancio.Instancio;
import static org.instancio.Select.field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.assertj.core.api.SoftAssertions;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProductServletTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    private ProductServlet productServlet;
    private ObjectMapper objectMapper;
    private StringWriter responseWriter;
    private SoftAssertions softly;

    @BeforeEach
    void setUp() {
        productServlet = new ProductServlet();
        setField(productServlet, "productService", productService);
        setField(productServlet, "productMapper", productMapper);
        setField(productServlet, "objectMapper", JacksonConfig.getObjectMapper());
        objectMapper = JacksonConfig.getObjectMapper();
        responseWriter = new StringWriter();
        softly = new SoftAssertions();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("GET /api/products - должен вернуть список всех товаров")
    void doGet_ShouldReturnAllProducts() throws Exception {
        Product product = Instancio.create(Product.class);
        ProductDTO productDTO = Instancio.create(ProductDTO.class);

        when(request.getPathInfo()).thenReturn("/");
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(productService.getAllProducts()).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        productServlet.doGet(request, response);

        softly.assertThat(responseWriter.toString()).contains(productDTO.getName());
        softly.assertThatCode(() -> {
            verify(response).setContentType("application/json");
            verify(response).setCharacterEncoding("UTF-8");
            verify(productService).getAllProducts();
        }).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("GET /api/products/{id} - должен вернуть товар по идентификатору")
    void doGet_ShouldReturnProductById() throws Exception {
        Product product = Instancio.create(Product.class);
        ProductDTO productDTO = Instancio.create(ProductDTO.class);

        when(request.getPathInfo()).thenReturn("/1");
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        productServlet.doGet(request, response);

        softly.assertThatCode(() -> {
            verify(productService).getProductById(1L);
            verify(response).setContentType("application/json");
        }).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("GET /api/products/{id} - должен вернуть 404 если товар не найден")
    void doGet_ShouldReturn404WhenProductNotFound() throws Exception {
        when(request.getPathInfo()).thenReturn("/999");
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(productService.getProductById(999L)).thenReturn(Optional.empty());

        productServlet.doGet(request, response);

        softly.assertThat(responseWriter.toString()).contains("Товар не найден");
        softly.assertThatCode(() -> verify(response).setStatus(HttpServletResponse.SC_NOT_FOUND)).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("POST /api/products - должен создать новый товар и вернуть его данные")
    void doPost_ShouldCreateProduct() throws Exception {
        CreateProductDTO createDto = Instancio.create(CreateProductDTO.class);
        Product product = Instancio.create(Product.class);
        ProductDTO productDTO = Instancio.create(ProductDTO.class);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(objectMapper.writeValueAsString(createDto))));
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(productMapper.toEntity(any(CreateProductDTO.class))).thenReturn(product);
        when(productService.addProduct(any(Product.class))).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        productServlet.doPost(request, response);

        softly.assertThat(responseWriter.toString()).contains(productDTO.getName());
        softly.assertThatCode(() -> {
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
            verify(productService).addProduct(any(Product.class));
        }).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("POST /api/products - должен вернуть 400 при невалидных данных")
    void doPost_ShouldReturn400ForInvalidData() throws Exception {
        CreateProductDTO invalidDto = Instancio.of(CreateProductDTO.class)
                .ignore(field(CreateProductDTO::getName))
                .create();
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(objectMapper.writeValueAsString(invalidDto))));
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));

        productServlet.doPost(request, response);

        softly.assertThat(responseWriter.toString()).contains("Ошибка валидации");
        softly.assertThatCode(() -> verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST)).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("DELETE /api/products/{id} - должен удалить товар и вернуть подтверждение")
    void doDelete_ShouldDeleteProduct() throws Exception {
        Product product = Instancio.create(Product.class);

        when(request.getPathInfo()).thenReturn("/1");
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(productService.getProductById(1L)).thenReturn(Optional.of(product));

        productServlet.doDelete(request, response);

        softly.assertThat(responseWriter.toString()).contains("Товар успешно удален");
        softly.assertThat(responseWriter.toString()).contains("\"status\"");
        softly.assertThatCode(() -> verify(productService).deleteProduct(1L)).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("GET /api/products/category/{category} - должен вернуть товары по категории")
    void doGet_ShouldReturnProductsByCategory() throws Exception {
        Product product = Instancio.create(Product.class);
        ProductDTO productDTO = Instancio.create(ProductDTO.class);

        when(request.getPathInfo()).thenReturn("/category/Electronics");
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(productService.getProductsByCategory("Electronics")).thenReturn(List.of(product));
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        productServlet.doGet(request, response);

        softly.assertThatCode(() -> {
            verify(productService).getProductsByCategory("Electronics");
            verify(response).setContentType("application/json");
        }).doesNotThrowAnyException();
        softly.assertAll();
    }
}