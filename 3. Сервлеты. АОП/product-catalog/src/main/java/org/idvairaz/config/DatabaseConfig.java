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

            config.setJdbcUrl(properties.getProperty("db.url"));
            config.setUsername(properties.getProperty("db.username"));
            config.setPassword(properties.getProperty("db.password"));

            config.setMaximumPoolSize(3);
            config.setMinimumIdle(1);
            config.setConnectionTimeout(5000);

            dataSource = new HikariDataSource(config);

        } catch (Exception e) {
            throw new RuntimeException("Ошибка инициализации пула соединений", e);
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
     * Возвращает имя схемы для таблиц Liquibase.
     * Значение загружается из свойства 'liquibase.liquibase-schema' в application.properties.
     * Если свойство не задано, возвращает значение по умолчанию 'liquibase_schema'.
     *
     * @return имя схемы для таблиц Liquibase, не может быть null
     */
    public static String getLiquibaseSchema() {
        return properties.getProperty("liquibase.liquibase-schema", "liquibase_schema");
    }

    /**
     * Возвращает имя схемы для таблиц аудита действий пользователей.
     * Значение загружается из свойства 'liquibase.audit-schema' в application.properties.
     * Если свойство не задано, возвращает значение по умолчанию 'audit'.
     *
     * @return имя схемы для таблиц аудита действий пользователей, не может быть null
     */
    public static String getAuditSchema() {
        return properties.getProperty("liquibase.audit-schema", "audit");
    }
}


