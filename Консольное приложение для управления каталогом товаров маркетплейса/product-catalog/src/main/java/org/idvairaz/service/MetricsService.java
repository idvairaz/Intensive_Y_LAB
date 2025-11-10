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
        operationCounters.put(" ПОЛУЧЕНИЕ_ВСЕХ_ТОВАРОВ", 0);
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

    /**
     * Увеличивает счетчик выполнения операции.
     * Используется когда не требуется измерение времени выполнения.
     *
     * @param operation название операции
     */
    public void incrementCounter(String operation) {
        operationCounters.put(operation, operationCounters.getOrDefault(operation, 0) + 1);
    }

    /**
     * Выводит полную статистику метрик в консоль.
     * Включает время работы приложения, статистику по операциям и экстремумы производительности.
     */
    public void showMetrics() {
        System.out.println("\n=== МЕТРИКИ ПРИЛОЖЕНИЯ ===");

        Duration uptime = Duration.between(appStartTime, LocalDateTime.now());
        System.out.println("Время работы: " + formatDuration(uptime));

        if (operationDurations.isEmpty()) {
            System.out.println("Данные о операциях отсутствуют");
            return;
        }

        System.out.println("Статистика операций:");
        operationDurations.forEach((operation, totalDuration) -> {
            int count = operationCounters.getOrDefault(operation, 0);
            if (count > 0) {
                double avgMillis = totalDuration.toMillis() / (double) count;
                System.out.printf("   - %s: %d раз, среднее время: %.2f мс%n",
                        operation, count, avgMillis);
            }
        });

        /** Самые быстрые и медленные операции */
        String fastestOp = operationDurations.entrySet().stream()
                .min(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        String slowestOp = operationDurations.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("N/A");

        System.out.println("Экстремумы:");
        System.out.println("   - Самая быстрая операция: " + fastestOp);
        System.out.println("   - Самая медленная операция: " + slowestOp);

        /** Общая статистика */
        int totalOperations = operationCounters.values().stream()
                .mapToInt(Integer::intValue)
                .sum();
        System.out.println("Всего операций: " + totalOperations);
    }

    /**
     * Форматирует продолжительность времени в читаемый формат ЧЧ:ММ:СС.
     *
     * @param duration продолжительность для форматирования
     * @return строка в формате "ЧЧ:ММ:СС"
     */
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
