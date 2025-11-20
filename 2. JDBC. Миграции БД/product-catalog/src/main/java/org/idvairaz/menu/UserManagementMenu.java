package org.idvairaz.menu;

import org.idvairaz.model.User;
import org.idvairaz.service.AuditService;
import org.idvairaz.service.AuthService;
import org.idvairaz.model.User.UserRole;

import java.util.Map;
import java.util.Scanner;

/**
 * Меню управления пользователями (только для администраторов).
 * Содержит функции управления пользователями и их ролями.
 *
 * @author idvavraz
 * @version 1.0
 */
public class UserManagementMenu {

    /** Сервис аутентификации и авторизации */
    private final AuthService authService;

    /** Сервис для ведения журнала аудита */
    private final AuditService auditService;

    /** Сканер для ввода данных от пользователя */
    private final Scanner scanner;


    /** Меню аутентификации */
    private final AuthMenu authMenu;

    /**
     * Конструктор меню управления пользователями.
     *
     * @param authService сервис аутентификации и авторизации
     * @param auditService сервис для ведения журнала аудита
     * @param authMenu меню аутентификации
     * @param scanner сканер для ввода данных от пользователя
     */public UserManagementMenu(AuthService authService, AuditService auditService, AuthMenu authMenu, Scanner scanner) {
        this.authService = authService;
        this.auditService = auditService;
        this.authMenu = authMenu;
        this.scanner = scanner;
    }

    /**
     * Запускает меню управления пользователями.
     */
    public void manageUsers() {
        boolean inUserManagement = true;

        while (inUserManagement && authService.isAdmin()) {
            showUserManagementMenu();
            int choice = authMenu.readChoice();

            if (choice == 0) {
                inUserManagement = false;
            } else {
                processUserManagementChoice(choice);
            }
        }
    }

    /**
     * Отображает меню управления пользователями.
     */
    private void showUserManagementMenu() {
        System.out.print("""
            \n=== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ===
            1. Показать всех пользователей
            2. Добавить пользователя
            3. Изменить роль пользователя
            4. Удалить пользователя
            0. Назад в главное меню
            Выберите действие:\s""");
    }

    /**
     * Обрабатывает выбор пользователя в меню управления пользователями.
     *
     * @param choice выбранный пункт меню
     */
    private void processUserManagementChoice(int choice) {
        String username = authService.getCurrentUsername();

        switch (choice) {
            case 1 -> {
                auditService.logAction(username, "ПРОСМОТР_ПОЛЬЗОВАТЕЛЕЙ", "Просмотр списка пользователей");
                showAllUsers();
            }
            case 2 -> {
                auditService.logAction(username, "ДОБАВЛЕНИЕ_ПОЛЬЗОВАТЕЛЯ", "Начало процесса");
                addUserWithRole();
            }
            case 3 -> {
                auditService.logAction(username, "ИЗМЕНЕНИЕ_РОЛИ", "Начало процесса");
                changeUserRole();
            }
            case 4 -> {
                auditService.logAction(username, "УДАЛЕНИЕ_ПОЛЬЗОВАТЕЛЯ", "Начало процесса");
                deleteUser();
            }
            case 0 -> {
                auditService.logAction(username, "ВЫХОД_ИЗ_УПРАВЛЕНИЯ", "Возврат в главное меню");
                System.out.println("Возврат в главное меню...");
            }
            default -> {
                auditService.logAction(username, "НЕВЕРНЫЙ_ВЫБОР_УПРАВЛЕНИЯ", "Выбрана несуществующая опция: " + choice);
                System.out.println("Неверный выбор!");
            }
        }
    }

    /**
     * Отображает список всех пользователей системы.
     */
    private void showAllUsers() {
        System.out.println("\n=== СПИСОК ПОЛЬЗОВАТЕЛЕЙ ===");
        Map<String, User> users = authService.getUsers();

        if (users.isEmpty()) {
            System.out.println("Пользователей нет");
        } else {
            System.out.println("Всего пользователей: " + users.size());
            users.forEach((username, user) -> {
                System.out.printf("- %s [%s] %s%n",
                        username,
                        user.getRole(),
                        user.isLoggedIn() ? "(online)" : ""
                );
            });
        }
    }

    /**
     * Добавляет нового пользователя с заданной ролью.
     */
    private void addUserWithRole() {
        System.out.println("\n=== ДОБАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ===");

        System.out.print("Введите имя пользователя: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Ошибка: имя пользователя не может быть пустым");
            return;
        }

        System.out.print("Введите пароль: ");
        String password = scanner.nextLine().trim();

        if (password.isEmpty()) {
            System.out.println("Ошибка: пароль не может быть пустым");
            return;
        }

        System.out.print("""
            Выберите роль:
            1. USER - Обычный пользователь
            2. MANAGER - Менеджер
            3. ADMIN - Администратор
            Введите номер роли:\s""");

