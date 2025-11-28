package org.idvairaz.web;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Утилитный класс для валидации объектов с использованием Bean Validation.
 * Инкапсулирует работу с Validator и предоставляет простой метод validate().
 * Используется сервлетами для проверки входящих DTO перед обработкой.
 *
 * @author idvavraz
 * @version 1.0
 */
public class ValidationUtil {

    /**
     * Валидатор для проверки объектов по аннотациям Bean Validation.
     * Инициализируется один раз при загрузке класса и используется повторно.
     */
    private static final Validator validator;

    /**
     * Статический блок инициализации валидатора.
     * Создает фабрику валидаторов и извлекает из нее экземпляр Validator.
     */
    static {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    /**
     * Выполняет валидацию объекта и возвращает строку с ошибками.
     * Если объект валиден, возвращает null. Если есть нарушения валидации,
     * возвращает строку с перечислением всех ошибок.
     *
     * @param <T> тип валидируемого объекта
     * @param object объект для валидации, не должен быть null
     * @return строка с ошибками валидации или null если объект валиден
     * @throws IllegalArgumentException если переданный объект равен null
     *
     */
    public static <T> String validate(T object) {
        Set<ConstraintViolation<T>> violations = validator.validate(object);

        if (violations.isEmpty()) {
            return null;
        }

        return violations.stream()
                .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                .collect(Collectors.joining(", "));
    }
}