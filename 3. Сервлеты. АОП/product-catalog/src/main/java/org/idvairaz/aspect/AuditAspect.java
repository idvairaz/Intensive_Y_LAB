package org.idvairaz.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.idvairaz.service.AuditService;
import org.idvairaz.service.AuthService;
import org.idvairaz.web.ServiceFactory;

import java.util.Arrays;

/**
 * Аспект для аудита действий пользователей.
 *
 * @author idvavraz
 * @version 1.0
 */
@Aspect
public class AuditAspect {

    /**
     * Сервис для записи действий в журнал аудита.
     */
    private AuditService auditService;

    /**
     * Сервис для получения информации о текущем пользователе.
     */
    private AuthService authService;


    /**
     * Конструктор инициализирует зависимости.
     */
    public AuditAspect() {
        this.auditService = ServiceFactory.getAuditService();
        this.authService = ServiceFactory.getAuthService();
    }

    /**
     * Перехватывает выполнение методов с аннотацией Auditable для аудита действий.
     * Логирует начало, успешное завершение и ошибки выполнения бизнес-методов.
     *
     * @param joinPoint точка соединения, представляющая перехваченный метод
     * @param auditable аннотация, содержащая описание действия для аудита
     * @return результат выполнения целевого метода
     * @throws Throwable если целевой метод выбрасывает исключение
     */
    @Around("@annotation(auditable)")
    public Object auditMethod(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String action = auditable.value().isEmpty() ? methodName : auditable.value();
        String username = authService.getCurrentUsername();


        String params = Arrays.toString(joinPoint.getArgs());
        if (params.length() > 200) {
            params = params.substring(0, 200) + "...";
        }

        if (auditService != null) {
            auditService.logAction(username, action,
                    "Начало метода: " + className + "." + methodName + ", Параметры: " + params);
        }

        System.out.printf("[АУДИТ ДЕЙСТВИЙ ПОЛЬЗОВАТЕЛЯ ЧЕРЕЗ АСПЕКТ] НАЧАЛО: %s.%s - %s - Параметры: %s%n",
                className, methodName, action, params);

        try {
            Object result = joinPoint.proceed();

            if (auditService != null) {
                auditService.logAction(username, action + "_SUCCESS",
                        "Метод выполнен успешно: " + className + "." + methodName);
            }

            System.out.printf("[АУДИТ ДЕЙСТВИЙ ПОЛЬЗОВАТЕЛЯ ЧЕРЕЗ АСПЕКТ] УСПЕХ: %s.%s - %s%n",
                    className, methodName, action);
            return result;
        } catch (Exception e) {

            if (auditService != null) {
                auditService.logAction(username, action + "_ERROR",
                        "Ошибка: " + e.getMessage() + " в методе: " + className + "." + methodName);
            }

            System.out.printf("[АУДИТ ДЕЙСТВИЙ ПОЛЬЗОВАТЕЛЯ ЧЕРЕЗ АСПЕКТ] ОШИБКА: %s.%s - %s - %s%n",
                    className, methodName, action, e.getMessage());
            throw e;
        }
    }
}