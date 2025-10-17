package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.UserLoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.dto.response.AuthStatusResponse;
import com.example.bankcards.exception.UsernameTakenException;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationProvider provider;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void signIn_Success() {
        UserLoginRequest request = new UserLoginRequest("serega", "password123");

        Authentication successAuthentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(
                1L,
                request.getUsername(),
                request.getPassword(),
                Set.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        when(provider.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(successAuthentication);
        when(successAuthentication.getPrincipal()).thenReturn(userDetails);
        when(jwtService.generateJwtToken(userDetails)).thenReturn("jwt");

        AuthStatusResponse response = authService.signIn(request);

        assertNotNull(response);
        assertEquals(200, response.getCode());
        assertEquals("User has been authorized", response.getState());
        assertEquals("jwt", response.getToken());

        verify(provider, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService, times(1)).generateJwtToken(userDetails);
    }

    @Test
    void signIn_Failure() {
        UserLoginRequest request = new UserLoginRequest("serega", "password123");

        when(provider.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        AuthStatusResponse response = authService.signIn(request);
        assertNotNull(response);
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getCode());
        assertEquals("Incorrect username or password is specified", response.getState());
        assertNull(response.getToken(), "Токена быть не должно при неудачной аутентификации");

        verify(provider, times(1)).authenticate(any());
        verify(jwtService, times(0)).generateJwtToken(any());
    }

    @Test
    void signUp_Success() {
        UserRegistrationRequest request = new UserRegistrationRequest("Test", "serega", "password123");
        String encodedPassword = "encodedPass";
        when(passwordEncoder.encode(any())).thenReturn(encodedPassword);
        AuthStatusResponse response = authService.signUp(request);

        assertNotNull(response);
        assertEquals(HttpStatus.OK.value(), response.getCode());
        assertEquals("User has been successfully registered", response.getState());

        verify(passwordEncoder, times(1)).encode(request.getPassword());
        verify(userService, times(1)).createUser(request.getFullName(), request.getUsername(), encodedPassword);
    }

    @Test
    void signUp_Failure() {
        UserRegistrationRequest request = new UserRegistrationRequest("Test", "serega", "password123");

        String encodedPassword = "encodedPassword456";

        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);

        doThrow(new UsernameTakenException("Username already exists"))
                .when(userService).createUser(request.getFullName(), request.getUsername(), encodedPassword);

        Exception exception = assertThrows(UsernameTakenException.class, () -> {
            authService.signUp(request);
        });

        assertEquals("Username already exists", exception.getMessage());

        verify(passwordEncoder, times(1)).encode("password123");
        verify(userService, times(1)).createUser("Test", "serega", encodedPassword);
    }

}