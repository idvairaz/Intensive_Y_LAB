package org.idvairaz.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.idvairaz.dto.CreateUserDTO;
import org.idvairaz.dto.UserDTO;
import org.idvairaz.mapper.CreateUserMapper;
import org.idvairaz.mapper.UserMapper;
import org.idvairaz.model.User;
import org.idvairaz.service.UserService;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервлет для управления пользователями через REST API.
 * Предоставляет endpoints для создания, получения, обновления и удаления пользователей.
 *
 * @author idvavraz
 * @version 1.0
 */
@WebServlet("/api/users/*")
public class UserServlet extends HttpServlet {

    /**
     * Сервис для работы с пользователями.
     */
    private UserService userService;

    /**
     * Маппер для преобразования User в UserDTO.
     */
    private UserMapper userMapper;

    /**
     * Маппер для преобразования CreateUserDTO в User.
     */
    private CreateUserMapper createUserMapper;

    /**
     * Объект для работы с JSON.
     */
    private ObjectMapper objectMapper;

    /**
     * Инициализирует сервлет, создавая необходимые зависимости.
     */
    @Override
    public void init() {
        this.userMapper = UserMapper.INSTANCE;
        this.createUserMapper = CreateUserMapper.INSTANCE;
        this.objectMapper = JacksonConfig.getObjectMapper();
        this.userService = ServiceFactory.getUserService();
    }

    /**
     * Обрабатывает GET запросы для получения информации о пользователях.
     * Поддерживает следующие endpoints:
     * - GET /api/users - все пользователи
     * - GET /api/users/{id} - пользователь по идентификатору
     *
     * @param req HTTP запрос
     * @param resp HTTP ответ с данными пользователей
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();

        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                getAllUsers(resp);
            } else {
                getUserById(resp, pathInfo);
            }
        } catch (Exception e) {
            sendErrorResponse(resp, "Внутренняя ошибка сервера: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Обрабатывает POST запросы для создания новых пользователей.
     * Endpoint: POST /api/users
     *
     * @param req HTTP запрос с телом содержащим CreateUserDTO
     * @param resp HTTP ответ с созданным пользователем
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            CreateUserDTO createDto = objectMapper.readValue(req.getReader(), CreateUserDTO.class);

            String validationError = ValidationUtil.validate(createDto);
            if (validationError != null) {
                sendErrorResponse(resp, "Ошибка валидации: " + validationError,
                        HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            createUser(resp, createDto);

        } catch (Exception e) {
            sendErrorResponse(resp, "Неверное тело запроса: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Обрабатывает PUT запросы для обновления ролей пользователей.
     * Endpoint: PUT /api/users/{id}
     *
     * @param req HTTP запрос с телом содержащим UserDTO
     * @param resp HTTP ответ с обновленным пользователем
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "Идентификатор пользователя обязателен для обновления",
                    HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            Long id = extractIdFromPath(pathInfo);

            UserDTO updateDto = objectMapper.readValue(req.getReader(), UserDTO.class);

            updateUser(resp, id, updateDto);

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "Неверный идентификатор пользователя",
                    HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(resp, "Неверное тело запроса: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Обрабатывает DELETE запросы для удаления пользователей.
     * Поддерживает следующие endpoints:
     * - DELETE /api/users/{id} - удаление по идентификатору
     * - DELETE /api/users/name/{username} - удаление по имени пользователя
     * Удаление доступно только администраторам.
     *
     * @param req HTTP запрос
     * @param resp HTTP ответ с подтверждением удаления
     * @throws IOException если произошла ошибка ввода-вывода
     */
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            sendErrorResponse(resp, "Путь запроса обязателен для удаления",
                    HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            if (pathInfo.startsWith("/name/")) {
                deleteUserByName(req, resp, pathInfo);
            } else {
                Long id = extractIdFromPath(pathInfo);
                deleteUserById(req, resp, id);
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "Неверный идентификатор пользователя",
                    HttpServletResponse.SC_BAD_REQUEST);
        } catch (Exception e) {
            sendErrorResponse(resp, "Внутренняя ошибка сервера: " + e.getMessage(),
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Возвращает список всех пользователей.
     *
     * @param resp HTTP ответ со списком пользователей
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void getAllUsers(HttpServletResponse resp) throws IOException {
        List<User> users = userService.getAllUsers();
        List<UserDTO> userDTOs = users.stream()
                .map(userMapper::toDTO)
                .collect(Collectors.toList());

        String jsonResponse = objectMapper.writeValueAsString(userDTOs);
        resp.getWriter().write(jsonResponse);
    }

    /**
     * Возвращает пользователя по идентификатору.
     *
     * @param resp HTTP ответ
     * @param pathInfo путь запроса
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void getUserById(HttpServletResponse resp, String pathInfo) throws IOException {
        try {
            Long id = extractIdFromPath(pathInfo);
            Optional<User> user = userService.getUserById(id);

            if (user.isPresent()) {
                UserDTO userDTO = userMapper.toDTO(user.get());
                String jsonResponse = objectMapper.writeValueAsString(userDTO);
                resp.getWriter().write(jsonResponse);
            } else {
                sendErrorResponse(resp, "Пользователь не найден", HttpServletResponse.SC_NOT_FOUND);
            }

        } catch (NumberFormatException e) {
            sendErrorResponse(resp, "Неверный идентификатор пользователя",
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Создает нового пользователя.
     *
     * @param resp HTTP ответ
     * @param createDto DTO с данными для создания пользователя
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void createUser(HttpServletResponse resp, CreateUserDTO createDto) throws IOException {
        try {
            User user = createUserMapper.toEntity(createDto);
            User savedUser = userService.createUser(user.getUsername(), user.getPassword(), user.getRole());
            UserDTO responseDto = userMapper.toDTO(savedUser);

            resp.setStatus(HttpServletResponse.SC_CREATED);
            String jsonResponse = objectMapper.writeValueAsString(responseDto);
            resp.getWriter().write(jsonResponse);

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, "Ошибка валидации: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Обновляет данные пользователя.
     *
     * @param resp HTTP ответ
     * @param id идентификатор пользователя
     * @param updateDto DTO с обновленными данными
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void updateUser(HttpServletResponse resp, Long id, UserDTO updateDto) throws IOException {
        try {
            Optional<User> existingUser = userService.getUserById(id);
            if (existingUser.isEmpty()) {
                sendErrorResponse(resp, "Пользователь не найден", HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            User existing = existingUser.get();

            User.UserRole newRole = updateDto.getRole() != null ? updateDto.getRole() : existing.getRole();

            User updatedUser = userService.updateUserRole(existing.getUsername(), newRole);
            UserDTO responseDto = userMapper.toDTO(updatedUser);

            String jsonResponse = objectMapper.writeValueAsString(responseDto);
            resp.getWriter().write(jsonResponse);

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, "Ошибка валидации: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param req HTTP запрос
     * @param resp HTTP ответ
     * @param id идентификатор пользователя
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void deleteUserById(HttpServletRequest req, HttpServletResponse resp, Long id) throws IOException {
        try {
            Long currentUserId = getCurrentUserId(req);
            if (currentUserId == null) {
                sendErrorResponse(resp, "Пользователь не аутентифицирован",
                        HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            Optional<User> user = userService.getUserById(id);
            if (user.isPresent()) {
                userService.deleteUserById(id, currentUserId);
                sendSuccessResponse(resp, "Пользователь удален успешно");
            } else {
                sendErrorResponse(resp, "Пользователь не найден", HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, "Ошибка удаления: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Удаляет пользователя по имени.
     * Доступно только администраторам.
     *
     * @param req HTTP запрос для проверки авторизации
     * @param resp HTTP ответ с подтверждением удаления
     * @param pathInfo путь запроса содержащий имя пользователя
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void deleteUserByName(HttpServletRequest req, HttpServletResponse resp, String pathInfo) throws IOException {
        try {
            String currentUsername = getCurrentUsername(req);
            if (currentUsername == null) {
                sendErrorResponse(resp, "Пользователь не аутентифицирован",
                        HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String username = pathInfo.substring("/name/".length());
            if (username.isEmpty()) {
                sendErrorResponse(resp, "Имя пользователя обязательно", HttpServletResponse.SC_BAD_REQUEST);
                return;
            }

            userService.deleteUserByName(username, currentUsername);
            sendSuccessResponse(resp, "Пользователь " + username + " удален успешно");

        } catch (IllegalArgumentException e) {
            sendErrorResponse(resp, "Ошибка удаления: " + e.getMessage(),
                    HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    /**
     * Получает идентификатор текущего пользователя из сессии.
     *
     * @param req HTTP запрос
     * @return идентификатор текущего пользователя или null если не аутентифицирован
     */
    private Long getCurrentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            return (Long) session.getAttribute("userId");
        }
        return null;
    }

