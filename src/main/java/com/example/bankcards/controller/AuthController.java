package com.example.bankcards.controller;

import com.example.bankcards.dto.response.AuthStatusResponse;
import com.example.bankcards.dto.request.UserLoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bank/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PutMapping("/signUp")
    public ResponseEntity<AuthStatusResponse> signUp(@RequestBody @Valid UserRegistrationRequest request) {
        AuthStatusResponse response = authService.signUp(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/signIn")
    public ResponseEntity<AuthStatusResponse> signIn(@RequestBody @Valid UserLoginRequest request) {
        AuthStatusResponse response = authService.signIn(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
