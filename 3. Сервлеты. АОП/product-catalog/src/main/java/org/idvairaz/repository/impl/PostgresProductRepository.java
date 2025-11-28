package org.idvairaz.repository.impl;

import org.idvairaz.config.DatabaseConfig;
import org.idvairaz.model.Product;
import org.idvairaz.repository.ProductRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Реализация репозитория товаров для работы с PostgreSQL.
 * Обеспечивает сохранение, поиск и управление товарами в базе данных.
 * Использует JDBC для прямого взаимодействия с PostgreSQL.
 * Идентификаторы товаров генерируются через sequence product_seq.
 *
 *  @author idvavraz
 *  @version 1.0
 */
public class PostgresProductRepository implements ProductRepository {

    /** Имя схемы базы данных, используемой приложением */
    private final String schema = DatabaseConfig.getSchema();

    /**
     * Сохраняет или обновляет товар в базе данных.
     * Для новых товаров (id = null) генерирует идентификатор через sequence product_seq.
     * Для существующих товаров обновляет все поля, кроме created_at.
     *
     * @param product товар для сохранения или обновления
     * @return сохраненный товар с присвоенным ID (для новых) или обновленными данными
     * @throws RuntimeException если произошла ошибка SQL или товар для обновления не найден
     */
     @Override
    public Product save(Product product) {
        boolean isNew = product.getId() == null;
        String sql;

        try (Connection conn = DatabaseConfig.getConnection()) {
            if (isNew) {
                long newId = getNextIdFromSequence(conn);
                product.setId(newId);

                sql = "INSERT INTO " + schema + ".products (id, name, description, price, category, brand, stock_quantity, created_at, updated_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            } else {
                sql = "UPDATE " + schema + ".products SET name = ?, description = ?, price = ?, category = ?, brand = ?, stock_quantity = ?, updated_at = ? " +
                        "WHERE id = ?";
            }

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                if (isNew) {
                    stmt.setLong(1, product.getId());
                    stmt.setString(2, product.getName());
                    stmt.setString(3, product.getDescription());
                    stmt.setBigDecimal(4, product.getPrice());
                    stmt.setString(5, product.getCategory());
                    stmt.setString(6, product.getBrand());
                    stmt.setInt(7, product.getStockQuantity());
                    stmt.setTimestamp(8, Timestamp.valueOf(product.getCreatedAt()));
                    stmt.setTimestamp(9, Timestamp.valueOf(product.getUpdatedAt()));
                } else {
                    stmt.setString(1, product.getName());
                    stmt.setString(2, product.getDescription());
                    stmt.setBigDecimal(3, product.getPrice());
                    stmt.setString(4, product.getCategory());
                    stmt.setString(5, product.getBrand());
                    stmt.setInt(6, product.getStockQuantity());
                    stmt.setTimestamp(7, Timestamp.valueOf(product.getUpdatedAt()));
                    stmt.setLong(8, product.getId());
                }

                int affectedRows = stmt.executeUpdate();

                if (isNew) {
                    System.out.println("Создан товар с ID: " + product.getId() + " (из sequence)");
                } else if (affectedRows == 0) {
                    throw new RuntimeException("Товар с ID " + product.getId() + " не найден для обновления");
                }

                return product;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения товара '" + product.getName() + "'", e);
        }
    }

