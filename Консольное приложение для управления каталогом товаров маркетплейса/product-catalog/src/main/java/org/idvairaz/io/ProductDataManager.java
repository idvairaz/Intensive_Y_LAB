package org.idvairaz.io;

import org.idvairaz.model.Product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Менеджер для работы с данными товаров.
 * Обеспечивает сохранение и загрузку списка товаров в файл с использованием сериализации.
 *
 * @author idvavraz
 * @version 1.0
 */
public class ProductDataManager {

    /** Имя файла для хранения данных о товарах */
    private static final String DATA_FILE = "products.dat";

    /**
     * Сохраняет список товаров в файл.
     * Использует Java сериализацию для сохранения данных.
     * В случае ошибки ввода-вывода выводит сообщение в консоль.
     *
     * @param products список товаров для сохранения
     */
    public void saveProducts(List<Product> products) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(products);
            System.out.println("Товары сохранены в файл: " + DATA_FILE);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения товаров: " + e.getMessage());
        }
    }

    /**
     * Загружает список товаров из файла.
     * Если файл не существует, возвращает пустой список.
     * В случае ошибки загрузки выводит сообщение в консоль и возвращает пустой список.
     *
     * @return список загруженных товаров или пустой список если файл не существует или произошла ошибка
     * @SuppressWarnings("unchecked") подавление предупреждения о небезопасном приведении типа,
     *                               безопасно так как запись осуществляется этим же классом
     */
    @SuppressWarnings("unchecked")
    public List<Product> loadProducts() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            System.out.println("Файл с товарами не найден, будет создан новый каталог");
            return new ArrayList<>();
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            List<Product> products = (List<Product>) ois.readObject();
            System.out.println("Товары загружены из файла: " + products.size() + " товаров");
            return products;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка загрузки товаров: " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
