package org.idvairaz.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.idvairaz.dto.CreateUserDTO;
import org.idvairaz.dto.UserDTO;
import org.idvairaz.mapper.CreateUserMapper;
import org.idvairaz.mapper.UserMapper;
import org.idvairaz.model.User;
import org.idvairaz.service.UserService;
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
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServletTest {

    @Mock
    private UserService userService;

    @Mock
    private UserMapper userMapper;

    @Mock
    private CreateUserMapper createUserMapper;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private HttpSession session;

    private UserServlet userServlet;
    private ObjectMapper objectMapper;
    private StringWriter responseWriter;
    private SoftAssertions softly;

    @BeforeEach
    void setUp() {
        userServlet = new UserServlet();
        setField(userServlet, "userService", userService);
        setField(userServlet, "userMapper", userMapper);
        setField(userServlet, "createUserMapper", createUserMapper);
        setField(userServlet, "objectMapper", JacksonConfig.getObjectMapper());
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
    @DisplayName("GET /api/users - должен вернуть список всех пользователей")
    void doGet_ShouldReturnAllUsers() throws Exception {
        User user = Instancio.create(User.class);
        UserDTO userDTO = Instancio.create(UserDTO.class);

        when(request.getPathInfo()).thenReturn("/");
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(userService.getAllUsers()).thenReturn(List.of(user));
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        userServlet.doGet(request, response);

        softly.assertThat(responseWriter.toString()).contains(userDTO.getUsername());
        softly.assertThatCode(() -> {
            verify(response).setContentType("application/json");
            verify(userService).getAllUsers();
        }).doesNotThrowAnyException();
        softly.assertAll();
    }

    @Test
    @DisplayName("POST /api/users - должен создать нового пользователя и вернуть его данные")
    void doPost_ShouldCreateUser() throws Exception {
        CreateUserDTO createDto = Instancio.create(CreateUserDTO.class);
        User user = Instancio.create(User.class);
        UserDTO userDTO = Instancio.create(UserDTO.class);

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(objectMapper.writeValueAsString(createDto))));
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        when(createUserMapper.toEntity(any(CreateUserDTO.class))).thenReturn(user);
        when(userService.createUser(any(), any(), any())).thenReturn(user);
        when(userMapper.toDTO(user)).thenReturn(userDTO);

        userServlet.doPost(request, response);

        softly.assertThatCode(() -> {
            verify(response).setStatus(HttpServletResponse.SC_CREATED);
            verify(userService).createUser(any(), any(), any());
        }).doesNotThrowAnyException();
        softly.assertAll();
    }
}