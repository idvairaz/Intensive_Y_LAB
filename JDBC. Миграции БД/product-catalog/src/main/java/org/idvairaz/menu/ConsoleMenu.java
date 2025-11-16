package org.idvairaz.menu;

import org.idvairaz.service.AuditService;
import org.idvairaz.service.AuthService;
import org.idvairaz.service.MetricsService;
import org.idvairaz.service.ProductService;

import java.util.Scanner;


/**
 * Консольное меню для взаимодействия с пользователем.
 * Предоставляет интерфейс для управления каталогом товаров с системой авторизации.
 *
 * @author idvavraz
 * @version 1.0
 */
public class ConsoleMenu {
    private final ProductService productService;
    private final AuthService authService;
     private final AuditService   auditService;
    private final MetricsService metricsService;
    private final Scanner scanner;
    private final AuthMenu authMenu;
    private final ProductMenu productMenu;
    private final UserManagementMenu userManagementMenu;
    private boolean running = true;

    /**
     * Конструктор консольного меню.
     *
     * @param productService сервис для работы с товарами
     * @param authService сервис аутентификации и авторизации
     * @param auditService сервис аудита действий
     * @param metricsService сервис сбора метрик
     */
    public ConsoleMenu(ProductService productService, AuthService authService,
                       AuditService auditService, MetricsService metricsService) {
        this.productService = productService;
        this.authService = authService;
        this.auditService = auditService;
        this.metricsService = metricsService;
        this.scanner = new Scanner(System.in);

        this.authMenu = new AuthMenu(authService, auditService, scanner);
        this.productMenu = new ProductMenu(productService, scanner);
        this.userManagementMenu = new UserManagementMenu(authService, auditService, authMenu, scanner);
    }

    /**
     * Запускает главный цикл приложения.
     * Отображает меню авторизации, затем основное меню.
     */
    public void start() {
        System.out.println("=== КАТАЛОГ ТОВАРОВ МАРКЕТПЛЕЙСА ===");
        authMenu.showAuthMenu();

        while (running) {
            showMainMenu();
            int choice = authMenu.readChoice();
            processChoice(choice);
        }

        scanner.close();
    }
//    public void start() {
//        System.out.println("=== КАТАЛОГ ТОВАРОВ МАРКЕТПЛЕЙСА ===");
//        authMenu.showAuthMenu();
//
//        while (running) {
//            showMainMenu();
//            int choice = readChoice(scanner);
//            processChoice(choice);
//        }
//
//        scanner.close();
//    }
//
//    /**
//     * Отображает меню авторизации до успешного входа в систему.
//     */
//    private void showAuthMenu() {
//        while (!authService.isAuthenticated()) {
//            System.out.println("\n=== МЕНЮ АВТОРИЗАЦИИ ===");
//            System.out.println("1. Войти");
//            System.out.println("2. Зарегистрироваться");
//            System.out.println("3. Выйти из приложения");
//            System.out.print("Выберите действие: ");
//
//            int choice = readChoice();
//            processAuthChoice(choice);
//        }
//    }
//
//    /**
//     * Обрабатывает выбор пользователя в меню авторизации.
//     *
//     * @param choice выбранный пункт меню
//     */
//    private void processAuthChoice(int choice) {
//        switch (choice) {
//            case 1 -> login();
//            case 2 -> register();
//            case 3 -> {
//                System.out.println("Выход из приложения.");
//                System.exit(0);
//            }
//            default -> System.out.println("Неверный выбор! Попробуйте снова.");
//        }
//    }
//
//    /**
//     * Выполняет процесс входа пользователя в систему.
//     * Предоставляет 3 попытки для ввода правильных учетных данных.
//     */
//    private void login() {
//        System.out.println("\n=== ВХОД В СИСТЕМУ ===");
//        int attempts = 3;
//
//        while (attempts > 0 && !authService.isAuthenticated()) {
//            System.out.print("Логин: ");
//            String username = scanner.nextLine();
//
//            System.out.print("Пароль: ");
//            String password = scanner.nextLine();
//
//            if (authService.login(username, password)) {
//                auditService.logAction(username, "ВХОД", "Успешная авторизация");
//                System.out.println("Добро пожаловать, " + username + "!");
//            } else {
//                attempts--;
//                System.out.println("Неверный логин или пароль. Осталось попыток: " + attempts);
//                auditService.logAction(username, "НЕУДАЧНАЯ_ПОПЫТКА_ВХОДА", "Неверные учетные данные");
//            }
//        }
//
//        if (!authService.isAuthenticated()) {
//            System.out.println("Превышено количество попыток. Попробуйте позже.");
//        }
//    }
//
//    /**
//     * Выполняет процесс регистрации нового пользователя.
//     */
//    private void register() {
//        System.out.println("\n=== РЕГИСТРАЦИЯ ===");
//
//        System.out.print("Придумайте логин: ");
//        String username = scanner.nextLine();
//
//        System.out.print("Придумайте пароль: ");
//        String password = scanner.nextLine();
//
//        if (authService.register(username, password)) {
//            System.out.println("Регистрация успешна! Теперь вы можете войти в систему.");
//            auditService.logAction(username, "РЕГИСТРАЦИЯ", "Новый пользователь зарегистрирован");
//        } else {
//            System.out.println("Ошибка регистрации. Возможно, пользователь с таким логином уже существует.");
//            auditService.logAction(username, "НЕУДАЧНАЯ_РЕГИСТРАЦИЯ", "Попытка создать существующего пользователя");
//        }
//    }

