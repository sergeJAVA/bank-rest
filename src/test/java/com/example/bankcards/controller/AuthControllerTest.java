package com.example.bankcards.controller;

import com.example.bankcards.config.security.SecurityConfig;
import com.example.bankcards.dto.request.UserLoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.dto.response.AuthStatusResponse;
import com.example.bankcards.exception.UsernameTakenException;
import com.example.bankcards.security.filter.JwtRequestFilter;
import com.example.bankcards.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtRequestFilter.class),
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class)
})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    @DisplayName("signUp: успешная регистрация пользователя (200 OK)")
    void signUp_Success() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFullName("Test test");
        request.setUsername("newUser");
        request.setPassword("password123");

        AuthStatusResponse response = new AuthStatusResponse();
        response.setCode(200);
        response.setState("Пользователь успешно зарегистрирован");

        Mockito.when(authService.signUp(any(UserRegistrationRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/v1/bank/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.state").value("Пользователь успешно зарегистрирован"));
    }

    @Test
    @DisplayName("signUp: ошибка при неуникальном логине (400 Bad Request)")
    void signUp_DuplicateLogin() throws Exception {
        UserRegistrationRequest request = new UserRegistrationRequest();
        request.setFullName("Test");
        request.setUsername("existingUser");
        request.setPassword("password123");


        Mockito.doThrow(new UsernameTakenException("Username <<" + request.getUsername() + ">> is already taken!"))
                .when(authService).signUp(any(UserRegistrationRequest.class));

        mockMvc.perform(put("/api/v1/bank/auth/signUp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.error")
                        .value("Username <<" + request.getUsername() + ">> is already taken!"));
    }

    @Test
    @DisplayName("signIn: успешная аутентификация (200 OK)")
    void signIn_Success() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("validUser");
        request.setPassword("validPass");

        AuthStatusResponse response = new AuthStatusResponse();
        response.setCode(200);
        response.setState("User has been authorized");
        response.setToken("jwt");

        Mockito.when(authService.signIn(any(UserLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/bank/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value("jwt"))
                .andExpect(jsonPath("$.state").value("User has been authorized"));
    }

    @Test
    @DisplayName("signIn: неудачная аутентификация (403 Forbidden)")
    void signIn_invalidCredentials() throws Exception {
        UserLoginRequest request = new UserLoginRequest();
        request.setUsername("wrongUser");
        request.setPassword("wrongPass");

        AuthStatusResponse response = new AuthStatusResponse();
        response.setCode(403);
        response.setState("Incorrect username or password is specified");

        Mockito.when(authService.signIn(any(UserLoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/bank/auth/signIn")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.state").value("Incorrect username or password is specified"));
    }

}