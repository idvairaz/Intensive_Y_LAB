package org.idvairaz;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.idvairaz.cache.ProductCacheService;
import org.idvairaz.cache.impl.ProductCacheServiceImpl;
import org.idvairaz.config.DatabaseConfig;
import org.idvairaz.menu.ConsoleMenu;
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

import java.sql.Connection;
import java.sql.SQLException;


/**
 * Главный класс приложения "Каталог товаров маркетплейса".
 * Реализует паттерн Dependency Injection для компоновки компонентов системы.
 * Архитектура приложения построена по принципу разделения ответственности:
 * - Model: данные (Product, User)
 * - Repository: доступ к данным (ProductRepository)
 * - Service: бизнес-логика (ProductService, AuthService, etc.)
 * - Menu: пользовательский интерфейс (ConsoleMenu)
 *
 * Приложение использует PostgreSQL для хранения данных, Liquibase для миграций
 * и HikariCP для управления пулом соединений.
 *
 * @author idvavraz
 * @version 2.0
 */
public class App {

    /**
     * Выполняет миграции базы данных с использованием Liquibase.
     * Создает служебную схему 'audit' для таблиц Liquibase и выполняет
     * все невыполненные миграции из changelog-master.xml.
     *
     * @throws RuntimeException если произошла ошибка при выполнении миграций
     *
     *  Служебные таблицы Liquibase создаются в схеме 'audit',
     *           таблицы приложения - в схеме указанной в конфигурации
     */
    private static void runLiquibaseMigrations() {
        System.out.println("\n=== ЗАПУСК МИГРАЦИЙ LIQUIBASE ===");

        try (Connection connection = DatabaseConfig.getConnection()) {

            createAuditSchema(connection);

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            database.setLiquibaseSchemaName(DatabaseConfig.getLiquibaseSchema());
            database.setDefaultSchemaName(DatabaseConfig.getSchema());

            System.out.println("Схема для таблиц Liquibase: " + DatabaseConfig.getLiquibaseSchema());
            System.out.println("Схема по умолчанию для приложения: " + DatabaseConfig.getSchema());

            Liquibase liquibase = new Liquibase(
                    "db/changelog/changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update();
            System.out.println("Миграции Liquibase успешно выполнены");

        } catch (SQLException e) {
            String errorMsg = "Ошибка подключения к базе данных при выполнении миграций: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        } catch (LiquibaseException e) {
            String errorMsg = "Ошибка выполнения миграций Liquibase: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Неожиданная ошибка при выполнении миграций: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Создает схему 'audit' для служебных таблиц Liquibase.
     * Если схема уже существует, операция игнорируется.
     *
     * @param connection активное соединение с базой данных
     * @throws SQLException если произошла ошибка при создании схемы
     */
    private static void createAuditSchema(Connection connection) throws SQLException {
        try (var statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS audit");
            System.out.println("Схема 'audit' создана для системных таблиц Liquibase");
        } catch (SQLException e) {
            System.err.println("Ошибка создания схемы 'audit': " + e.getMessage());
            throw e;
        }
    }

    /**
     * Проверяет подключение к базе данных и выводит статистику пула соединений.
     *
     * @throws RuntimeException если не удалось установить подключение к БД
     */
    private static void testDatabaseConnection() {
        System.out.println("\n=== ПРОВЕРКА ПОДКЛЮЧЕНИЯ К БАЗЕ ДАННЫХ ===");

        try (Connection connection = DatabaseConfig.getConnection()) {
            var databaseMetaData = connection.getMetaData();
            String connectionInfo = """
                Успешное подключение к базе данных:
                  - URL: %s
                  - База данных: %s
                  - Версия: %s
                  - Схема: %s
                """.formatted(
                    databaseMetaData.getURL(),
                    databaseMetaData.getDatabaseProductName(),
                    databaseMetaData.getDatabaseProductVersion(),
                    DatabaseConfig.getSchema()
            );

            System.out.println(connectionInfo);
            System.out.println(DatabaseConfig.getPoolStatistics());

        } catch (SQLException e) {
            String errorMsg = "Ошибка подключения к базе данных: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Инициализирует и настраивает все компоненты приложения по принципу Dependency Injection.
     * Создает цепочку зависимостей: Repository → Service → Menu.
     *
     * @return сконфигурированный экземпляр ConsoleMenu готовый к запуску
     */
    private static ConsoleMenu initializeApplication() {
        System.out.println("\n=== ИНИЦИАЛИЗАЦИЯ ПРИЛОЖЕНИЯ ===");

        try {
            MetricsService metricsService = new MetricsService();
            System.out.println("Сервисы утилиты инициализированы");

            ProductRepository productRepository = new PostgresProductRepository();
            UserRepository userRepository = new PostgresUserRepository();
            AuditRepository auditRepository = new PostgresAuditRepository();
            System.out.println("Репозитории данных инициализированы");

            ProductCacheService cacheService = new ProductCacheServiceImpl();

            ProductService productService = new ProductService(productRepository, metricsService, cacheService);
            UserService userService = new UserService(userRepository);
            AuditService auditService = new AuditService(auditRepository);
            AuthService authService = new AuthService(userService);
            System.out.println("Бизнес-сервисы инициализированы");


            ConsoleMenu consoleMenu = new ConsoleMenu(productService, authService, auditService, metricsService);
            System.out.println("Пользовательский интерфейс инициализирован");

            return consoleMenu;

        } catch (Exception e) {
            String errorMsg = "Ошибка инициализации приложения: " + e.getMessage();
            System.err.println(errorMsg);
            throw new RuntimeException(errorMsg, e);
        }
    }

    /**
     * Регистрирует обработчик для корректного завершения работы приложения.
     * Обеспечивает закрытие пула соединений и других ресурсов при выходе.
     */
    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\n=== ЗАВЕРШЕНИЕ РАБОТЫ ПРИЛОЖЕНИЯ ===");
            try {
                DatabaseConfig.close();
                System.out.println("Ресурсы приложения корректно освобождены");
            } catch (Exception e) {
                System.err.println("Ошибка при завершении работы: " + e.getMessage());
            }
        }));
    }

    /**
     * Точка входа в приложение. Выполняет компоновку зависимостей по принципу DI.
     * Порядок инициализации:
     * 1. Проверка подключения к базе данных
     * 2. Выполнение миграций Liquibase
     * 3. Инициализация компонентов приложения
     * 4. Запуск главного меню
     * 5. Корректное освобождение ресурсов при завершении
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        System.out.println("=== КАТАЛОГ ТОВАРОВ МАРКЕТПЛЕЙСА ===");
        System.out.println("Версия: 2.0 (PostgreSQL + Liquibase)");

        registerShutdownHook();

        try {
            testDatabaseConnection();
            runLiquibaseMigrations();
            ConsoleMenu menu = initializeApplication();

            System.out.println("\n=== ЗАПУСК ПРИЛОЖЕНИЯ ===");
            menu.start();

        } catch (Exception e) {
            System.err.println("\nКритическая ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            DatabaseConfig.close();
        }
    }
}
