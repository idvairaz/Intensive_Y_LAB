package org.idvairaz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.idvairaz.model.User.UserRole;

/**
 * DTO класс для создания нового пользователя.
 * Содержит все необходимые данные для регистрации включая пароль.
 *
 * @author idvavraz
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserDTO {

    /** Имя пользователя (логин) */
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String username;

    /** Пароль пользователя */
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    /** Роль пользователя в системе */
    private UserRole role;
}