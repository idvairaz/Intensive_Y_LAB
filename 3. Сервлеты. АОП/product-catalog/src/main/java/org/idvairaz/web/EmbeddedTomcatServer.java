package org.idvairaz.web;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

/**
 * Класс для запуска встроенного Tomcat сервера.
 * Обеспечивает развертывание веб-приложения без необходимости
 * внешнего веб-сервера. Регистрирует все сервлеты приложения
 * и настраивает их маппинг.
 *
 * @author idvavraz
 * @version 1.0
 */
public class EmbeddedTomcatServer {

    /**
     * Экземпляр встроенного Tomcat сервера.
     */
    private Tomcat tomcat;

    /**
     * Порт, на котором будет запущен сервер.
     */
    private final int port;

    /**
     * Создает новый экземпляр сервера с указанным портом.
     *
     * @param port порт для запуска сервера, должен быть в диапазоне 1-65535
     */
    public EmbeddedTomcatServer(int port) {
        this.port = port;
    }

    /**
     * Запускает встроенный Tomcat сервер.
     * Выполняет настройку сервера, регистрацию сервлетов и запуск.
     * После успешного запуска сервер начинает принимать HTTP запросы.
     *
     * @throws LifecycleException если произошла ошибка при запуске сервера
     */
    public void start() throws LifecycleException {
        tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        Context context = tomcat.addContext("", null);

        registerServlets(context);

        tomcat.start();
        System.out.println("Tomcat Embedded запущен на порту: " + port);
        System.out.println("REST API доступно по: http://localhost:" + port + "/api");

        new Thread(() -> {
            try {
                tomcat.getServer().await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * Регистрирует все сервлеты приложения в контексте Tomcat.
     * Настраивает маппинг URL паттернов на соответствующие сервлеты.
     *
     * @param context контекст Tomcat для регистрации сервлетов
     */
    private void registerServlets(org.apache.catalina.Context context) {
        try {
            Tomcat.addServlet(context, "ProductServlet", new ProductServlet());
            context.addServletMappingDecoded("/api/products/*", "ProductServlet");

            Tomcat.addServlet(context, "UserServlet", new UserServlet());
            context.addServletMappingDecoded("/api/users/*", "UserServlet");

            Tomcat.addServlet(context, "AuthServlet", new AuthServlet());
            context.addServletMappingDecoded("/api/auth/*", "AuthServlet");


            System.out.printf("""
                    Сервлеты зарегистрированы:
             - ProductServlet: /api/products/*
             - UserServlet: /api/users/*
             - AuthServlet: /api/auth/*
             """);


        } catch (Exception e) {
            System.err.println("Ошибка регистрации сервлетов: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Останавливает Tomcat сервер и освобождает все ресурсы.
     * Должен вызываться при завершении работы приложения.
     *
     *  @throws LifecycleException если произошла ошибка при остановке сервера
     */
    public void stop() throws LifecycleException {
        if (tomcat != null) {
            tomcat.stop();
            tomcat.destroy();
            System.out.println("Tomcat Embedded остановлен");
        }
    }

    /**
     * Останавливает Tomcat сервер и освобождает все ресурсы.
     * Должен вызываться при завершении работы приложения.
     *
     */
    public void startInBackground() {

        Thread serverThread = new Thread(() -> {
            try {
                start();
            } catch (LifecycleException e) {
                System.err.println("Ошибка запуска Tomcat: " + e.getMessage());
                e.printStackTrace();
            }
        });
        serverThread.setDaemon(true);
        serverThread.start();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

}