    /**
     * Отображает главное меню приложения с учетом прав доступа пользователя.
     */
    private void showMainMenu() {
        System.out.println("\n=== ГЛАВНОЕ МЕНЮ ===");
        System.out.println("Текущий пользователь: " + authService.getCurrentUsername() +
                " [" + authService.getCurrentUserRole() + "]");

        System.out.println("1. Показать все товары");
        System.out.println("2. Найти товар по ID");
        System.out.println("3. Найти товар по имени");
        System.out.println("4. Найти товары по категории");
        System.out.println("5. Найти товары по бренду");

        /** Только для MANAGER и ADMIN */
        if (authService.canManageProducts()) {
            System.out.println("6. Добавить товар");
            System.out.println("7. Обновить товар");
            System.out.println("8. Удалить товар");
        }

        System.out.println("9. Статистика кэша");

        /** Только для MANAGER и ADMIN */
        if (authService.canViewAudit()) {
            System.out.println("10. Журнал аудита");
        }

        if (authService.canManageProducts()) {
            System.out.println("11. Метрики приложения");
        }

        /** Только для ADMIN */
        if (authService.isAdmin()) {
            System.out.println("12. Управление пользователями");
        }

        System.out.println("0. Выйти");
        System.out.print("Выберите действие: ");
    }

//    /**
//     * Читает выбор пользователя из консоли.
//     *
//     * @return числовой выбор пользователя или -1 при ошибке
//     */
//    private int readChoice() {
//        try {
//            return scanner.nextInt();
//        } catch (Exception e) {
//            scanner.nextLine();
//            return -1;
//        } finally {
//            scanner.nextLine();
//        }
//    }

