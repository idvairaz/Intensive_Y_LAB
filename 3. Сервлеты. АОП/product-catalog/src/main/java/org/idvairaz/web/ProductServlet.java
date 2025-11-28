package org.idvairaz.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.idvairaz.aspect.HttpAuditable;
import org.idvairaz.dto.CreateProductDTO;
import org.idvairaz.dto.ProductDTO;
import org.idvairaz.dto.UpdateProductDTO;
import org.idvairaz.mapper.ProductMapper;
import org.idvairaz.model.Product;
import org.idvairaz.service.ProductService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервлет для управления товарами через REST API.
 * Предоставляет endpoints для создания, получения, обновления и удаления товаров.
 * Поддерживает поиск товаров по категории и бренду.
 *
 * @author idvavraz
 * @version 1.0
 */
@WebServlet("/api/products/*")
@HttpAuditable("Product API")
public class ProductServlet extends HttpServlet {

    /**
     * Сервис для работы с товарами.
     */
    private ProductService productService;

    /**
     * Маппер для преобразования между Product и ProductDTO.
     */
    private ProductMapper productMapper;

    /**
     * Объект для работы с JSON.
     */
    private ObjectMapper objectMapper;

    /**
     * Инициализирует сервлет, создавая необходимые зависимости.
     * Вызывается контейнером сервлетов при развертывании приложения.
     */
    @Override
    public void init() {
        this.productMapper = ProductMapper.INSTANCE;
        this.objectMapper = JacksonConfig.getObjectMapper();;
        this.productService = ServiceFactory.getProductService();
    }

