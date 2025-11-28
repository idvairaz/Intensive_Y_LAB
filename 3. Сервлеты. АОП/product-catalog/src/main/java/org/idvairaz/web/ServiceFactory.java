package org.idvairaz.web;

import org.idvairaz.cache.ProductCacheService;
import org.idvairaz.config.CacheConfig;
import org.idvairaz.repository.AuditRepository;
import org.idvairaz.repository.ProductRepository;
import org.idvairaz.repository.UserRepository;
import org.idvairaz.repository.impl.PostgresAuditRepository;
import org.idvairaz.repository.impl.PostgresProductRepository;
import org.idvairaz.repository.impl.PostgresUserRepository;
import org.idvairaz.service.AuditService;
import org.idvairaz.service.AuthService;
import org.idvairaz.service.MetricsService;
import org.idvairaz.service.ProductService;
import org.idvairaz.service.UserService;

/**
 * Фабрика для создания и управления сервисами приложения.
 * Реализует паттерн Singleton для обеспечения единственного экземпляра каждого сервиса.
 * Обеспечивает ленивую инициализацию сервисов при первом обращении.
 *
 * @author idvavraz
 * @version 1.0
 */
public class ServiceFactory {

    /**
     * Единственный экземпляр сервиса для работы с товарами.
     */
    private static ProductService productService;

    /**
     * Единственный экземпляр сервиса для работы с пользователями.
     */
    private static UserService userService;

    /**
     * Единственный экземпляр сервиса для работы с аудитом.
     */
    private static AuditService auditService;

    /**
     * Единственный экземпляр сервиса для аутентификации.
     */
    private static AuthService authService;

    /**
     * Возвращает экземпляр сервиса для работы с товарами.
     * При первом вызове инициализирует сервис с зависимостями:
     * - MetricsService для сбора метрик
     * - PostgresProductRepository для работы с базой данных
     * - ProductCacheService для кэширования
     *
     * @return экземпляр ProductService готовый к использованию
     * @throws RuntimeException если произошла ошибка при инициализации сервиса
     */
    public static ProductService getProductService() {
        if (productService == null) {
            try {
                System.out.println("Инициализация ProductService...");

                MetricsService metricsService = new MetricsService();
                ProductRepository productRepository = new PostgresProductRepository();
                CacheConfig cacheConfig = new CacheConfig();
                ProductCacheService cacheService = cacheConfig.createProductCacheService();

                productService = new ProductService(productRepository, metricsService, cacheService);
                System.out.println("ProductService успешно инициализирован");

            } catch (Exception e) {
                System.err.println("Ошибка инициализации ProductService: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Не удалось инициализировать ProductService", e);
            }
        }
        return productService;
    }

    /**
     * Возвращает экземпляр сервиса для работы с аудитом.
     * При первом вызове инициализирует сервис с зависимостью:
     * - PostgresAuditRepository для работы с таблицей аудита в базе данных
     *
     * @return экземпляр AuditService готовый к использованию
     */
    public static AuditService getAuditService() {
        if (auditService == null) {
            AuditRepository auditRepository = new PostgresAuditRepository();
            auditService = new AuditService(auditRepository);
        }
        return auditService;
    }

    /**
     * Возвращает экземпляр сервиса для работы с пользователями.
     *
     * @return экземпляр UserService готовый к использованию
     */
    public static UserService getUserService() {
        if (userService == null) {
            UserRepository userRepository = new PostgresUserRepository();
            userService = new UserService(userRepository);
        }
        return userService;
    }

    /**
     * Возвращает экземпляр сервиса для аутентификации.
     *
     * @return экземпляр AuthService готовый к использованию
     */
    public static AuthService getAuthService() {
        if (authService == null) {
            UserService userService = getUserService();
            authService = new AuthService(userService);
        }
        return authService;
    }
}