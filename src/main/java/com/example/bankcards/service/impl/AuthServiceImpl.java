package com.example.bankcards.service.impl;

import com.example.bankcards.dto.response.AuthStatusResponse;
import com.example.bankcards.dto.request.UserLoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.security.CustomUserDetails;
import com.example.bankcards.security.service.JwtService;
import com.example.bankcards.service.AuthService;
import com.example.bankcards.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthStatusResponse signUp(UserRegistrationRequest request) {
        saveUser(request);
        return AuthStatusResponse.builder()
                .state("User has been successfully registered")
                .code(HttpStatus.OK.value())
                .timestamp(LocalDateTime.now())
                .build();
    }

    @Override
    public AuthStatusResponse signIn(UserLoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtService.generateJwtToken(userDetails);

            return AuthStatusResponse.builder()
                    .code(HttpStatus.OK.value())
                    .state("User has been authorized")
                    .timestamp(LocalDateTime.now())
                    .token(token)
                    .build();

        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}. Error: {}", request.getUsername(), e.getMessage(), e);
            return AuthStatusResponse.builder()
                    .code(HttpStatus.FORBIDDEN.value())
                    .state("Incorrect username or password is specified")
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    private void saveUser(UserRegistrationRequest request) {
        String fullName = request.getFullName();
        String username = request.getUsername();
        String password = passwordEncoder.encode(request.getPassword());

        userService.createUser(fullName, username, password);
    }

}
