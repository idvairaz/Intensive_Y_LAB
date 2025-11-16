package org.idvairaz.menu;

import org.idvairaz.service.AuditService;
import org.idvairaz.service.AuthService;


import java.util.Scanner;

/**
 * Меню аутентификации и регистрации пользователей.
 * Отвечает за процессы входа, регистрации и управления сессиями.
 *
 * @author idvavraz
 * @version 1.0
 */
public class AuthMenu {

    private final AuthService authService;
    private final AuditService auditService;
    private final Scanner scanner;

    public AuthMenu(AuthService authService, AuditService auditService, Scanner scanner) {
        this.authService = authService;
        this.auditService = auditService;
        this.scanner = scanner;
    }

    /**
     * Отображает меню авторизации до успешного входа в систему.
     */
    public void showAuthMenu() {
        while (!authService.isAuthenticated()) {
            System.out.println("\n=== МЕНЮ АВТОРИЗАЦИИ ===");
            System.out.println("1. Войти");
            System.out.println("2. Зарегистрироваться");
            System.out.println("3. Выйти из приложения");
            System.out.print("Выберите действие: ");

            int choice = readChoice();
            processAuthChoice(choice);
        }
    }

    /**
     * Обрабатывает выбор пользователя в меню авторизации.
     *
     * @param choice выбранный пункт меню
     */
    private void processAuthChoice(int choice) {
        switch (choice) {
            case 1 -> login();
            case 2 -> register();
            case 3 -> {
                System.out.println("Выход из приложения.");
                System.exit(0);
            }
            default -> System.out.println("Неверный выбор! Попробуйте снова.");
        }
    }

    /**
     * Выполняет процесс входа пользователя в систему.
     * Предоставляет 3 попытки для ввода правильных учетных данных.
     */
    private void login() {
        System.out.println("\n=== ВХОД В СИСТЕМУ ===");
        int attempts = 3;

        while (attempts > 0 && !authService.isAuthenticated()) {
            System.out.print("Логин: ");
            String username = scanner.nextLine();

            System.out.print("Пароль: ");
            String password = scanner.nextLine();

            if (authService.login(username, password)) {
                auditService.logAction(username, "ВХОД", "Успешная авторизация");
                System.out.println("Добро пожаловать, " + username + "!");
            } else {
                attempts--;
                System.out.println("Неверный логин или пароль. Осталось попыток: " + attempts);
                auditService.logAction(username, "НЕУДАЧНАЯ_ПОПЫТКА_ВХОДА", "Неверные учетные данные");
            }
        }

        if (!authService.isAuthenticated()) {
            System.out.println("Превышено количество попыток. Попробуйте позже.");
        }
    }

    /**
     * Выполняет процесс регистрации нового пользователя.
     */
    private void register() {
        System.out.println("\n=== РЕГИСТРАЦИЯ ===");

        System.out.print("Придумайте логин: ");
        String username = scanner.nextLine();

        System.out.print("Придумайте пароль: ");
        String password = scanner.nextLine();

        if (authService.register(username, password)) {
            System.out.println("Регистрация успешна! Теперь вы можете войти в систему.");
            auditService.logAction(username, "РЕГИСТРАЦИЯ", "Новый пользователь зарегистрирован");
        } else {
            System.out.println("Ошибка регистрации. Возможно, пользователь с таким логином уже существует.");
            auditService.logAction(username, "НЕУДАЧНАЯ_РЕГИСТРАЦИЯ", "Попытка создать существующего пользователя");
        }
    }

    /**
     * Читает выбор пользователя из консоли.
     *
     * @return числовой выбор пользователя или -1 при ошибке
     */
    public int readChoice() {
        try {
            return scanner.nextInt();
        } catch (Exception e) {
            scanner.nextLine();
            return -1;
        } finally {
            scanner.nextLine();
        }
    }
}
