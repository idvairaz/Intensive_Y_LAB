package org.idvairaz.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.idvairaz.service.AuditService;
import org.idvairaz.service.AuthService;
import org.idvairaz.web.ServiceFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;

/**
 * Аспект для аудита HTTP запросов.
 *
 * @author idvavraz
 * @version 1.0
 */
@Aspect
public class HttpAuditAspect {

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
    public HttpAuditAspect() {
        this.auditService = ServiceFactory.getAuditService();
        this.authService = ServiceFactory.getAuthService();
    }

    /**
     * Pointcut для всех методов сервлетов, которые принимают HttpServletRequest и HttpServletResponse.
     *
     * @param request HTTP запрос
     * @param response HTTP ответ
     */
    @Pointcut("execution(* org.idvairaz.web.*Servlet.*(..)) && args(request, response)")
    public void servletMethods(HttpServletRequest request, HttpServletResponse response) {

    }

    /**
     * Перехватывает выполнение методов сервлетов для аудита HTTP запросов.
     * Логирует начало и окончание обработки HTTP запросов, включая время выполнения и статус.
     *
     * @param joinPoint точка соединения, представляющая перехваченный метод
     * @param request HTTP запрос
     * @param response HTTP ответ
     * @return результат выполнения целевого метода
     * @throws Throwable если целевой метод выбрасывает исключение
     */
    @Around("servletMethods(request, response)")
    public Object auditHttpRequest(ProceedingJoinPoint joinPoint, HttpServletRequest request, HttpServletResponse response) throws Throwable {

        String httpMethod = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String fullUrl = queryString != null ? uri + "?" + queryString : uri;
        String username = authService.getCurrentUsername();

        String params = getRequestParameters(request);
        if (params.length() > 200) {
            params = params.substring(0, 200) + "...";
        }

        if (auditService != null) {
            auditService.logAction(username, "HTTP_" + httpMethod,
                    "Начало: " + fullUrl + ", Параметры: " + params);
        }

        System.out.printf("[HTTP АУДИТ] НАЧАЛО: %s %s%n", httpMethod, fullUrl);

        long startTime = System.currentTimeMillis();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            int statusCode = response.getStatus();
            String statusText = getStatusText(statusCode);

            if (auditService != null) {
                auditService.logAction(username, "HTTP_" + httpMethod + "_SUCCESS",
                        "Успех: " + fullUrl + ", Статус: " + statusCode + ", Время: " + executionTime + "мс");
            }

            System.out.printf("[HTTP АУДИТ] УСПЕХ: %s %s - Статус: %d (%s) - Время: %d мс%n",
                    httpMethod, fullUrl, statusCode, statusText, executionTime);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            if (auditService != null) {
                auditService.logAction(username, "HTTP_" + httpMethod + "_ERROR",
                        "Ошибка: " + fullUrl + ", Сообщение: " + e.getMessage() + ", Время: " + executionTime + "мс");
            }

            System.out.printf("[HTTP АУДИТ] ОШИБКА: %s %s - %s - Время: %d мс%n",
                    httpMethod, fullUrl, e.getMessage(), executionTime);
            throw e;
        }
    }

    /**
     * Возвращает текстовое описание HTTP статуса.
     *
     * @param statusCode HTTP статус код
     * @return текстовое описание статуса
     */
    private String getStatusText(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) return "УСПЕХ";
        if (statusCode >= 400 && statusCode < 500) return "ОШИБКА КЛИЕНТА";
        return "ОШИБКА СЕРВЕРА";
    }

    /**
     * Извлекает параметры запроса в виде строки.
     *
     * @param request HTTP запрос
     * @return строка с параметрами запроса
     */
    private String getRequestParameters(HttpServletRequest request) {
        StringBuilder params = new StringBuilder();
        request.getParameterMap().forEach((key, values) -> {
            params.append(key).append("=").append(Arrays.toString(values)).append("; ");
        });

        if (params.length() == 0) {
            params.append("JSON_BODY");
        }

        return params.toString();
    }
}