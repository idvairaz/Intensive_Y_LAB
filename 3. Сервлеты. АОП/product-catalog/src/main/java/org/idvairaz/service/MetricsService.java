package org.idvairaz.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для сбора и анализа метрик производительности приложения.
 * Собирает статистику по операциям: количество вызовов и время выполнения.
 *
 * @author idvavraz
 * @version 1.0
 */
public class MetricsService {
    /** Счетчики количества выполненных операций */
    private final Map<String, Integer> operationCounters = new HashMap<>();

    /** Общее время выполнения для каждой операции */
    private final Map<String, Duration> operationDurations = new HashMap<>();

    /** Время запуска приложения для расчета общего времени работы */
    private LocalDateTime appStartTime;


    /**
     * Конструктор сервиса метрик.
     * Инициализирует счетчики для основных операций и запоминает время запуска.
     */
    public MetricsService() {
        this.appStartTime = LocalDateTime.now();
        operationCounters.put("ПОИСК_ПО_ID", 0);
        operationCounters.put("ПОИСК_ПО_ИМЕНИ", 0);
        operationCounters.put("ПОИСК_ПО_КАТЕГОРИИ", 0);
        operationCounters.put("ПОИСК_ПО_БРЕНДУ", 0);
        operationCounters.put("ДОБАВИТЬ_ТОВАР", 0);
        operationCounters.put("ОБНОВИТЬ_ТОВАР", 0);
        operationCounters.put("УДАЛИТЬ_ТОВАР", 0);
        operationCounters.put("ПОЛУЧЕНИЕ_ВСЕХ_ТОВАРОВ", 0);
    }



    /**
     * Записывает выполнение операции с измерением времени.
     * Увеличивает счетчик операций и добавляет время выполнения к общей статистике.
     *
     * @param operation название операции
     * @param duration время выполнения операции
     */
    public void recordOperation(String operation, Duration duration) {
        operationCounters.put(operation, operationCounters.getOrDefault(operation, 0) + 1);
        operationDurations.merge(operation, duration, Duration::plus);
    }
}
