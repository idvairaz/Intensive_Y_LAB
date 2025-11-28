package org.idvairaz.mapper;

import org.idvairaz.dto.UserDTO;
import org.idvairaz.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Маппер для преобразования между сущностью User и DTO.
 * Использует MapStruct для автоматической генерации кода преобразования.
 * Обеспечивает безопасное преобразование, исключая конфиденциальные данные.
 *
 * @author idvavraz
 * @version 1.0
 */
@Mapper(componentModel = "default")
public interface UserMapper {

    /**
     * Экземпляр маппера для использования в коде.
     * MapStruct автоматически генерирует реализацию этого интерфейса.
     */
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    /**
     * Преобразует сущность User в UserDTO.
     * Исключает пароль и другие конфиденциальные данные из DTO.
     *
     * @param user сущность пользователя для преобразования
     * @return DTO объект с данными пользователя (без пароля)
     */
    UserDTO toDTO(User user);
}