    /**
     * Обрабатывает выбор пользователя в главном меню.
     *
     * @param choice выбранный пункт меню
     */
    private void processChoice(int choice) {
        String username = authService.getCurrentUsername();

        switch (choice) {
            case 1 -> {
                auditService.logAction(username, "ПРОСМОТР_ТОВАРОВ", "Показать все товары");
                productMenu.showAllProducts();
            }
            case 2 -> {
                auditService.logAction(username, "ПОИСК_ПО_ID", "Поиск товара по ID");
                productMenu.findProductById();
            }
            case 3 -> {
                auditService.logAction(username, "ПОИСК_ПО_ИМЕНИ", "Поиск товара по имени");
                productMenu.findProductByName();
            }
            case 4 -> {
                auditService.logAction(username, "ПОИСК_ПО_КАТЕГОРИИ", "Поиск по категории");
                productMenu.findProductsByCategory();
            }
            case 5 -> {
                auditService.logAction(username, "ПОИСК_ПО_БРЕНДУ", "Поиск по бренду");
                productMenu.findProductsByBrand();
            }
            case 6 -> {
                if (!authService.canManageProducts()) {
                    System.out.println("Ошибка: Недостаточно прав для добавления товаров");
                    auditService.logAction(username, "ОТКАЗ_В_ДОСТУПЕ", "Попытка добавить товар без прав");
                    return;
                }
                auditService.logAction(username, "ДОБАВЛЕНИЕ_ТОВАРА", "Начало процесса");
                productMenu.addProduct();
            }
            case 7 -> {
                if (!authService.canManageProducts()) {
                    System.out.println("Ошибка: Недостаточно прав для обновления товаров");
                    auditService.logAction(username, "ОТКАЗ_В_ДОСТУПЕ", "Попытка обновить товар без прав");
                    return;
                }
                auditService.logAction(username, "ОБНОВЛЕНИЕ_ТОВАРА", "Начало процесса");
                productMenu.updateProduct();
            }
            case 8 -> {
                if (!authService.canManageProducts()) {
                    System.out.println("Ошибка: Недостаточно прав для удаления товаров");
                    auditService.logAction(username, "ОТКАЗ_В_ДОСТУПЕ", "Попытка удалить товар без прав");
                    return;
                }
                auditService.logAction(username, "УДАЛЕНИЕ_ТОВАРА", "Начало процесса");
                productMenu.deleteProduct();
            }
            case 9 -> {
                auditService.logAction(username, "ПРОСМОТР_СТАТИСТИКИ", "Статистика кэша");
                productMenu.showCacheStats();
            }
            case 10 -> {
                if (!authService.canViewAudit()) {
                    System.out.println("Ошибка: Недостаточно прав для просмотра журнала аудита");
                    auditService.logAction(username, "ОТКАЗ_В_ДОСТУПЕ", "Попытка просмотреть аудит без прав");
                    return;
                }
                auditService.logAction(username, "ПРОСМОТР_ЖУРНАЛА", "Просмотр журнала аудита");
                auditService.showAuditLog();
            }
            case 11 -> {
                if (!authService.canManageProducts()) {
                    System.out.println("Ошибка: Недостаточно прав для просмотра метрик");
                    return;
                }
                auditService.logAction(username, "ПРОСМОТР_МЕТРИК", "Просмотр метрик приложения");
                showMetrics();
            }
            case 12 -> {
                if (!authService.isAdmin()) {
                    System.out.println("Ошибка: Требуются права администратора");
                    auditService.logAction(username, "ОТКАЗ_В_ДОСТУПЕ", "Попытка управления пользователями без прав ADMIN");
                    return;
                }
                auditService.logAction(username, "УПРАВЛЕНИЕ_ПОЛЬЗОВАТЕЛЯМИ", "Просмотр управления пользователями");
                userManagementMenu.manageUsers();
            }
            case 0 -> {
                exit();
            }
            default -> {
                auditService.logAction(username, "НЕВЕРНЫЙ_ВЫБОР", "Выбрана несуществующая опция: " + choice);
                System.out.println("Неверный выбор!");
            }
        }
    }

//
//
//    /**
//     * Управление пользователями (расширенная версия для администратора).
//     */
//    private void manageUsers() {
//        boolean inUserManagement = true;
//
//        while (inUserManagement && authService.isAdmin()) {
//            showUserManagementMenu();
//            int choice = readChoice();
//
//            if (choice == 0) {
//                inUserManagement = false;
//            } else {
//                processUserManagementChoice(choice);
//            }
//        }
//    }
//
//    /**
//     * Отображает меню управления пользователями (только для администратора).
//     */
//    private void showUserManagementMenu() {
//        System.out.println("\n=== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ===");
//        System.out.println("1. Показать всех пользователей");
//        System.out.println("2. Добавить пользователя");
//        System.out.println("3. Изменить роль пользователя");
//        System.out.println("4. Удалить пользователя");
//        System.out.println("0. Назад в главное меню");
//        System.out.print("Выберите действие: ");
//    }
//
//    /**
//     * Обрабатывает выбор в меню управления пользователями.
//     */
//    private void processUserManagementChoice(int choice) {
//        String username = authService.getCurrentUsername();
//
//        switch (choice) {
//            case 1 -> {
//                auditService.logAction(username, "ПРОСМОТР_ПОЛЬЗОВАТЕЛЕЙ", "Просмотр списка пользователей");
//                showAllUsers();
//            }
//            case 2 -> {
//                auditService.logAction(username, "ДОБАВЛЕНИЕ_ПОЛЬЗОВАТЕЛЯ", "Начало процесса");
//                addUserWithRole();
//            }
//            case 3 -> {
//                auditService.logAction(username, "ИЗМЕНЕНИЕ_РОЛИ", "Начало процесса");
//                changeUserRole();
//            }
//            case 4 -> {
//                auditService.logAction(username, "УДАЛЕНИЕ_ПОЛЬЗОВАТЕЛЯ", "Начало процесса");
//                deleteUser();
//            }
//            case 0 -> {
//                auditService.logAction(username, "ВЫХОД_ИЗ_УПРАВЛЕНИЯ", "Возврат в главное меню");
//                System.out.println("Возврат в главное меню...");
//            }
//            default -> {
//                auditService.logAction(username, "НЕВЕРНЫЙ_ВЫБОР_УПРАВЛЕНИЯ", "Выбрана несуществующая опция: " + choice);
//                System.out.println("Неверный выбор!");
//            }
//        }
//    }
//
//    /**
//     * Отображает список всех пользователей системы.
//     */
//    private void showAllUsers() {
//        System.out.println("\n=== СПИСОК ПОЛЬЗОВАТЕЛЕЙ ===");
//        Map<String, User> users = authService.getUsers();
//
//        if (users.isEmpty()) {
//            System.out.println("Пользователей нет");
//        } else {
//            System.out.println("Всего пользователей: " + users.size());
//            users.forEach((username, user) -> {
//                System.out.printf("- %s [%s] %s%n",
//                        username,
//                        user.getRole(),
//                        user.isLoggedIn() ? "(online)" : ""
//                );
//            });
//        }
//    }
//
//    /**
//     * Добавляет нового пользователя с заданной ролью.
//     */
//    private void addUserWithRole() {
//        System.out.println("\n=== ДОБАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ===");
//
//        System.out.print("Введите имя пользователя: ");
//        String username = scanner.nextLine().trim();
//
//        if (username.isEmpty()) {
//            System.out.println("Ошибка: имя пользователя не может быть пустым");
//            return;
//        }
//
//        System.out.print("Введите пароль: ");
//        String password = scanner.nextLine().trim();
//
//        if (password.isEmpty()) {
//            System.out.println("Ошибка: пароль не может быть пустым");
//            return;
//        }
//
//        // Выбор роли
//        System.out.println("Выберите роль:");
//        System.out.println("1. USER - Обычный пользователь");
//        System.out.println("2. MANAGER - Менеджер");
//        System.out.println("3. ADMIN - Администратор");
//        System.out.print("Введите номер роли: ");
//
//        try {
//            int roleChoice = scanner.nextInt();
//            scanner.nextLine(); // очистка буфера
//
//            User.UserRole role;
//            switch (roleChoice) {
//                case 1 -> role = User.UserRole.USER;
//                case 2 -> role = User.UserRole.MANAGER;
//                case 3 -> role = User.UserRole.ADMIN;
//                default -> {
//                    System.out.println("Ошибка: неверный выбор роли");
//                    return;
//                }
//            }
//
//            if (authService.registerWithRole(username, password, role)) {
//                System.out.println("Пользователь " + username + " успешно создан с ролью " + role);
//                auditService.logAction(authService.getCurrentUsername(), "УСПЕШНОЕ_ДОБАВЛЕНИЕ_ПОЛЬЗОВАТЕЛЯ",
//                        "Создан пользователь: " + username + " с ролью: " + role);
//            } else {
//                System.out.println("Ошибка: не удалось создать пользователя. Возможно, пользователь с таким именем уже существует.");
//                auditService.logAction(authService.getCurrentUsername(), "ОШИБКА_ДОБАВЛЕНИЯ_ПОЛЬЗОВАТЕЛЯ",
//                        "Не удалось создать пользователя: " + username);
//            }
//
//        } catch (Exception e) {
//            System.out.println("Ошибка: неверный формат ввода");
//            scanner.nextLine(); // очистка буфера
//        }
//    }
//
//    /**
//     * Изменяет роль существующего пользователя.
//     */
//    private void changeUserRole() {
//        System.out.println("\n=== ИЗМЕНЕНИЕ РОЛИ ПОЛЬЗОВАТЕЛЯ ===");
//
//        // Сначала покажем текущих пользователей
//        showAllUsers();
//
//        System.out.print("\nВведите имя пользователя для изменения роли: ");
//        String username = scanner.nextLine().trim();
//
//        if (username.isEmpty()) {
//            System.out.println("Ошибка: имя пользователя не может быть пустым");
//            return;
//        }
//
//        // Проверяем, что пользователь существует
//        Map<String, User> users = authService.getUsers();
//        if (!users.containsKey(username)) {
//            System.out.println("Ошибка: пользователь " + username + " не найден");
//            return;
//        }
//
//        // Нельзя изменять роль самому себе
//        if (username.equals(authService.getCurrentUsername())) {
//            System.out.println("Ошибка: нельзя изменять роль самому себе");
//            return;
//        }
//
//        System.out.println("Выберите новую роль для пользователя " + username + ":");
//        System.out.println("1. USER - Обычный пользователь");
//        System.out.println("2. MANAGER - Менеджер");
//        System.out.println("3. ADMIN - Администратор");
//        System.out.print("Введите номер роли: ");
//
//        try {
//            int roleChoice = scanner.nextInt();
//            scanner.nextLine(); // очистка буфера
//
//            User.UserRole newRole;
//            switch (roleChoice) {
//                case 1 -> newRole = User.UserRole.USER;
//                case 2 -> newRole = User.UserRole.MANAGER;
//                case 3 -> newRole = User.UserRole.ADMIN;
//                default -> {
//                    System.out.println("Ошибка: неверный выбор роли");
//                    return;
//                }
//            }
//
//            if (authService.changeUserRole(username, newRole)) {
//                System.out.println("Роль пользователя " + username + " успешно изменена на " + newRole);
//                auditService.logAction(authService.getCurrentUsername(), "УСПЕШНОЕ_ИЗМЕНЕНИЕ_РОЛИ",
//                        "Пользователь: " + username + ", новая роль: " + newRole);
//            } else {
//                System.out.println("Ошибка: не удалось изменить роль пользователя");
//                auditService.logAction(authService.getCurrentUsername(), "ОШИБКА_ИЗМЕНЕНИЯ_РОЛИ",
//                        "Не удалось изменить роль пользователя: " + username);
//            }
//
//        } catch (Exception e) {
//            System.out.println("Ошибка: неверный формат ввода");
//            scanner.nextLine(); // очистка буфера
//        }
//    }
//
//    /**
//     * Удаляет пользователя из системы.
//     */
//    private void deleteUser() {
//        System.out.println("\n=== УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ===");
//
//        // Сначала покажем текущих пользователей
//        showAllUsers();
//
//        System.out.print("\nВведите имя пользователя для удаления: ");
//        String username = scanner.nextLine().trim();
//
//        if (username.isEmpty()) {
//            System.out.println("Ошибка: имя пользователя не может быть пустым");
//            return;
//        }
//
//        // Проверяем, что пользователь существует
//        Map<String, User> users = authService.getUsers();
//        if (!users.containsKey(username)) {
//            System.out.println("Ошибка: пользователь " + username + " не найден");
//            return;
//        }
//
//        // Подтверждение удаления
//        System.out.print("Вы уверены, что хотите удалить пользователя " + username + "? (да/нет): ");
//        String confirmation = scanner.nextLine().trim().toLowerCase();
//
//        if (confirmation.equals("да") || confirmation.equals("yes") || confirmation.equals("y")) {
//            if (authService.deleteUser(username)) {
//                System.out.println("Пользователь " + username + " успешно удален");
//                auditService.logAction(authService.getCurrentUsername(), "УСПЕШНОЕ_УДАЛЕНИЕ_ПОЛЬЗОВАТЕЛЯ",
//                        "Удален пользователь: " + username);
//            } else {
//                System.out.println("Ошибка: не удалось удалить пользователя");
//                auditService.logAction(authService.getCurrentUsername(), "ОШИБКА_УДАЛЕНИЕ_ПОЛЬЗОВАТЕЛЯ",
//                        "Не удалось удалить пользователя: " + username);
//            }
//        } else {
//            System.out.println("Удаление отменено");
//            auditService.logAction(authService.getCurrentUsername(), "ОТМЕНА_УДАЛЕНИЯ",
//                    "Отменено удаление пользователя: " + username);
//        }
//    }
//
//
//    /**
//     * Добавляет новый товар в каталог.
//     */
//    private void addProduct() {
//        System.out.println("\n=== ДОБАВЛЕНИЕ ТОВАРА ===");
//
//        String name = readName("Введите название: ");
//
//        System.out.print("Введите описание: ");
//        String description = scanner.nextLine();
//
//        double price = readDouble("Введите цену: ");
//
//        System.out.print("Введите категорию: ");
//        String category = scanner.nextLine();
//
//        System.out.print("Введите бренд: ");
//        String brand = scanner.nextLine();
//
//        int stockQuantity = readInt("Введите количество: ");
//
//        try {
//            Product product = new Product(name, description, price, category, brand, stockQuantity);
//            Product savedProduct = productService.addProduct(product);
//            System.out.println("Товар добавлен! ID: " + savedProduct.getId());
//        } catch (Exception e) {
//            System.out.println("Ошибка: " + e.getMessage());
//        }
//    }
//
//    /**
//     * Отображает все товары из каталога.
//     */
//    private void showAllProducts() {
//        System.out.println("\n=== ВСЕ ТОВАРЫ ===");
//        List<Product> products = productService.getAllProducts();
//
//        if (products.isEmpty()) {
//            System.out.println("Товаров нет");
//        } else {
//            products.forEach(this::printProductShort);
//        }
//
//        productService.printProductStats();
//    }
//
//    /**
//     * Находит и отображает товар по идентификатору.
//     */
//    private void findProductById() {
//        System.out.println("\n=== ПОИСК ПО ID ===");
//        System.out.print("Введите ID товара: ");
//
//        try {
//            Long id = scanner.nextLong();
//            scanner.nextLine();
//
//            Optional<Product> product = productService.getProductById(id);
//            if (product.isPresent()) {
//                printProductDetailed(product.get());
//            } else {
//                System.out.println("Товар с ID " + id + " не найден");
//            }
//        } catch (Exception e) {
//            System.out.println("Ошибка: неверный формат ID");
//            scanner.nextLine();
//        }
//    }
//
//    /**
//     * Находит и отображает товар по названию.
//     */
//    private void findProductByName() {
//        System.out.println("\n=== ПОИСК ПО ИМЕНИ ===");
//        System.out.print("Введите название товара: ");
//        String name = scanner.nextLine();
//
//        Optional<Product> product = productService.getProductByName(name);
//        if (product.isPresent()) {
//            printProductDetailed(product.get());
//        } else {
//            System.out.println("Товар '" + name + "' не найден");
//        }
//    }
//
//    /**
//     * Находит и отображает товары по категории.
//     */
//    private void findProductsByCategory() {
//        System.out.println("\n=== ПОИСК ПО КАТЕГОРИИ ===");
//        System.out.print("Введите категорию: ");
//        String category = scanner.nextLine();
//
//        List<Product> products = productService.getProductsByCategory(category);
//
//        if (products.isEmpty()) {
//            System.out.println("Товаров в категории '" + category + "' нет");
//        } else {
//            System.out.println("Найдено товаров: " + products.size());
//            products.forEach(this::printProductShort);
//        }
//    }
//
//    /**
//     * Находит и отображает товары по бренду.
//     */
//    private void findProductsByBrand() {
//        System.out.println("\n=== ПОИСК ПО БРЕНДУ ===");
//        System.out.print("Введите бренд: ");
//        String brand = scanner.nextLine();
//
//        List<Product> products = productService.getProductsByBrand(brand);
//
//        if (products.isEmpty()) {
//            System.out.println("Товаров бренда '" + brand + "' нет");
//        } else {
//            System.out.println("Найдено товаров: " + products.size());
//            products.forEach(this::printProductShort);
//        }
//    }
//
//    /**
//     * Обновляет существующий товар.
//     */
//    private void updateProduct() {
//        System.out.println("\n=== ОБНОВЛЕНИЕ ТОВАРА ===");
//        System.out.print("Введите ID товара для обновления: ");
//
//        try {
//            Long id = scanner.nextLong();
//            scanner.nextLine(); // очистка буфера
//
//            Optional<Product> existingProduct = productService.getProductById(id);
//            if (existingProduct.isEmpty()) {
//                System.out.println("Товар с ID " + id + " не найден");
//                return;
//            }
//
//            System.out.println("Текущие данные:");
//            printProductDetailed(existingProduct.get());
//            System.out.println("\nВведите новые данные:");
//
//            String name = readName("Введите название: ");
//
//            System.out.print("Новое описание: ");
//            String description = scanner.nextLine();
//
//            double price = readDouble("Новая цена: ");
//
//            System.out.print("Новая категория: ");
//            String category = scanner.nextLine();
//
//            System.out.print("Новый бренд: ");
//            String brand = scanner.nextLine();
//
//            int stockQuantity = readInt("Новое количество: ");
//
//            Product updatedProduct = new Product(name, description, price, category, brand, stockQuantity);
//            Product result = productService.updateProduct(id, updatedProduct);
//            System.out.println("Товар обновлен! ID: " + result.getId());
//
//        } catch (Exception e) {
//            System.out.println("Ошибка: Не верный формат ID");
//        }
//    }
//
//    /**
//     * Удаляет товар из каталога.
//     */
//    private void deleteProduct() {
//        System.out.println("\n=== УДАЛЕНИЕ ТОВАРА ===");
//        System.out.print("Введите ID товара для удаления: ");
//
//        try {
//            Long id = scanner.nextLong();
//            scanner.nextLine();
//            Optional<Product> product = productService.getProductById(id);
//            if (product.isPresent()) {
//                productService.deleteProduct(id);
//                System.out.println("Товар с ID " + id + " удален");
//            } else {
//                System.out.println("Товар с ID " + id + " не найден");
//            }
//        } catch (Exception e) {
//            System.out.println("Ошибка: неверный формат ID");
//            scanner.nextLine();
//        }
//    }
//
//    /**
//     * Отображает статистику кэширования.
//     */
//    private void showCacheStats() {
//        productService.printCacheStats();
//    }

