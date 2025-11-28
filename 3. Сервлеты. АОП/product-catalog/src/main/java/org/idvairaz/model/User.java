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
 * @version 2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
public class User implements Serializable {


    /** Уникальный идентификатор версии для сериализации */
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

    /** Уникальный идентификатор пользователя */
    @EqualsAndHashCode.Include
    private Long id;

    /** Логин пользователя */
    private String username;

    /** Пароль  пользователя */
    private String password;

    /** Роль пользователя в системе */
    private UserRole role;

    /** Флаг indicating, выполнен ли вход пользователя в систему */
    @Builder.Default
    private boolean loggedIn = false;

    /**
     * Проверяет, является ли пользователь администратором.
     *
     * @return true если пользователь имеет роль ADMIN
     */
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(role);
    }

    /**
     * Проверяет, является ли пользователь менеджером.
     *
     * @return true если пользователь имеет роль MANAGER
     */
    public boolean isManager() {
        return UserRole.MANAGER.equals(role);
    }

    /**
     * Проверяет, является ли пользователь обычным пользователем.
     *
     * @return true если пользователь имеет роль USER
     */
    public boolean isUser() {
        return UserRole.USER.equals(role);
    }

    /**
     * Проверяет, может ли пользователь управлять товарами.
     *
     * @return true если пользователь имеет права ADMIN или MANAGER
     */
    public boolean canManageProducts() {
        return isAdmin() || isManager();
    }

    /**
     * Проверяет, может ли пользователь просматривать журнал аудита.
     *
     * @return true если пользователь имеет права ADMIN или MANAGER
     */
    public boolean canViewAudit() {
        return isAdmin() || isManager();
    }

    /**
     * Проверяет, выполнен ли вход пользователя в систему.
     *
     * @return true если пользователь авторизован
     */
    public boolean isLoggedIn() {
        return loggedIn;
    }

    /**
     * Устанавливает статус авторизации пользователя.
     *
     * @param loggedIn true если пользователь авторизован
     */
    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}
