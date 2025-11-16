package org.idvairaz.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Утилитарный класс для управления подключением к базе данных.
 * Загружает конфигурацию из файла application.properties и предоставляет
 * методы для установления соединения с PostgreSQL через пул соединений HikariCP.
 *
 * @author idvavraz
 * @version 1.0
 */
 public class DatabaseConfig {

    /**
     * Хранилище свойств конфигурации, загруженных из application.properties.
     * Инициализируется в статическом блоке и остается неизменным в течение
     * работы приложения.
     */
    private static final Properties properties = new Properties();

    /**
     * Пул соединений HikariCP для эффективного управления подключениями к БД.
     * Инициализируется один раз при загрузке класса и используется throughout
     * всего жизненного цикла приложения.
     */
    private static HikariDataSource dataSource;

    /**
     * Статический блок инициализации, выполняемый при первой загрузке класса.
     * Загружает конфигурационные параметры из файла application.properties
     * и инициализирует пул соединений HikariCP.
     *
     * @throws RuntimeException если файл конфигурации не найден или произошла ошибка ввода-вывода
     */
    static {
        try (InputStream input = DatabaseConfig.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (input == null) {
                throw new RuntimeException("Не удалось найти application.properties");
            }
            properties.load(input);
            System.out.println("Конфигурация базы данных успешно загружена");

            // Инициализируем пул соединений после загрузки конфигурации
            initializeConnectionPool();

        } catch (IOException e) {
            throw new RuntimeException("Ошибка загрузки конфигурации базы данных", e);
        }
    }

    /**
     * Инициализирует и настраивает пул соединений HikariCP с параметрами
     * из конфигурационного файла. Использует оптимальные настройки для PostgreSQL.
     *
     * @throws RuntimeException если не удалось инициализировать пул соединений
     */
    private static void initializeConnectionPool() {
        try {
            HikariConfig config = new HikariConfig();

            /* Основные настройки подключения*/
            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.username"));
            config.setPassword(properties.getProperty("db.password"));

            /* Настройки пула соединений с значениями по умолчанию */
            config.setMaximumPoolSize(getIntProperty("db.pool.maximumPoolSize", 10));
            config.setMinimumIdle(getIntProperty("db.pool.minimumIdle", 2));
            config.setConnectionTimeout(getLongProperty("db.pool.connectionTimeout", 30000));
            config.setIdleTimeout(getLongProperty("db.pool.idleTimeout", 600000));
            config.setMaxLifetime(getLongProperty("db.pool.maxLifetime", 1800000));

            /* Оптимизации для PostgreSQL */
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");

            /*  Настройка имени пула для мониторинга */
            config.setPoolName("MarketplacePostgreSQLPool");

            dataSource = new HikariDataSource(config);
            System.out.println("Пул соединений HikariCP успешно инициализирован");
            System.out.println("URL базы данных: " + config.getJdbcUrl());
            System.out.println("Размер пула: " + config.getMaximumPoolSize());

        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации пула соединений HikariCP", e);
        }
    }

    /**
     * Устанавливает и возвращает соединение с базой данных PostgreSQL из пула.
     * Автоматически устанавливает схему по умолчанию для каждого соединения.
     *
     * @return активное соединение с базой данных
     * @throws SQLException если произошла ошибка при установлении соединения:
     *         - неверные учетные данные
     *         - база данных недоступна
     *         - сетевые проблемы
     *         - превышен лимит соединений в пуле
     */
    public static Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();

        // Устанавливаем схему по умолчанию для этого соединения
        try (var stmt = connection.createStatement()) {
            stmt.execute("SET search_path TO " + getSchema());
        }

        return connection;
    }

    /**
     * Возвращает имя схемы базы данных, используемой приложением.
     * Значение загружается из свойства 'db.schema' в application.properties.
     * Если свойство не задано, возвращает значение по умолчанию 'marketplace'.
     *
     * @return имя схемы базы данных, не может быть null
     */
    public static String getSchema() {
        return properties.getProperty("db.schema", "marketplace");
    }

    /**
     * Закрывает пул соединений и освобождает все ресурсы.
     * Должен вызываться при завершении работы приложения для корректного
     * закрытия всех активных соединений с базой данных.
     */
    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            System.out.println("Пул соединений HikariCP успешно закрыт");
        }
    }

    /**
     * Возвращает статистику пула соединений для мониторинга и отладки.
     * Включает информацию об активных, idle и ожидающих соединениях.
     *
     * @return строка с детальной статистикой пула соединений
     */
    public static String getPoolStatistics() {
        if (dataSource == null || dataSource.isClosed()) {
            return "Пул соединений не инициализирован или закрыт";
        }

        return String.format(
                "Статистика пула соединений:%n" +
                        "  - Активные соединения: %d%n" +
                        "  - Простаивающие соединения: %d%n" +
                        "  - Всего соединений: %d%n" +
                        "  - Потоков в ожидании: %d%n" +
                        "  - Состояние: %s",
                dataSource.getHikariPoolMXBean().getActiveConnections(),
                dataSource.getHikariPoolMXBean().getIdleConnections(),
                dataSource.getHikariPoolMXBean().getTotalConnections(),
                dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(),
                dataSource.isRunning() ? "работает" : "остановлен"
        );
    }

    /**
     * Вспомогательный метод для получения целочисленного свойства с значением по умолчанию.
     *
     * @param key ключ свойства
     * @param defaultValue значение по умолчанию
     * @return значение свойства или значение по умолчанию
     */
    private static int getIntProperty(String key, int defaultValue) {
        try {
            return Integer.parseInt(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            System.err.printf("Некорректное значение свойства %s, используется значение по умолчанию: %d%n",
                    key, defaultValue);
            return defaultValue;
        }
    }

    /**
     * Вспомогательный метод для получения long свойства с значением по умолчанию.
     *
     * @param key ключ свойства
     * @param defaultValue значение по умолчанию
     * @return значение свойства или значение по умолчанию
     */
    private static long getLongProperty(String key, long defaultValue) {
        try {
            return Long.parseLong(properties.getProperty(key, String.valueOf(defaultValue)));
        } catch (NumberFormatException e) {
            System.err.printf("Некорректное значение свойства %s, используется значение по умолчанию: %d%n",
                    key, defaultValue);
            return defaultValue;
        }
    }
}