    /**
     * Получает имя текущего пользователя из сессии.
     *
     * @param req HTTP запрос
     * @return имя текущего пользователя или null если не аутентифицирован
     */
    private String getCurrentUsername(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) {
            return (String) session.getAttribute("username");
        }
        return null;
    }

    /**
     * Отправляет успешный ответ.
     *
     * @param resp HTTP ответ
     * @param message сообщение об успехе
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void sendSuccessResponse(HttpServletResponse resp, String message) throws IOException {
        String successResponse = String.format("{\"message\": \"%s\", \"status\": %d}",
                message, HttpServletResponse.SC_OK);
        resp.getWriter().write(successResponse);
    }

    /**
     * Извлекает идентификатор из пути запроса.
     *
     * @param pathInfo путь запроса
     * @return идентификатор
     */
    private Long extractIdFromPath(String pathInfo) {
        String idStr = pathInfo.substring(1);
        return Long.parseLong(idStr);
    }

    /**
     * Отправляет ответ с ошибкой.
     *
     * @param resp HTTP ответ
     * @param message сообщение об ошибке
     * @param statusCode код статуса HTTP
     * @throws IOException если произошла ошибка ввода-вывода
     */
    private void sendErrorResponse(HttpServletResponse resp, String message, int statusCode) throws IOException {
        resp.setStatus(statusCode);
        String errorResponse = String.format("{\"error\": \"%s\", \"status\": %d}", message, statusCode);
        resp.getWriter().write(errorResponse);
    }
}