package org.idvairaz;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Базовый класс для интеграционных тестов с Testcontainers.
 * Настраивает PostgreSQL контейнер для тестирования репозиториев.
 *
 * Контейнер автоматически запускается перед тестами и останавливается после.
 * Каждый тестовый класс получает чистую БД для изолированного тестирования.
 *
 * @author idvavraz
 * @version 1.0
 */
@Testcontainers
public abstract class BaseRepositoryTest {

    /**
     * Docker контейнер с PostgreSQL 15.
     * Автоматически управляется Testcontainers.
     */
    @Container
    protected static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("test_db")
            .withUsername("test_user")
            .withPassword("test_pass");

    protected static DataSource dataSource;

    /**
     * Настройка тестового окружения перед всеми тестами.
     * Устанавливает системные свойства для подключения к тестовой БД.
     */
    @BeforeAll
    static void beforeAll() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(postgres.getJdbcUrl());
        config.setUsername(postgres.getUsername());
        config.setPassword(postgres.getPassword());
        config.setMaximumPoolSize(5);

        dataSource = new HikariDataSource(config);

        createTables();

        System.setProperty("db.url", postgres.getJdbcUrl());
        System.setProperty("db.username", postgres.getUsername());
        System.setProperty("db.password", postgres.getPassword());

        System.out.println("Тестовый PostgreSQL запущен: " + postgres.getJdbcUrl());
        System.out.println("Тестовые таблицы успешно созданы");
    }

    /**
     * Создание тестовых таблиц
     */
    private static void createTables() {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {

            String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                username VARCHAR(50) NOT NULL UNIQUE,
                password VARCHAR(255) NOT NULL,
                role VARCHAR(20) NOT NULL,
                is_logged_in BOOLEAN DEFAULT false
            )
            """;
            statement.execute(createUsersTable);

            String createProductsTable = """
                CREATE TABLE IF NOT EXISTS products (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    description TEXT,
                    price DECIMAL(10,2),
                    category VARCHAR(100),
                    brand VARCHAR(100),
                    stock_quantity INTEGER,
                    created_at TIMESTAMP,
                    updated_at TIMESTAMP
                )
                """;
            statement.execute(createProductsTable);

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при создании таблиц\"", e);
        }
    }

    /**
     * Очистка после всех тестов.
     */
    @AfterAll
    static void afterAll() {
        System.clearProperty("db.url");
        System.clearProperty("db.username");
        System.clearProperty("db.password");

        if (dataSource instanceof HikariDataSource) {
            ((HikariDataSource) dataSource).close();
        }
    }
}