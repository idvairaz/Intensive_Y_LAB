package org.idvairaz.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO класс для аутентификации пользователя.
 * Содержит учетные данные для входа в систему.
 *
 * @author idvavraz
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    /** Имя пользователя для входа в систему */
    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String username;

    /** Пароль пользователя для входа в систему */
    @NotBlank(message = "Пароль не может быть пустым")
    private String password;
}