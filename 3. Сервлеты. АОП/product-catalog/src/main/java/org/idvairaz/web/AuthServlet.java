package org.idvairaz.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.idvairaz.dto.LoginDTO;
import org.idvairaz.dto.UserDTO;
import org.idvairaz.mapper.UserMapper;
import org.idvairaz.model.User;
import org.idvairaz.service.AuthService;

import java.io.IOException;
import java.util.Optional;

/**
 * Сервлет для аутентификации и управления сессиями пользователей.
 * Предоставляет REST API для регистрации, входа, выхода и получения
 * информации о текущем пользователе.
 *
 * @author idvavraz
 * @version 1.0
 */
@WebServlet("/api/auth/*")
public class AuthServlet extends HttpServlet {

    /**
     * Сервис для аутентификации и авторизации пользователей.
     */
    private AuthService authService;
    /**
     * Маппер для преобразования User в UserDTO.
     */
    private UserMapper userMapper;

    /**
     * Объект для работы с JSON.
     */
    private ObjectMapper objectMapper;

    /**
     * Инициализирует сервлет, создавая необходимые зависимости.
     * Вызывается контейнером сервлетов при развертывании приложения.
     */
    @Override
    public void init() {
        this.authService = ServiceFactory.getAuthService();
        this.userMapper = UserMapper.INSTANCE;
        this.objectMapper = JacksonConfig.getObjectMapper();
    }

    /**
     * Обрабатывает POST запросы для операций аутентификации.
     * Поддерживает следующие endpoints:
     * - /api/auth/login - вход пользователя
     * - /api/auth/logout - выход пользователя
     * - /api/auth/register - регистрация нового пользователя
     *
     * @param req HTTP запрос содержащий данные аутентификации
     * @param resp HTTP ответ с результатом операции
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        if (pathInfo == null || pathInfo.equals("/login")) {
            login(req, resp);
        } else if (pathInfo.equals("/logout")) {
            logout(req, resp);
        } else if (pathInfo.equals("/register")) {
            register(req, resp);
        } else {
            sendErrorResponse(resp, "Неизвестный endpoint", HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /**
     * Обрабатывает GET запросы для получения информации о текущем пользователе.
     * Endpoint: GET /api/auth
     *
     * @param req HTTP запрос
     * @param resp HTTP ответ с информацией о пользователе или ошибкой аутентификации
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Optional<User> currentUser = authService.getCurrentUser();
        if (currentUser.isPresent()) {
            UserDTO userDTO = userMapper.toDTO(currentUser.get());
            String jsonResponse = objectMapper.writeValueAsString(userDTO);
            resp.getWriter().write(jsonResponse);
        } else {
            sendErrorResponse(resp, "Пользователь не аутентифицирован", HttpServletResponse.SC_UNAUTHORIZED);
        }
    }

    /**
     * Выполняет вход пользователя в систему.
     * Проверяет учетные данные и создает сессию при успешной аутентификации.
     *
     * @param req HTTP запрос с телом содержащим LoginDTO
     * @param resp HTTP ответ с информацией о пользователе или ошибкой
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void login(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            LoginDTO loginDto = objectMapper.readValue(req.getReader(), LoginDTO.class);

            String validationError = ValidationUtil.validate(loginDto);
            if (validationError != null) {
                sendErrorResponse(resp, "Ошибка валидации: " + validationError,
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (authService.login(loginDto.getUsername(), loginDto.getPassword())) {
                Optional<User> user = authService.getCurrentUser();
                UserDTO userDTO = userMapper.toDTO(user.get());

                HttpSession session = req.getSession(true);
                session.setAttribute("username", user.get().getUsername());
                session.setAttribute("userId", user.get().getId());

                String jsonResponse = objectMapper.writeValueAsString(userDTO);
                resp.getWriter().write(jsonResponse);
            } else {
                sendErrorResponse(resp, "Неверное имя пользователя или пароль",
                        HttpServletResponse.SC_UNAUTHORIZED);
            }

        } catch (Exception e) {
            sendErrorResponse(resp, "Неверное тело запроса: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Выполняет выход пользователя из системы.
     * Завершает текущую сессию и очищает данные аутентификации.
     *
     * @param req HTTP запрос
     * @param resp HTTP ответ с подтверждением выхода
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void logout(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String username = authService.getCurrentUsername();
        authService.logout();

        HttpSession session = req.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        sendSuccessResponse(resp, "Пользователь " + username + " вышел из системы");
    }

    /**
     * Регистрирует нового пользователя в системе.
     * Создает учетную запись с указанными учетными данными.
     *
     * @param req HTTP запрос с телом содержащим LoginDTO для регистрации
     * @param resp HTTP ответ с подтверждением регистрации или ошибкой
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void register(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            LoginDTO registerDto = objectMapper.readValue(req.getReader(), LoginDTO.class);

            String validationError = ValidationUtil.validate(registerDto);
            if (validationError != null) {
                sendErrorResponse(resp, "Ошибка валидации: " + validationError,
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            if (authService.register(registerDto.getUsername(), registerDto.getPassword())) {
                sendSuccessResponse(resp, "Пользователь " + registerDto.getUsername() + " зарегистрирован");
            } else {
                sendErrorResponse(resp, "Пользователь с таким именем уже существует",
                        HttpServletResponse.SC_BAD_REQUEST);
            }

        } catch (Exception e) {
            sendErrorResponse(resp, "Неверное тело запроса: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Отправляет HTTP ответ с ошибкой.
     *
     * @param resp HTTP ответ для записи
     * @param message сообщение об ошибке
     * @param statusCode HTTP статус код ошибки
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void sendErrorResponse(HttpServletResponse resp, String message, int statusCode) throws IOException {
        resp.setStatus(statusCode);
        String errorResponse = String.format("{\"error\": \"%s\", \"status\": %d}", message, statusCode);
        resp.getWriter().write(errorResponse);
    }

    /**
     * Отправляет HTTP ответ с успешным выполнением операции.
     *
     * @param resp HTTP ответ для записи
     * @param message сообщение об успехе
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void sendSuccessResponse(HttpServletResponse resp, String message) throws IOException {
        String successResponse = String.format("{\"message\": \"%s\", \"status\": %d}",
                message, HttpServletResponse.SC_OK);
        resp.getWriter().write(successResponse);
    }
}