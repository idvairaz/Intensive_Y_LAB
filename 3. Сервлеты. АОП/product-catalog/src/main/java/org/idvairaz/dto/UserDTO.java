package org.idvairaz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.idvairaz.model.User.UserRole;

/**
 * DTO класс для передачи данных о пользователе.
 * Используется для операций с пользователями, исключая конфиденциальные данные
 * такие как пароль.
 *
 * @author idvavraz
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    /** Уникальный идентификатор пользователя */
    private Long id;

    /** Имя пользователя (логин) */
    private String username;

    /** Роль пользователя в системе */
    private UserRole role;

    /** Флаг indicating, выполнен ли вход пользователя в систему */
    private boolean loggedIn;

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