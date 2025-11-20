package org.idvairaz.menu;

import org.idvairaz.model.Product;
import org.idvairaz.service.AuthService;
import org.idvairaz.service.AuditService;
import org.idvairaz.service.ProductService;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Меню для операций с товарами.
 * Содержит логику работы с каталогом товаров.
 *
 * @author idvavraz
 * @version 1.0
 */
public class ProductMenu {

    /** Сервис для работы с товарами */
    private final ProductService productService;

    /** Сервис для ведения журнала аудита */
    private final AuditService auditService;

    /** Сервис аутентификации и авторизации */
    private final AuthService authService;

    /** Сканер для ввода данных от пользователя */
    private final Scanner scanner;

    /**
     * Конструктор меню товаров.
     *
     * @param productService сервис для работы с товарами
     * @param auditService сервис для ведения аудита
     * @param authService сервис аутентификации
     * @param scanner сканер для ввода данных
     */
    public ProductMenu(ProductService productService, AuditService auditService,
                       AuthService authService, Scanner scanner) {
        this.productService = productService;
        this.auditService = auditService;
        this.authService = authService;
        this.scanner = scanner;
    }

    /**
     * Отображает все товары из каталога.
     */
    public void showAllProducts() {
        System.out.println("\n=== ВСЕ ТОВАРЫ ===");
        List<Product> products = productService.getAllProducts();

        if (products.isEmpty()) {
            System.out.println("Товаров нет");
        } else {
            products.forEach(this::printProductShort);
        }

        productService.printProductStats();
    }

    /**
     * Находит и отображает товар по идентификатору.
     */
    public void findProductById() {
        System.out.println("\n=== ПОИСК ПО ID ===");
        System.out.print("Введите ID товара: ");

        try {
            Long id = scanner.nextLong();
            scanner.nextLine();

            Optional<Product> product = productService.getProductById(id);
            if (product.isPresent()) {
                printProductDetailed(product.get());
            } else {
                System.out.println("Товар с ID " + id + " не найден");
            }
        } catch (Exception e) {
            System.out.println("Ошибка: неверный формат ID");
            scanner.nextLine();
        }
    }

    /**
     * Находит и отображает товар по названию.
     */
    public void findProductByName() {
        System.out.println("\n=== ПОИСК ПО ИМЕНИ ===");
        System.out.print("Введите название товара: ");
        String name = scanner.nextLine();

        Optional<Product> product = productService.getProductByName(name);
        if (product.isPresent()) {
            printProductDetailed(product.get());
        } else {
            System.out.println("Товар '" + name + "' не найден");
        }
    }

    /**
     * Находит и отображает товары по категории.
     */
    public void findProductsByCategory() {
        System.out.println("\n=== ПОИСК ПО КАТЕГОРИИ ===");
        System.out.print("Введите категорию: ");
        String category = scanner.nextLine();

        List<Product> products = productService.getProductsByCategory(category);

        if (products.isEmpty()) {
            System.out.println("Товаров в категории '" + category + "' нет");
        } else {
            System.out.println("Найдено товаров: " + products.size());
            products.forEach(this::printProductShort);
        }
    }

    /**
     * Находит и отображает товары по бренду.
     */
    public void findProductsByBrand() {
        System.out.println("\n=== ПОИСК ПО БРЕНДУ ===");
        System.out.print("Введите бренд: ");
        String brand = scanner.nextLine();

        List<Product> products = productService.getProductsByBrand(brand);

        if (products.isEmpty()) {
            System.out.println("Товаров бренда '" + brand + "' нет");
        } else {
            System.out.println("Найдено товаров: " + products.size());
            products.forEach(this::printProductShort);
        }
    }

    /**
     * Добавляет новый товар в каталог.
     */
    public void addProduct() {
        String username = authService.getCurrentUsername();
        System.out.println("\n=== ДОБАВЛЕНИЕ ТОВАРА ===");

        String name = readName("Введите название: ");

        System.out.print("Введите описание: ");
        String description = scanner.nextLine();

        BigDecimal price = readBigDecimal("Введите цену: ");

        System.out.print("Введите категорию: ");
        String category = scanner.nextLine();

        System.out.print("Введите бренд: ");
        String brand = scanner.nextLine();

        int stockQuantity = readInt("Введите количество: ");

        try {
            Product product = new Product(name, description, price, category, brand, stockQuantity);
            Product savedProduct = productService.addProduct(product);
            System.out.println("Товар добавлен! ID: " + savedProduct.getId());
            auditService.logAction(username, "УСПЕШНОЕ_ДОБАВЛЕНИЕ_ТОВАРА",
                    "Добавлен товар: " + name + " (ID: " + savedProduct.getId() + ")");
        } catch (Exception e) {
            System.out.println("Ошибка: " + e.getMessage());
            auditService.logAction(username, "ОШИБКА_ДОБАВЛЕНИЯ_ТОВАРА",
                    "Не удалось добавить товар: " + name + " - " + e.getMessage());
        }
    }

