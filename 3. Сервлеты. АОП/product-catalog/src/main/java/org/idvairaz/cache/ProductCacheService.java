//package org.idvairaz.cache;
//
//import org.idvairaz.model.Product;
//import java.util.List;
//import java.util.Optional;
//import java.util.Map;
//
///**
// * Специализированный интерфейс для кэширования товаров.
// * Предоставляет методы для работы с различными типами кэшей товаров.
// *
// * @author idvavraz
// * @version 1.0
// */
//public interface ProductCacheService {
//
//    /**
//     * Кэширует товар по его идентификатору.
//     *
//     * @param product товар для кэширования
//     */
//    void cacheProduct(Product product);
//
//    /**
//     * Получает товар из кэша по идентификатору.
//     *
//     * @param id идентификатор товара
//     * @return Optional с товаром или empty если не найден в кэше
//     */
//    Optional<Product> getCachedProduct(Long id);
//
//    /**
//     * Кэширует список товаров по категории.
//     *
//     * @param category категория товаров
//     * @param products список товаров для кэширования
//     */
//    void cacheProductsByCategory(String category, List<Product> products);
//
//    /**
//     * Получает список товаров из кэша по категории.
//     *
//     * @param category категория для поиска
//     * @return Optional со списком товаров или empty если не найден в кэше
//     */
//    Optional<List<Product>> getCachedProductsByCategory(String category);
//
//    /**
//     * Кэширует список товаров по бренду.
//     *
//     * @param brand бренд товаров
//     * @param products список товаров для кэширования
//     */
//    void cacheProductsByBrand(String brand, List<Product> products);
//
//    /**
//     * Получает список товаров из кэша по бренду.
//     *
//     * @param brand бренд для поиска
//     * @return Optional со списком товаров или empty если не найден в кэше
//     */
//    Optional<List<Product>> getCachedProductsByBrand(String brand);
//
//    /**
//     * Очищает все кэши товаров.
//     */
//    void clear();
//
//    /**
//     * Возвращает статистику по всем кэшам товаров.
//     *
//     * @return карта со статистикой кэширования
//     */
//    Map<String, Object> getStats();
//
//    /**
//     * Удаляет товар из всех кэшей по идентификатору.
//     *
//     * @param id идентификатор товара для удаления
//     * При изменении товара нужно очистить кэши категорий и брендов,
//     * так как списки товаров в них могли измениться
//     */
//    void invalidateProduct(Long id);
//}

package org.idvairaz.cache;

import org.idvairaz.model.Product;

import java.util.List;
import java.util.Optional;
import java.util.Map;

/**
 * Специализированный интерфейс для кэширования товаров с расширенными возможностями.
 * Предоставляет методы для работы с различными типами кэшей товаров и поисковыми результатами.
 *
 * @author idvavraz
 * @version 2.0
 */
public interface ProductCacheService {

    /**
     * Кэширует товар по его идентификатору.
     *
     * @param product товар для кэширования
     */
    void cacheProduct(Product product);

    /**
     * Получает товар из кэша по идентификатору.
     *
     * @param id идентификатор товара
     * @return Optional с товаром или empty если не найден в кэше
     */
    Optional<Product> getCachedProduct(Long id);

    /**
     * Кэширует список товаров по категории.
     *
     * @param category категория товаров
     * @param products список товаров для кэширования
     */
    void cacheProductsByCategory(String category, List<Product> products);

    /**
     * Получает список товаров из кэша по категории.
     *
     * @param category категория для поиска
     * @return Optional со списком товаров или empty если не найден в кэше
     */
    Optional<List<Product>> getCachedProductsByCategory(String category);

    /**
     * Кэширует список товаров по бренду.
     *
     * @param brand бренд товаров
     * @param products список товаров для кэширования
     */
    void cacheProductsByBrand(String brand, List<Product> products);

    /**
     * Получает список товаров из кэша по бренду.
     *
     * @param brand бренд для поиска
     * @return Optional со списком товаров или empty если не найден в кэше
     */
    Optional<List<Product>> getCachedProductsByBrand(String brand);

    /**
     * Кэширует результаты поискового запроса.
     *
     * @param searchKey поисковый ключ или запрос
     * @param products список найденных товаров
     */
    void cacheSearchResults(String searchKey, List<Product> products);

    /**
     * Получает закэшированные результаты поиска.
     *
     * @param searchKey поисковый ключ или запрос
     * @return Optional со списком товаров или empty если не найден в кэше
     */
    Optional<List<Product>> getCachedSearchResults(String searchKey);

    /**
     * Очищает все кэши товаров.
     */
    void clear();

    /**
     * Возвращает статистику по всем кэшам товаров.
     *
     * @return карта со статистикой кэширования
     */
    Map<String, Object> getStats();

    /**
     * Удаляет товар из всех кэшей по идентификатору.
     * Использует умную инвалидацию для минимизации потерь кэша.
     *
     * @param id идентификатор товара для удаления
     */
    void invalidateProduct(Long id);
}