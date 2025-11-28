package org.idvairaz.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO класс для передачи данных о товаре.
 * Используется для операций ЧТЕНИЯ данных - возвращает полную информацию о товаре
 * включая системные поля (id, даты создания и обновления).
 *
 * @author idvavraz
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    /** Уникальный идентификатор товара */
    private Long id;

    /** Название товара */
    private String name;

    /** Описание товара */
    private String description;

    /** Цена товара */
    private BigDecimal price;

    /** Категория товара */
    private String category;

    /** Бренд товара */
    private String brand;

    /** Количество товара на складе */
    private int stockQuantity;

    /** Дата и время создания товара */
    private LocalDateTime createdAt;

    /** Дата и время последнего обновления товара */
    private LocalDateTime updatedAt;
}