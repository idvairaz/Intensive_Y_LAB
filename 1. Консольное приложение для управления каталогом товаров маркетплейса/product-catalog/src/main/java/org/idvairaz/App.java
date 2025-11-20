package org.idvairaz;


import org.idvairaz.menu.ConsoleMenu;
import org.idvairaz.repository.InMemoryProductRepository;
import org.idvairaz.repository.ProductRepository;
import org.idvairaz.service.AuditService;
import org.idvairaz.service.AuthService;
import org.idvairaz.service.MetricsService;
import org.idvairaz.service.ProductService;
/**
 * Главный класс приложения "Каталог товаров маркетплейса".
 * Реализует паттерн Dependency Injection для компоновки компонентов системы.
 * Архитектура приложения построена по принципу разделения ответственности:
 * - Model: данные (Product, User)
 * - Repository: доступ к данным (ProductRepository)
 * - Service: бизнес-логика (ProductService, AuthService, etc.)
 * - Menu: пользовательский интерфейс (ConsoleMenu)
 *
 * @author idvavraz
 * @version 1.0
 */

public class App {

    /**
     * Точка входа в приложение. Выполняет компоновку зависимостей по принципу DI.
     * Порядок инициализации:
     * 1. Сервисы утилиты (Metrics, Audit)
     * 2. Слой данных (Repository)
     * 3. Бизнес-сервисы (Product, Auth)
     * 4. Пользовательский интерфейс (ConsoleMenu)
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        MetricsService metricsService = new MetricsService();
        ProductRepository repository = new InMemoryProductRepository();
        ProductService productService = new ProductService(repository, metricsService);
        AuthService authService = new AuthService();
        AuditService auditService = new AuditService();

        ConsoleMenu menu = new ConsoleMenu(productService, authService, auditService, metricsService);
        menu.start();
    }
}

