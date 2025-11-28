package org.idvairaz.mapper;

import org.idvairaz.dto.ProductDTO;
import org.idvairaz.dto.CreateProductDTO;
import org.idvairaz.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Маппер для преобразования между сущностью Product и DTO.
 * Использует MapStruct для автоматической генерации кода преобразования.
 * Поддерживает преобразование в обе стороны.
 *
 * @author idvavraz
 * @version 1.0
 */
@Mapper(componentModel = "default")
public interface ProductMapper {

    /**
     * Экземпляр маппера для использования в коде.
     * MapStruct автоматически генерирует реализацию этого интерфейса.
     */
    ProductMapper INSTANCE = Mappers.getMapper(ProductMapper.class);

    /**
     * Преобразует сущность Product в ProductDTO.
     * Копирует все поля включая системные (id, даты создания и обновления).
     *
     * @param product сущность товара для преобразования
     * @return DTO объект с данными товара
     */
    ProductDTO toDTO(Product product);

    /**
     * Преобразует CreateProductDTO в сущность Product.
     * Используется при создании новых товаров, игнорирует все системные поля.
     *
     * @param createProductDTO DTO для создания товара
     * @return сущность товара без системных полей
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(java.time.LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(java.time.LocalDateTime.now())")
    Product toEntity(CreateProductDTO createProductDTO);
}