    /**
     * Обрабатывает GET запросы для получения информации о товарах.
     * Поддерживает следующие endpoints:
     * - GET /api/products - все товары
     * - GET /api/products/{id} - товар по идентификатору
     * - GET /api/products/category/{category} - товары по категории
     * - GET /api/products/brand/{brand} - товары по бренду
     *
     * @param req HTTP запрос
     * @param resp HTTP ответ с данными товаров
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    @HttpAuditable("GET_ALL_PRODUCTS")
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllProducts(resp);
            } else if (pathInfo.startsWith("/category/")) {
                getProductsByCategory(resp, pathInfo);
            } else if (pathInfo.startsWith("/brand/")) {
                getProductsByBrand(resp, pathInfo);
            } else {
                getProductById(resp, pathInfo);
            }
        } catch (Exception e) {
            sendErrorResponse(resp, "Internal server error: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обрабатывает POST запросы для создания новых товаров.
     * Endpoint: POST /api/products
     *
     * @param req HTTP запрос с телом содержащим CreateProductDTO
     * @param resp HTTP ответ с созданным товаром
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            CreateProductDTO createDto = objectMapper.readValue(req.getReader(), CreateProductDTO.class);

            String validationError = ValidationUtil.validate(createDto);
            if (validationError != null) {
                sendErrorResponse(resp, "Ошибка валидации: " + validationError,
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            createProduct(resp, createDto);

        } catch (Exception e) {
            sendErrorResponse(resp, "Неверное тело запроса: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }


    /**
     * Обрабатывает PUT запросы для обновления существующих товаров.
     * Endpoint: PUT /api/products/{id}
     *
     * @param req HTTP запрос с телом содержащим UpdateProductDTO
     * @param resp HTTP ответ с обновленным товаром
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    @HttpAuditable("UPDATE_PRODUCT")
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "Идентификатор товара обязателен для обновления",
                    HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Long id = extractIdFromPath(pathInfo);
            UpdateProductDTO updateDto = objectMapper.readValue(req.getReader(), UpdateProductDTO.class);

            String validationError = ValidationUtil.validate(updateDto);
            if (validationError != null) {
                sendErrorResponse(resp, "Ошибка валидации: " + validationError,
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            updateProduct(resp, id, updateDto);

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "Неверный идентификатор товара",
                    HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(resp, "Неверное тело запроса: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }


    /**
     * Обрабатывает DELETE запросы для удаления товаров.
     * Endpoint: DELETE /api/products/{id}
     *
     * @param req HTTP запрос
     * @param resp HTTP ответ с подтверждением удаления
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    @HttpAuditable("DELETE_PRODUCT")
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "Идентификатор товара обязателен для удаления",
                    HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Long id = extractIdFromPath(pathInfo);
            deleteProduct(resp, id);

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "Неверный идентификатор товара",
                    HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(resp, "Внутренняя ошибка сервера: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Возвращает список всех товаров.
     *
     * @param resp HTTP ответ со списком товаров
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void getAllProducts(HttpServletResponse resp)
            throws IOException {
        List<Product> products = productService.getAllProducts();
        List<ProductDTO> productDTOs = products.stream()
                .map(productMapper::toDTO)
                .collect(Collectors.toList());

        String jsonResponse = objectMapper.writeValueAsString(productDTOs);
        resp.getWriter().write(jsonResponse);
    }

    /**
     * Возвращает товар по идентификатору.
     *
     * @param resp HTTP ответ с данными товара
     * @param pathInfo путь запроса содержащий идентификатор товара
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void getProductById(HttpServletResponse resp, String pathInfo)
            throws IOException {
        Long id = extractIdFromPath(pathInfo);
        Optional<Product> product = productService.getProductById(id);

        if (product.isPresent()) {
            ProductDTO productDTO = productMapper.toDTO(product.get());
            String jsonResponse = objectMapper.writeValueAsString(productDTO);
            resp.getWriter().write(jsonResponse);
        } else {
            sendErrorResponse(resp, "Товар не найден", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Создает новый товар.
     *
     * @param resp HTTP ответ с созданным товаром
     * @param createDto DTO с данными для создания товара
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void createProduct(HttpServletResponse resp,
                               CreateProductDTO createDto) throws IOException {
        try {
            Product product = productMapper.toEntity(createDto);
            Product savedProduct = productService.addProduct(product);
            ProductDTO responseDto = productMapper.toDTO(savedProduct);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            String jsonResponse = objectMapper.writeValueAsString(responseDto);
            resp.getWriter().write(jsonResponse);

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, "Ошибка валидации: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(resp, "Внутренняя ошибка сервера: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обновляет существующий товар.
     *
     * @param resp HTTP ответ с обновленным товаром
     * @param id идентификатор товара
     * @param updateDto DTO с обновленными данными
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void updateProduct(HttpServletResponse resp,
                               Long id, UpdateProductDTO updateDto) throws IOException {
        try {
            Optional<Product> existingProduct = productService.getProductById(id);
            if (existingProduct.isEmpty()) {
                sendErrorResponse(resp, "Товар не найден", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Product existing = existingProduct.get();

            Product updatedProduct = Product.builder()
                    .id(id)
                    .name(updateDto.getName() != null ? updateDto.getName() : existing.getName())
                    .description(updateDto.getDescription() != null ? updateDto.getDescription() : existing.getDescription())
                    .price(updateDto.getPrice() != null ? updateDto.getPrice() : existing.getPrice())
                    .category(updateDto.getCategory() != null ? updateDto.getCategory() : existing.getCategory())
                    .brand(updateDto.getBrand() != null ? updateDto.getBrand() : existing.getBrand())
                    .stockQuantity(updateDto.getStockQuantity() != null ? updateDto.getStockQuantity() : existing.getStockQuantity())
                    .createdAt(existing.getCreatedAt())
                    .updatedAt(java.time.LocalDateTime.now())
                    .build();

            Product savedProduct = productService.updateProduct(id, updatedProduct);
            ProductDTO responseDto = productMapper.toDTO(savedProduct);

            String jsonResponse = objectMapper.writeValueAsString(responseDto);
            resp.getWriter().write(jsonResponse);

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, "Ошибка валидации: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Удаляет товар по идентификатору.
     *
     * @param resp HTTP ответ с подтверждением удаления
     * @param id идентификатор товара
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void deleteProduct(HttpServletResponse resp, Long id)
            throws IOException {
        Optional<Product> product = productService.getProductById(id);
        if (product.isPresent()) {
            productService.deleteProduct(id);
            sendSuccessResponse(resp, "Товар успешно удален");
        } else {
            sendErrorResponse(resp, "Товар не найден", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Возвращает товары по категории.
     *
     * @param resp HTTP ответ со списком товаров
     * @param pathInfo путь запроса содержащий название категории
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void getProductsByCategory(HttpServletResponse resp, String pathInfo)
            throws IOException {
        try {
            String category = pathInfo.substring("/category/".length());
            if (category.isEmpty()) {
                sendErrorResponse(resp, "Название категории обязательно", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            List<Product> products = productService.getProductsByCategory(category);
            List<ProductDTO> productDTOs = products.stream()
                    .map(productMapper::toDTO)
                    .collect(Collectors.toList());

            String jsonResponse = objectMapper.writeValueAsString(productDTOs);
            resp.getWriter().write(jsonResponse);

        } catch (Exception e) {
            sendErrorResponse(resp, "Ошибка поиска по категории: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Возвращает товары по бренду.
     *
     * @param resp HTTP ответ со списком товаров
     * @param pathInfo путь запроса содержащий название бренда
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void getProductsByBrand(HttpServletResponse resp, String pathInfo)
            throws IOException {
        try {
            String brand = pathInfo.substring("/brand/".length());
            if (brand.isEmpty()) {
                sendErrorResponse(resp, "Название бренда обязательно", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            List<Product> products = productService.getProductsByBrand(brand);
            List<ProductDTO> productDTOs = products.stream()
                    .map(productMapper::toDTO)
                    .collect(Collectors.toList());

            String jsonResponse = objectMapper.writeValueAsString(productDTOs);
            resp.getWriter().write(jsonResponse);

        } catch (Exception e) {
            sendErrorResponse(resp, "Ошибка поиска по бренду: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Извлекает идентификатор из пути запроса.
     *
     * @param pathInfo путь запроса
     * @return идентификатор товара
     * @throws NumberFormatException если идентификатор не является числом
     */
    private Long extractIdFromPath(String pathInfo) {
        String idStr = pathInfo.substring(1);
        return Long.parseLong(idStr);
    }

    /**
     * Отправляет HTTP ответ с ошибкой.
     *
     * @param resp HTTP ответ для записи
     * @param message сообщение об ошибке
     * @param statusCode HTTP статус код ошибки
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void sendErrorResponse(HttpServletResponse resp, String message, int statusCode) throws IOException {
        resp.setStatus(statusCode);
        String errorResponse = String.format("{\"error\": \"%s\", \"status\": %d}", message, statusCode);
        resp.getWriter().write(errorResponse);
    }

    /**
     * Отправляет HTTP ответ с успешным выполнением операции.
     *
     * @param resp HTTP ответ для записи
     * @param message сообщение об успехе
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void sendSuccessResponse(HttpServletResponse resp, String message) throws IOException {
        String successResponse = String.format("{\"message\": \"%s\", \"status\": %d}",
                message, HttpServletResponse.SC_OK);
        resp.getWriter().write(successResponse);
    }
}