        try {
            int roleChoice = scanner.nextInt();
            scanner.nextLine();

            UserRole role;
            switch (roleChoice) {
                case 1 -> role = UserRole.USER;
                case 2 -> role = UserRole.MANAGER;
                case 3 -> role = UserRole.ADMIN;
                default -> {
                    System.out.println("Ошибка: неверный выбор роли");
                    return;
                }
            }

            if (authService.registerWithRole(username, password, role)) {
                System.out.println("Пользователь " + username + " успешно создан с ролью " + role);
                auditService.logAction(authService.getCurrentUsername(), "УСПЕШНОЕ_ДОБАВЛЕНИЕ_ПОЛЬЗОВАТЕЛЯ",
                        "Создан пользователь: " + username + " с ролью: " + role);
            } else {
                System.out.println("Ошибка: не удалось создать пользователя. Возможно, пользователь с таким именем уже существует.");
                auditService.logAction(authService.getCurrentUsername(), "ОШИБКА_ДОБАВЛЕНИЯ_ПОЛЬЗОВАТЕЛЯ",
                        "Не удалось создать пользователя: " + username);
            }

        } catch (Exception e) {
            System.out.println("Ошибка: неверный формат ввода");
            scanner.nextLine();
        }
    }

    /**
     * Изменяет роль существующего пользователя.
     */
    private void changeUserRole() {
        System.out.println("\n=== ИЗМЕНЕНИЕ РОЛИ ПОЛЬЗОВАТЕЛЯ ===");

        showAllUsers();

        System.out.print("\nВведите имя пользователя для изменение роли: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Ошибка: имя пользователя не может быть пустым");
            return;
        }

        Map<String, User> users = authService.getUsers();
        if (!users.containsKey(username)) {
            System.out.println("Ошибка: пользователь " + username + " не найден");
            return;
        }

        if (username.equals(authService.getCurrentUsername())) {
            System.out.println("Ошибка: нельзя изменять роль самому себе");
            return;
        }

        System.out.printf("""
            Выберите новую роль для пользователя %s:
            1. USER - Обычный пользователь
            2. MANAGER - Менеджер
            3. ADMIN - Администратор
            Введите номер роли:\s""", username);

        try {
            int roleChoice = scanner.nextInt();
            scanner.nextLine();

            User.UserRole newRole;
            switch (roleChoice) {
                case 1 -> newRole = User.UserRole.USER;
                case 2 -> newRole = User.UserRole.MANAGER;
                case 3 -> newRole = User.UserRole.ADMIN;
                default -> {
                    System.out.println("Ошибка: неверный выбор роли");
                    return;
                }
            }

            if (authService.changeUserRole(username, newRole)) {
                System.out.println("Роль пользователя " + username + " успешно изменена на " + newRole);
                auditService.logAction(authService.getCurrentUsername(), "УСПЕШНОЕ_ИЗМЕНЕНИЕ_РОЛИ",
                        "Пользователь: " + username + ", новая роль: " + newRole);
            } else {
                System.out.println("Ошибка: не удалось изменить роль пользователя");
                auditService.logAction(authService.getCurrentUsername(), "ОШИБКА_ИЗМЕНЕНИЯ_РОЛИ",
                        "Не удалось изменить роль пользователя: " + username);
            }

        } catch (Exception e) {
            System.out.println("Ошибка: неверный формат ввода");
            scanner.nextLine();
        }
    }

    /**
     * Удаляет пользователя из системы.
     */
    private void deleteUser() {
        System.out.println("\n=== УДАЛЕНИЕ ПОЛЬЗОВАТЕЛЯ ===");

        showAllUsers();

        System.out.print("\nВведите имя пользователя для удаления: ");
        String username = scanner.nextLine().trim();

        if (username.isEmpty()) {
            System.out.println("Ошибка: имя пользователя не может быть пустым");
            return;
        }

        Map<String, User> users = authService.getUsers();
        if (!users.containsKey(username)) {
            System.out.println("Ошибка: пользователь " + username + " не найден");
            return;
        }

        System.out.print("Вы уверены, что хотите удалить пользователя " + username + "? (да/нет): ");
        String confirmation = scanner.nextLine().trim().toLowerCase();

        if (confirmation.equals("да") || confirmation.equals("yes") || confirmation.equals("y")) {
            if (authService.deleteUser(username)) {
                System.out.println("Пользователь " + username + " успешно удален");
                auditService.logAction(authService.getCurrentUsername(), "УСПЕШНОЕ_УДАЛЕНИЕ_ПОЛЬЗОВАТЕЛЯ",
                        "Удален пользователь: " + username);
            } else {
                System.out.println("Ошибка: не удалось удалить пользователя");
                auditService.logAction(authService.getCurrentUsername(), "ОШИБКА_УДАЛЕНИЕ_ПОЛЬЗОВАТЕЛЯ",
                        "Не удалось удалить пользователя: " + username);
            }
        } else {
            System.out.println("Удаление отменено");
            auditService.logAction(authService.getCurrentUsername(), "ОТМЕНА_УДАЛЕНИЯ",
                    "Отменено удаление пользователя: " + username);
        }
    }
}
