package org.idvairaz.aspect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для пометки сервлетов или методов, которые должны быть залогированы в HTTP аудите.
 * Используется аспектом HttpAuditAspect для автоматического логирования HTTP запросов.
 *
 * @author idvavraz
 * @version 1.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpAuditable {

    /**
     * Описание действия для HTTP аудита.
     * Если не указано, будет использован HTTP метод и URI.
     *
     * @return описание действия
     */
    String value() default "";
}