package org.idvairaz.repository;

import org.idvairaz.model.Product;

import java.util.List;
import java.util.Optional;

/**
 * Интерфейс репозитория для работы с товарами.
 * Определяет контракт для операций хранения и поиска товаров.
 *
 * @author idvavraz
 * @version 2.0
 */
public interface ProductRepository {

    /**
     * Сохраняет товар в репозитории.
     * Если товар новый (id = null), присваивает ему идентификатор.
     *
     * @param product товар для сохранения
     * @return сохраненный товар с присвоенным ID
     */
    Product save(Product product);

    /**
     * Находит товар по идентификатору.
     *
     * @param id идентификатор товара
     * @return Optional с найденным товаром или empty если не найден
     */
    Optional<Product> findById(Long id);

    /**
     * Находит товар по точному совпадению названия.
     * Поиск выполняется без учета регистра.
     *
     * @param name название товара для поиска
     * @return Optional с найденным товаром или empty если не найден
     */
    Optional<Product> findByName(String name);

    /**
     * Возвращает все товары из репозитория.
     *
     * @return список всех товаров
     */
    List<Product> findAll();

    /**
     * Удаляет товар по идентификатору.
     *
     * @param id идентификатор товара для удаления
     */
    void delete(Long id);

    /**
     * Находит все товары указанной категории.
     *
     * @param category категория для поиска
     * @return список товаров указанной категории
     */
    List<Product> findByCategory(String category);

    /**
     * Находит все товары указанного бренда.
     *
     * @param brand бренд для поиска
     * @return список товаров указанного бренда
     */
    List<Product> findByBrand(String brand);

    /**
     * Возвращает общее количество товаров в каталоге.
     *
     * @return количество товаров
     */
    int getTotalProductsCount();

    /**
     * Возвращает количество уникальных категорий в каталоге.
     *
     * @return количество категорий
     */
    int getTotalCategoriesCount();

    /**
     * Возвращает количество уникальных брендов в каталоге.
     *
     * @return количество брендов
     */
    int getTotalBrandsCount();
}
