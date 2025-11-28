package org.idvairaz.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.idvairaz.dto.LoginDTO;
import org.idvairaz.dto.UserDTO;
import org.idvairaz.mapper.UserMapper;
import org.idvairaz.model.User;
import org.idvairaz.service.AuthService;
import org.instancio.Instancio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.assertj.core.api.SoftAssertions;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServletTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private AuthServlet authServlet;
    private ObjectMapper objectMapper;
    private StringWriter responseWriter;
    private SoftAssertions softly;

    @BeforeEach
    void setUp() {
        authServlet = new AuthServlet();
        setField(authServlet, "authService", authService);
        setField(authServlet, "userMapper", userMapper);
        setField(authServlet, "objectMapper", JacksonConfig.getObjectMapper());
        objectMapper = JacksonConfig.getObjectMapper();
        responseWriter = new StringWriter();
        softly = new SoftAssertions();
    }

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @DisplayName("POST /api/auth/login - должен успешно аутентифицировать пользователя и вернуть данные пользователя")
    void doPost_ShouldLoginSuccessfully() throws Exception {
        LoginDTO loginDto = Instancio.create(LoginDTO.class);
        User user = Instancio.create(User.class);
        UserDTO userDTO = Instancio.create(UserDTO.class);

        when(request.getPathInfo()).thenReturn("/login");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(objectMapper.writeValueAsString(loginDto))));
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(authService.login(loginDto.getUsername(), loginDto.getPassword())).thenReturn(true);
        when(authService.getCurrentUser()).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);
        when(request.getSession(true)).thenReturn(session);

        authServlet.doPost(request, response);

        softly.assertThat(responseWriter.toString()).contains(userDTO.getUsername());
        softly.assertThatCode(() -> {
            verify(authService).login(loginDto.getUsername(), loginDto.getPassword());
            verify(session).setAttribute("username", user.getUsername());
            verify(session).setAttribute("userId", user.getId());
            verify(response).setContentType("application/json");
        }).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("POST /api/auth/login - должен вернуть 401 при неверных учетных данных")
    void doPost_ShouldReturn401ForInvalidCredentials() throws Exception {
        LoginDTO loginDto = Instancio.create(LoginDTO.class);

        when(request.getPathInfo()).thenReturn("/login");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(objectMapper.writeValueAsString(loginDto))));
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(authService.login(loginDto.getUsername(), loginDto.getPassword())).thenReturn(false);

        authServlet.doPost(request, response);

        softly.assertThat(responseWriter.toString()).contains("Неверное имя пользователя или пароль");
        softly.assertThatCode(() -> verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED)).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("POST /api/auth/register - должен успешно зарегистрировать нового пользователя")
    void doPost_ShouldRegisterUser() throws Exception {
        LoginDTO registerDto = Instancio.create(LoginDTO.class);

        when(request.getPathInfo()).thenReturn("/register");
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(objectMapper.writeValueAsString(registerDto))));
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(authService.register(registerDto.getUsername(), registerDto.getPassword())).thenReturn(true);

        authServlet.doPost(request, response);

        var responseJson = objectMapper.readTree(responseWriter.toString());
        softly.assertThat(responseJson.has("message")).isTrue();
        softly.assertThat(responseJson.get("message").asText()).contains(registerDto.getUsername() + " зарегистрирован");
        softly.assertThat(responseJson.has("status")).isTrue();
        softly.assertThat(responseJson.get("status").asInt()).isEqualTo(200);
        softly.assertThatCode(() -> verify(authService).register(registerDto.getUsername(), registerDto.getPassword())).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("GET /api/auth - должен вернуть данные текущего аутентифицированного пользователя")
    void doGet_ShouldReturnCurrentUser() throws Exception {
        User user = Instancio.create(User.class);
        UserDTO userDTO = Instancio.create(UserDTO.class);

        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(authService.getCurrentUser()).thenReturn(Optional.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        authServlet.doGet(request, response);

        softly.assertThat(responseWriter.toString()).contains(userDTO.getUsername());
        softly.assertThatCode(() -> {
            verify(authService).getCurrentUser();
            verify(response).setContentType("application/json");
        }).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("GET /api/auth - должен вернуть 401 если пользователь не аутентифицирован")
    void doGet_ShouldReturn401WhenNotAuthenticated() throws Exception {
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(authService.getCurrentUser()).thenReturn(Optional.empty());

        authServlet.doGet(request, response);

        softly.assertThat(responseWriter.toString()).contains("Пользователь не аутентифицирован");
        softly.assertThatCode(() -> verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED)).doesNotThrowAnyException();
        softly.assertAll();
    }
}