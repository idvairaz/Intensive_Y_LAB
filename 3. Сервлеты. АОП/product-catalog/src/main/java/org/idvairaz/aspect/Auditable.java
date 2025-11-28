package org.idvairaz.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для пометки методов, которые должны быть залогированы в аудите.
 * Используется аспектом AuditAspect для автоматического логирования действий.
 *
 * @author idvavraz
 * @version 1.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {

    /**
     * Описание действия для аудита.
     * Если не указано, будет использовано имя метода.
     *
     * @return описание действия
     */
    String value() default "";
}
