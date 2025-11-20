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

    /** Сервис для работы с товарами */
    private final ProductService productService;

    /** Сервис аутентификации и авторизации */
    private final AuthService authService;

    /** Сервис для ведения журнала аудита */
    private final AuditService   auditService;

    /** Сервис для сбора метрик производительности */
    private final MetricsService metricsService;

    /** Сканер для ввода данных от пользователя */
    private final Scanner scanner;

    /** Меню аутентификации */
    private final AuthMenu authMenu;

    /** Меню работы с товарами */
    private final ProductMenu productMenu;

    /** Меню управления пользователями */
    private final UserManagementMenu userManagementMenu;

    /** Флаг indicating, работает ли главное меню */
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
        this.productMenu = new ProductMenu(productService, auditService, authService, scanner);
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

        if (authService.canManageProducts()) {
            System.out.println("6. Добавить товар");
            System.out.println("7. Обновить товар");
            System.out.println("8. Удалить товар");
        }

        System.out.println("9. Статистика кэша");

        if (authService.canViewAudit()) {
            System.out.println("10. Журнал аудита");
        }

        if (authService.canManageProducts()) {
            System.out.println("11. Метрики приложения");
        }

        if (authService.isAdmin()) {
            System.out.println("12. Управление пользователями");
        }

        System.out.println("0. Выйти");
        System.out.print("Выберите действие: ");
    }

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
}