    /**
     * Обновляет существующий товар.
     */
    public void updateProduct() {
        String username = authService.getCurrentUsername();

        System.out.println("\n=== ОБНОВЛЕНИЕ ТОВАРА ===");
        System.out.print("Введите ID товара для обновления: ");

        try {
            Long id = scanner.nextLong();
            scanner.nextLine();

            Optional<Product> existingProduct = productService.getProductById(id);
            if (existingProduct.isEmpty()) {
                System.out.println("Товар с ID " + id + " не найден");
                return;
            }

            System.out.println("Текущие данные:");
            printProductDetailed(existingProduct.get());
            System.out.println("\nВведите новые данные:");

            String name = readName("Введите название: ");

            System.out.print("Новое описание: ");
            String description = scanner.nextLine();

            BigDecimal price = readBigDecimal("Новая цена: ");

            System.out.print("Новая категория: ");
            String category = scanner.nextLine();

            System.out.print("Новый бренд: ");
            String brand = scanner.nextLine();

            int stockQuantity = readInt("Новое количество: ");

            Product updatedProduct = new Product(name, description, price, category, brand, stockQuantity);
            Product result = productService.updateProduct(id, updatedProduct);
            System.out.println("Товар обновлен! ID: " + result.getId());
            auditService.logAction(username, "УСПЕШНОЕ_ОБНОВЛЕНИЕ_ТОВАРА",
                    "Обновлен товар ID: " + id + " на " + name);

        } catch (Exception e) {
            System.out.println("Ошибка: Не верный формат ID");
            auditService.logAction(username, "ОШИБКА_ОБНОВЛЕНИЯ_ТОВАРА",
                    "Ошибка обновления товара: " + e.getMessage());
        }
    }

    /**
     * Удаляет товар из каталога.
     */
    public void deleteProduct() {
        String username = authService.getCurrentUsername();

        System.out.println("\n=== УДАЛЕНИЕ ТОВАРА ===");
        System.out.print("Введите ID товара для удаления: ");

        try {
            Long id = scanner.nextLong();
            scanner.nextLine();
            Optional<Product> product = productService.getProductById(id);

            if (product.isPresent()) {
                productService.deleteProduct(id);
                System.out.println("Товар с ID " + id + " удален");
                auditService.logAction(username, "УСПЕШНОЕ_УДАЛЕНИЕ_ТОВАРА",
                        "Удален товар ID: " + id);
            } else {
                System.out.println("Товар с ID " + id + " не найден");
                auditService.logAction(username, "ОШИБКА_УДАЛЕНИЯ_ТОВАРА",
                        "Товар не найден ID: " + id);
            }
        } catch (Exception e) {
            System.out.println("Ошибка: неверный формат ID");
            scanner.nextLine();
            auditService.logAction(username, "ОШИБКА_УДАЛЕНИЯ_ТОВАРА",
                    "Ошибка формата ID: " + e.getMessage());
        }
    }

    /**
     * Отображает статистику кэширования.
     */
    public void showCacheStats() {
        productService.printCacheStats();
    }

    /**
     * Читает и проверяет уникальность названия товара.
     *
     * @param prompt приглашение для ввода
     * @return уникальное название товара
     */
    private String readName(String prompt) {
        String name = "";
        while (true) {
            System.out.print("Введите название: ");
            name = scanner.nextLine().trim();
            Optional<Product> existingProduct = productService.getProductByName(name);
            if (existingProduct.isPresent()) {
                System.out.println("Ошибка: товар с именем '" + name + "' уже существует. Введите другое название.");
            } else {
                break;
            }
        }
        return name;
    }

    /**
     * Читает и проверяет цену товара.
     *
     * @param prompt приглашение для ввода (текст, который показывается пользователю перед вводом)
     * @return корректная положительная цена
     */
    private BigDecimal readBigDecimal(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("Ошибка: введите число. Попробуйте снова.");
                continue;
            }

            try {
                input = input.replace(',', '.');
                BigDecimal value = new BigDecimal(input);

                if (value.compareTo(BigDecimal.ZERO) <= 0) {
                    System.out.println("Ошибка: цена должна быть положительной. Попробуйте снова.");
                    continue;
                }

                value = value.setScale(2, RoundingMode.HALF_UP);

                return value;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число (например: 15.99 или 15,99). Попробуйте снова.");
            } catch (ArithmeticException e) {
                System.out.println("Ошибка: некорректное числовое значение. Попробуйте снова.");
            }
        }
    }

    /**
     * Читает и проверяет количество товара.
     *
     * @param prompt приглашение для ввода (текст, который показывается пользователю перед вводом)
     * @return корректное неотрицательное количество
     */
    private int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                int value = Integer.parseInt(input);
                if (value < 0) {
                    System.out.println("Ошибка: количество не может быть отрицательным. Попробуйте снова.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите целое не отрицательное число. Попробуйте снова.");
            }
        }
    }

    /**
     * Выводит краткую информацию о товаре.
     *
     * @param product товар для отображения
     */
    private void printProductShort(Product product) {
        System.out.printf("ID: %-3d | Название: %-15s | Цена: %-7.2f руб. | Категория: %-15s | Бренд: %-15s | Кол-во: %-3d шт.%n",
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getCategory(),
                product.getBrand(),
                product.getStockQuantity());
    }

    /**
     * Выводит подробную информацию о товаре.
     *
     * @param product товар для отображения
     */
    private void printProductDetailed(Product product) {
        System.out.printf("""
            === ПОДРОБНАЯ ИНФОРМАЦИЯ ===
            ID: %d
            Название: %s
            Описание: %s
            Цена: %.2f руб.
            Категория: %s
            Бренд: %s
            Количество: %d шт.
            Создан: %s
            Обновлен: %s
            """,
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.getBrand(),
                product.getStockQuantity(),
                product.getCreatedAt(),
                product.getUpdatedAt());
    }
}

