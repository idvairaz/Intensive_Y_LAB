package org.idvairaz.menu;

import org.idvairaz.model.Product;
import org.idvairaz.service.AuthService;
import org.idvairaz.service.AuditService;
import org.idvairaz.service.ProductService;

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

    private final ProductService productService;
    private final AuditService auditService;

    private final AuthService authService;
    private final Scanner scanner;

    public ProductMenu(ProductService productService, Scanner scanner) {
        this.productService = productService;
        this.scanner = scanner;
        this.auditService = new AuditService();
        this.authService = new AuthService();

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

        double price = readDouble("Введите цену: ");

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

            double price = readDouble("Новая цена: ");

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
    private double readDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();

            try {
                input = input.replace(',', '.');
                double value = Double.parseDouble(input);
                if (value <= 0) {
                    System.out.println("Ошибка: число должно быть положительным. Попробуйте снова.");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("Ошибка: введите корректное число (например: 15.99 или 15,99). Попробуйте снова.");
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
        System.out.println("=== ПОДРОБНАЯ ИНФОРМАЦИЯ ===");
        System.out.printf("ID: %d%n", product.getId());
        System.out.printf("Название: %s%n", product.getName());
        System.out.printf("Описание: %s%n", product.getDescription());
        System.out.printf("Цена: %.2f руб.%n", product.getPrice());
        System.out.printf("Категория: %s%n", product.getCategory());
        System.out.printf("Бренд: %s%n", product.getBrand());
        System.out.printf("Количество: %d шт.%n", product.getStockQuantity());
        System.out.printf("Создан: %s%n", product.getCreatedAt());
        System.out.printf("Обновлен: %s%n", product.getUpdatedAt());
    }
}

