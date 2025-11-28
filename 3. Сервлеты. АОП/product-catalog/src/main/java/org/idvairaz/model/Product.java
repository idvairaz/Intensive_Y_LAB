package org.idvairaz.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Класс представляющий товар в каталоге маркетплейса.
 * Содержит информацию о товаре: название, описание, цену, категорию, бренд и количество.
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
public class Product implements Serializable {

    /** Уникальный идентификатор версии для сериализации */
    private static final long serialVersionUID = 1L;

    /** Уникальный идентификатор товара */
    @EqualsAndHashCode.Include
    private Long id;

    /** Название товара */
    private String name;

    /** Oписание товара */
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

    /**
     * Конструктор для создания НОВЫХ товаров (даты ставятся автоматически)
     *
     * @param name название товара
     * @param description описание товара
     * @param price цена товара
     * @param category категория товара
     * @param brand бренд товара
     * @param stockQuantity количество на складе
     */
    public Product(String name, String description, BigDecimal price,
                   String category, String brand, int stockQuantity) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.brand = brand;
        this.stockQuantity = stockQuantity;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}
