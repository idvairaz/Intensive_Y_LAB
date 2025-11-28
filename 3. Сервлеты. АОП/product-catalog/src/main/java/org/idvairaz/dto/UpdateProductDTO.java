package org.idvairaz.dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO класс для обновления существующего товара.
 * Используется для операций ОБНОВЛЕНИЯ данных.
 * Все поля опциональны для частичного обновления (PATCH).
 *
 * @author idvavraz
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductDTO {

    /** Название товара */
    private String name;

    /** Описание товара */
    private String description;

    /** Цена товара */
    @Positive(message = "Цена должна быть положительной")
    private BigDecimal price;

    /** Категория товара */
    private String category;

    /** Бренд товара */
    private String brand;

    /** Количество товара на складе */
    @Min(value = 0, message = "Количество не может быть отрицательным")
    private Integer stockQuantity;
}