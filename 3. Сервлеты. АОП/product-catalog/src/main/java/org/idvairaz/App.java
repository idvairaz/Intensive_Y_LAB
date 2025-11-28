package org.idvairaz;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.LiquibaseException;
import liquibase.resource.ClassLoaderResourceAccessor;

import org.idvairaz.config.DatabaseConfig;
import org.idvairaz.web.EmbeddedTomcatServer;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.sql.Statement;


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
     * Встроенный Tomcat сервер для обработки HTTP запросов.
     * Обеспечивает работу REST API через сервлеты.
     * Инициализируется при запуске приложения и останавливается при завершении.
     */
    private static EmbeddedTomcatServer tomcatServer;


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

        try (Connection connection = DatabaseConfig.getConnection()) {

            createLiquibaseSchema(connection);

            Database database = DatabaseFactory.getInstance()
                    .findCorrectDatabaseImplementation(new JdbcConnection(connection));

            database.setLiquibaseSchemaName(DatabaseConfig.getLiquibaseSchema());
            database.setDefaultSchemaName(DatabaseConfig.getSchema());

            database.setLiquibaseSchemaName(DatabaseConfig.getLiquibaseSchema());
            database.setDefaultSchemaName(DatabaseConfig.getSchema());

            System.out.printf("""
                    Служебные таблицы Liquibase будут в схеме: %s
                    Таблицы приложения будут в схеме: %s
                    Логи действий пользователей будут в схеме: %s
                    """,
                    DatabaseConfig.getLiquibaseSchema(), DatabaseConfig.getSchema(),
                    DatabaseConfig.getAuditSchema(), DatabaseConfig.getAuditSchema());

            Liquibase liquibase = new Liquibase(
                    "db/changelog/changelog-master.xml",
                    new ClassLoaderResourceAccessor(),
                    database
            );

            liquibase.update();

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
     * Создает схему для служебных таблиц Liquibase.
     * Если схема уже существует, операция игнорируется.
     * @param connection активное соединение с базой данных
     * @throws SQLException если произошла ошибка при создании схемы
     */
    private static void createLiquibaseSchema(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS " + DatabaseConfig.getLiquibaseSchema());
        } catch (SQLException e) {
            System.err.println("Ошибка создания схемы '" + DatabaseConfig.getLiquibaseSchema() + "': " + e.getMessage());
            throw e;
        }
    }

    /**
     * Проверяет подключение к базе данных и выводит статистику пула соединений.
     *
     * @throws RuntimeException если не удалось установить подключение к БД
     */
    private static void testDatabaseConnection() {

        try (Connection connection = DatabaseConfig.getConnection()) {
            DatabaseMetaData databaseMetaData = connection.getMetaData();

            System.out.printf("""
                Успешное подключение к базе данных:
                  - URL: %s
                  - База данных: %s
                  - Версия: %s
                  - Схемы: %s, %s, %s
                """,
                    databaseMetaData.getURL(),
                    databaseMetaData.getDatabaseProductName(),
                    databaseMetaData.getDatabaseProductVersion(),
                    DatabaseConfig.getSchema(),
                    DatabaseConfig.getAuditSchema(),
                    DatabaseConfig.getLiquibaseSchema());

        } catch (SQLException e) {
            String errorMsg = "Ошибка подключения к базе данных: " + e.getMessage();
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
                if (tomcatServer != null) {
                    tomcatServer.stop();
                }
                DatabaseConfig.close();
                System.out.println("Ресурсы приложения корректно освобождены");
            } catch (Exception e) {
                System.err.println("Ошибка при завершении работы: " + e.getMessage());
            }
        }));
    }

    /**
     * Запускает встроенный Tomcat сервер для REST API.
     */
    private static void startEmbeddedTomcat() {
        try {
            tomcatServer = new EmbeddedTomcatServer(8080);
            tomcatServer.startInBackground();

            Thread.sleep(1000);

        } catch (Exception e) {
            System.err.println("Не удалось запустить Tomcat: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Точка входа в приложение "Каталог товаров маркетплейса".
     * Инициализирует базу данных, выполняет миграции и запускает REST API сервер.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        registerShutdownHook();

        try {
            System.out.println("=== ЗАПУСК ПРИЛОЖЕНИЯ КАТАЛОГ ТОВАРОВ ===");
            System.out.println("Версия: 3.0 (REST API + PostgreSQL + AOP)");

            testDatabaseConnection();
            runLiquibaseMigrations();
            startEmbeddedTomcat();

            System.out.printf("""
            \nПриложение успешно запущено!
            Для остановки нажмите Ctrl+C
            """);

            Thread.currentThread().join();

        } catch (Exception e) {
            System.err.println("\nКритическая ошибка при запуске приложения: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } finally {
            DatabaseConfig.close();
        }
    }

}
