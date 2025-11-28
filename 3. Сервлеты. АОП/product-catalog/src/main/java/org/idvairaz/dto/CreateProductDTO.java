package org.idvairaz.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO класс для создания нового товара.
 * Используется для операций СОЗДАНИЯ данных - содержит только поля,
 * которые пользователь может указать при создании товара.
 * Системные поля (id, даты) исключены, так как генерируются автоматически.
 *
 * @author idvavraz
 * @version 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateProductDTO {

    /** Название товара (обязательное поле) */
    @NotBlank(message = "Название товара не может быть пустым")
    private String name;

    /** Описание товара */
    private String description;

    /** Цена товара (должна быть положительной) */
    @NotNull(message = "Цена обязательна")
    @Positive(message = "Цена должна быть положительной")
    private BigDecimal price;

    /** Категория товара (обязательное поле) */
    @NotBlank(message = "Категория не может быть пустой")
    private String category;

    /** Бренд товара (обязательное поле) */
    @NotBlank(message = "Бренд не может быть пустым")
    private String brand;

    /** Количество товара на складе (не может быть отрицательным) */
    @Min(value = 0, message = "Количество не может быть отрицательным")
    private int stockQuantity;
}