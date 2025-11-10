package org.idvairaz.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Класс представляющий пользователя системы.
 * Содержит учетные данные и информацию о роли пользователя.
 *
 * @author idvavraz
 * @version 1.0
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;
    private String username;
    private String password;
    private UserRole role;

    public enum UserRole {
        /** Администратор - полный доступ ко всем функциям */
        ADMIN,
        /** Менеджер - управление товарами и просмотр аудита */
        MANAGER,
        /** Обычный пользователь - только просмотр товаров */
        USER
    }

    private boolean isLoggedIn;

    public User() {

    }

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isLoggedIn = false;
    }

    public String getUsername() {
        return username;
    }


    public String getPassword() {
        return password;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setLoggedIn(boolean loggedIn) {
        isLoggedIn = loggedIn;
    }

    public boolean isAdmin() {
        return UserRole.ADMIN.equals(role);
    }

    public boolean isManager() {
        return UserRole.MANAGER.equals(role);
    }

    public boolean isUser() {
        return UserRole.USER.equals(role);
    }

    public boolean canManageProducts() {
        return isAdmin() || isManager();
    }

    public boolean canViewAudit() {
        return isAdmin() || isManager();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return username.equals(user.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", role=" + role +
                ", isLoggedIn=" + isLoggedIn +
                '}';
    }
}