    /**
     * Получает следующий идентификатор из sequence product_seq.
     * Используется для генерации уникальных ID новых товаров.
     *
     * @param conn активное соединение с базой данных
     * @return следующий уникальный идентификатор из sequence
     * @throws SQLException если произошла ошибка при обращении к sequence
     *
     */
    private long getNextIdFromSequence(Connection conn) throws SQLException {
        String sequenceSql = "SELECT nextval('" + schema + ".product_seq')";
        try (PreparedStatement seqStmt = conn.prepareStatement(sequenceSql);
             ResultSet rs = seqStmt.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            } else {
                throw new SQLException("Не удалось получить значение из sequence product_seq");
            }
        }
    }

    /**
     * Находит товар по его идентификатору.
     *
     * @param id идентификатор товара для поиска
     * @return Optional с найденным товаром, или empty если товар не найден
     * @throws RuntimeException если произошла ошибка при выполнении запроса
     */
    @Override
    public Optional<Product> findById(Long id) {
        String sql = "SELECT * FROM " + schema + ".products WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска товара по ID: " + id, e);
        }

        return Optional.empty();
    }

    /**
     * Находит товар по точному совпадению названия без учета регистра.
     *
     * @param name название товара для поиска
     * @return Optional с найденным товаром, или empty если товар не найден
     * @throws RuntimeException если произошла ошибка при выполнении запроса
     */
    @Override
    public Optional<Product> findByName(String name) {
        String sql = "SELECT * FROM " + schema + ".products WHERE LOWER(name) = LOWER(?)";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска товара по имени: " + name, e);
        }

        return Optional.empty();
    }

    /**
     * Возвращает все товары из базы данных.
     * Товары возвращаются в порядке их идентификаторов.
     *
     * @return список всех товаров, может быть пустым если товаров нет
     * @throws RuntimeException если произошла ошибка при выполнении запроса
     */
    @Override
    public List<Product> findAll() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".products ORDER BY id";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения всех товаров", e);
        }

        return products;
    }

    /**
     * Удаляет товар по идентификатору.
     * Если товар с указанным ID не существует, метод не генерирует исключение, а операция считается успешной
     * и выводится информационное сообщение.
     *
     * @param id идентификатор товара для удаления
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     */
    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM " + schema + ".products WHERE id = ?";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setLong(1, id);
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                System.out.println("Товар с ID " + id + " не найден для удаления");
            } else {
                System.out.println("Товар с ID " + id + " удален");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления товара с ID: " + id, e);
        }
    }

    /**
     * Находит все товары указанной категории.
     * Поиск выполняется без учета регистра.
     *
     * @param category категория для поиска
     * @return список товаров указанной категории, может быть пустым
     * @throws RuntimeException если произошла ошибка при выполнении запроса
     */
    @Override
    public List<Product> findByCategory(String category) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".products WHERE LOWER(category) = LOWER(?) ORDER BY name";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, category);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска товаров по категории: " + category, e);
        }

        return products;
    }

    /**
     * Находит все товары указанного бренда.
     * Поиск выполняется без учета регистра.
     *
     * @param brand бренд для поиска
     * @return список товаров указанного бренда, может быть пустым
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     */
    @Override
    public List<Product> findByBrand(String brand) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM " + schema + ".products WHERE LOWER(brand) = LOWER(?) ORDER BY name";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, brand);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                products.add(mapResultSetToProduct(rs));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка поиска товаров по бренду: " + brand, e);
        }

        return products;
    }

    /**
     * Возвращает общее количество товаров в каталоге.
     *
     * @return количество товаров
     * @throws RuntimeException если произошла ошибка при выполнении запроса
     */
    @Override
    public int getTotalProductsCount() {
        String sql = "SELECT COUNT(*) FROM " + schema + ".products";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения общего количества товаров", e);
        }

        return 0;
    }

    /**
     * Возвращает количество уникальных категорий в каталоге.
     *
     * @return количество категорий
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     */
    @Override
    public int getTotalCategoriesCount() {
        String sql = "SELECT COUNT(DISTINCT category) FROM " + schema + ".products";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения количества категорий", e);
        }

        return 0;
    }

    /**
     * Возвращает количество уникальных брендов в каталоге.
     *
     * @return количество брендов
     * @throws RuntimeException если произошла ошибка при выполнении SQL запроса
     */
    @Override
    public int getTotalBrandsCount() {
        String sql = "SELECT COUNT(DISTINCT brand) FROM " + schema + ".products";

        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения количества брендов", e);
        }

        return 0;
    }

    /**
     * Преобразует ResultSet в объект Product.
     * Вспомогательный метод для маппинга данных из базы в Java-объект.
     *
     * @param rs ResultSet с данными товара
     * @return объект Product с данными из ResultSet
     * @throws SQLException если произошла ошибка при чтении данных из ResultSet
     *
     *  Метод не перемещает курсор ResultSet, вызывающий код должен
     * убедиться что rs.next() был вызван и вернул true
     */
    private Product mapResultSetToProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setId(rs.getLong("id"));
        product.setName(rs.getString("name"));
        product.setDescription(rs.getString("description"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setCategory(rs.getString("category"));
        product.setBrand(rs.getString("brand"));
        product.setStockQuantity(rs.getInt("stock_quantity"));
        product.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        product.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return product;
    }
}
