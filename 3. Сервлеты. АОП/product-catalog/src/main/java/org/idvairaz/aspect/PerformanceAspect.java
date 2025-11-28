//package org.idvairaz.aspect;
//
//import lombok.RequiredArgsConstructor;
//import org.aspectj.lang.ProceedingJoinPoint;
//import org.aspectj.lang.annotation.Around;
//import org.aspectj.lang.annotation.Aspect;
//import org.aspectj.lang.reflect.MethodSignature;
//import org.idvairaz.repository.MethodMetricsRepository;
//import org.idvairaz.service.AuthService;
//
///**
// * Аспект для замера времени выполнения методов и сохранения метрик в БД.
// */
//@Aspect
//@RequiredArgsConstructor
//public class PerformanceAspect {
//
//    private final MethodMetricsRepository metricsRepository;
//    private final AuthService authService;
//
//    @Around("execution(* org.idvairaz.service.*.*(..))")
//    public Object measurePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
//        long startTime = System.currentTimeMillis();
//        String methodName = joinPoint.getSignature().getName();
//        String className = joinPoint.getTarget().getClass().getSimpleName();
//
//        String username = authService.getCurrentUser()
//                .map(user -> user.getUsername())
//                .orElse("SYSTEM");
//
//        boolean successful = true;
//        String errorMessage = null;
//
//        try {
//            Object result = joinPoint.proceed();
//            return result;
//        } catch (Exception e) {
//            successful = false;
//            errorMessage = e.getMessage();
//            throw e;
//        } finally {
//            long executionTime = System.currentTimeMillis() - startTime;
//
//            metricsRepository.saveMetric(
//                    methodName,
//                    className,
//                    executionTime,
//                    successful,
//                    errorMessage,
//                    username
//            );
//
//            System.out.printf("[METRIC] %s.%s - %d ms - %s%n",
//                    className, methodName, executionTime,
//                    successful ? "SUCCESS" : "FAILED");
//        }
//    }
//}

package org.idvairaz.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

/**
 * Аспект для замера времени выполнения методов.
 *
 * @author idvavraz
 *  * @version 1.0
 */
@Aspect
public class PerformanceAspect {

    /**
     * Перехватывает выполнение методов сервисного слоя для замера времени выполнения.
     *
     * @param joinPoint точка соединения, представляющая перехваченный метод
     * @return результат выполнения целевого метода
     * @throws Throwable если целевой метод выбрасывает исключение
     *
     */
    @Around("execution(* org.idvairaz.service.*.*(..)) && " +
            "!execution(* org.idvairaz.service.*.isAuthenticated(..)) && " +
            "!execution(* org.idvairaz.service.*.getCurrent*(..)) && " +
            "!execution(* org.idvairaz.service.*.can*(..))")
    public Object measurePerformance(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = "Unknown";

        try {
            Object target = joinPoint.getTarget();
            if (target != null) {
                className = target.getClass().getSimpleName();
            } else {
                className = joinPoint.getSignature().getDeclaringType().getSimpleName();
            }
        } catch (Exception e) {
            className = joinPoint.getSignature().getDeclaringType().getSimpleName();
        }
        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.printf("[ВЫПОЛНЕНИЕ ЛЮБОГО МЕТОДА ЧЕРЕЗ АСПЕКТ] %s.%s - %d ms - SUCCESS%n",
                    className, methodName, executionTime);
            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.printf("[ВЫПОЛНЕНИЕ ЛЮБОГО МЕТОДА ЧЕРЕЗ АСПЕКТ] %s.%s - %d ms - FAILED: %s%n",
                    className, methodName, executionTime, e.getMessage());
            throw e;
        }
    }
}