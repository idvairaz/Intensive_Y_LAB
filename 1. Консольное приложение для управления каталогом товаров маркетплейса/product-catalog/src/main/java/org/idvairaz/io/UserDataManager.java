package org.idvairaz.io;

import org.idvairaz.model.User;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * Менеджер для работы с данными пользователей.
 * Обеспечивает сохранение и загрузку списка пользователей в файл с использованием сериализации.
 *
 * @author idvavraz
 * @version 1.0
 */
public class UserDataManager {

    /** Имя файла для хранения данных о пользователях */
    private static final String USERS_FILE = "users.dat";

    /**
     * Сохраняет список пользователей в файл.
     * Использует Java сериализацию для сохранения данных.
     * В случае ошибки ввода-вывода выводит сообщение в консоль.
     *
     * @param users список пользователей для сохранения
     */
    public void saveUsers(List<User> users) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(USERS_FILE))) {
            oos.writeObject(users);
            System.out.println("Пользователи сохранены в файл: " + USERS_FILE);
        } catch (IOException e) {
            System.out.println("Ошибка сохранения пользователей: " + e.getMessage());
        }
    }

    /**
     * Загружает список пользователей из файла.
     * Если файл не существует, возвращает null для создания пользователей по умолчанию.
     * В случае ошибки загрузки выводит сообщение в консоль и возвращает null.
     *
     * @return список загруженных пользователей, null если файл не существует или произошла ошибка
     */
    @SuppressWarnings("unchecked")
    public List<User> loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            System.out.println("Файл с пользователями не найден, будут созданы пользователи по умолчанию");
            return null;
        }
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(USERS_FILE))) {
            List<User> users = (List<User>) ois.readObject();
            System.out.println("Пользователи загружены из файла: " + users.size() + " пользователей");
            return users;
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Ошибка загрузки пользователей: " + e.getMessage());
            return null;
        }
    }
}
