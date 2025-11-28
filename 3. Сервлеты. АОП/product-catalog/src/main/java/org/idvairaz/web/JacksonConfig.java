package org.idvairaz.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

/**
 * Конфигурационный класс для настройки Jackson ObjectMapper.
 * Обеспечивает правильную сериализацию и десериализацию JSON,
 *
 * @author idvavraz
 * @version 1.0
 */
public class JacksonConfig {

    /**
     * Единственный экземпляр ObjectMapper с предварительной настройкой.
     */
    private static ObjectMapper objectMapper;

    /**
     * Возвращает настроенный экземпляр ObjectMapper.
     * ObjectMapper настраивается для поддержки:
     * - Java 8 Date/Time API (LocalDateTime, etc.)
     * - Читаемого формата дат (вместо timestamp)
     *
     * @return настроенный экземпляр ObjectMapper готовый к использованию
     */
    public static ObjectMapper getObjectMapper() {
        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());
            objectMapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        }
        return objectMapper;
    }
}