    /**
     * Отображает метрики производительности приложения.
     */
    private void showMetrics() {
        metricsService.showMetrics();
    }

    /**
     * Выполняет выход пользователя из системы и возвращает в меню авторизации.
     */
    private void exit() {
        String username = authService.getCurrentUsername();
        auditService.logAction(username, "ВЫХОД", "Завершение сеанса");
        System.out.println("До свидания! " + username + "!");
        authService.logout();
        authMenu.showAuthMenu();
    }
//
//    /**
//     * Выводит краткую информацию о товаре.
//     *
//     * @param product товар для отображения
//     */
//    private void printProductShort(Product product) {
//        System.out.printf("ID: %-3d | Название: %-15s | Цена: %-7.2f руб. | Категория: %-15s | Бренд: %-15s | Кол-во: %-3d шт.%n",
//                product.getId(),
//                product.getName(),
//                product.getPrice(),
//                product.getCategory(),
//                product.getBrand(),
//                product.getStockQuantity());
//    }
//
//    /**
//     * Выводит подробную информацию о товаре.
//     *
//     * @param product товар для отображения
//     */
//    private void printProductDetailed(Product product) {
//        System.out.println("=== ПОДРОБНАЯ ИНФОРМАЦИЯ ===");
//        System.out.printf("ID: %d%n", product.getId());
//        System.out.printf("Название: %s%n", product.getName());
//        System.out.printf("Описание: %s%n", product.getDescription());
//        System.out.printf("Цена: %.2f руб.%n", product.getPrice());
//        System.out.printf("Категория: %s%n", product.getCategory());
//        System.out.printf("Бренд: %s%n", product.getBrand());
//        System.out.printf("Количество: %d шт.%n", product.getStockQuantity());
//        System.out.printf("Создан: %s%n", product.getCreatedAt());
//        System.out.printf("Обновлен: %s%n", product.getUpdatedAt());
//    }
//
//    /**
//     * Читает и проверяет уникальность названия товара.
//     *
//     * @param prompt приглашение для ввода
//     * @return уникальное название товара
//     */
//    private String readName(String prompt) {
//        String name = "";
//        while (true) {
//            System.out.print("Введите название: ");
//            name = scanner.nextLine().trim();
//            Optional<Product> existingProduct = productService.getProductByName(name);
//            if (existingProduct.isPresent()) {
//                System.out.println("Ошибка: товар с именем '" + name + "' уже существует. Введите другое название.");
//            } else {
//                break;
//            }
//        }
//        return name;
//    }
//
//    /**
//     * Читает и проверяет цену товара.
//     *
//     * @param prompt приглашение для ввода (текст, который показывается пользователю перед вводом)
//     * @return корректная положительная цена
//     */
//    private double readDouble(String prompt) {
//        while (true) {
//            System.out.print(prompt);
//            String input = scanner.nextLine().trim();
//
//            try {
//                input = input.replace(',', '.');
//                double value = Double.parseDouble(input);
//                if (value <= 0) {
//                    System.out.println("Ошибка: число должно быть положительным. Попробуйте снова.");
//                    continue;
//                }
//                return value;
//            } catch (NumberFormatException e) {
//                System.out.println("Ошибка: введите корректное число (например: 15.99 или 15,99). Попробуйте снова.");
//            }
//        }
//    }
//
//    /**
//     * Читает и проверяет количество товара.
//     *
//     * @param prompt приглашение для ввода (текст, который показывается пользователю перед вводом)
//     * @return корректное неотрицательное количество
//     */
//    private int readInt(String prompt) {
//        while (true) {
//            System.out.print(prompt);
//            String input = scanner.nextLine().trim();
//
//            try {
//                int value = Integer.parseInt(input);
//                if (value < 0) {
//                    System.out.println("Ошибка: количество не может быть отрицательным. Попробуйте снова.");
//                    continue;
//                }
//                return value;
//            } catch (NumberFormatException e) {
//                System.out.println("Ошибка: введите целое не отрицательное число. Попробуйте снова.");
//            }
//        }
//    }
}
