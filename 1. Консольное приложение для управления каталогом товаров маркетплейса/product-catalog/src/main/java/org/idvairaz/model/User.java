package org.idvairaz.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

/**
 * Класс представляющий пользователя системы.
 * Содержит учетные данные и информацию о роли пользователя.
 *
 * @author idvavraz
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * Роли пользователей системы
     */
    public enum UserRole {
        /** Администратор - полный доступ ко всем функциям */
        ADMIN,
        /** Менеджер - управление товарами и просмотр аудита */
        MANAGER,
        /** Обычный пользователь - только просмотр товаров */
        USER
    }

    @EqualsAndHashCode.Include
    private String username;
    private String password;
    private UserRole role;

    @Builder.Default
    private boolean isLoggedIn = false;

    /**
     * Конструктор для создания пользователя с автоматической установкой isLoggedIn = false
     */
    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.isLoggedIn = false;
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
}
