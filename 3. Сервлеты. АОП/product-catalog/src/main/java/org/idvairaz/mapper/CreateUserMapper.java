package org.idvairaz.mapper;

import org.idvairaz.dto.CreateUserDTO;
import org.idvairaz.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Специализированный маппер для создания новых пользователей.
 * Отделен от основного UserMapper для лучшего разделения ответственности.
 *
 * @author idvavraz
 * @version 1.0
 */
@Mapper(componentModel = "default")
public interface CreateUserMapper {

    /**
     * Экземпляр маппера для использования в коде.
     */
    CreateUserMapper INSTANCE = Mappers.getMapper(CreateUserMapper.class);

    /**
     * Преобразует CreateUserDTO в сущность User для регистрации.
     * Включает пароль и устанавливает значения по умолчанию для новых пользователей.
     *
     * @param createUserDTO DTO для создания пользователя
     * @return сущность пользователя готовый для сохранения
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "loggedIn", ignore = true)
    User toEntity(CreateUserDTO createUserDTO);
}

