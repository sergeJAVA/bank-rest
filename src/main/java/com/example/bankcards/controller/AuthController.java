package com.example.bankcards.controller;

import com.example.bankcards.dto.response.ErrorResponse;
import com.example.bankcards.dto.response.AuthStatusResponse;
import com.example.bankcards.dto.request.UserLoginRequest;
import com.example.bankcards.dto.request.UserRegistrationRequest;
import com.example.bankcards.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/bank/auth")
@RequiredArgsConstructor
@Tag(name = "Регистрация, аутентификация и авторизация пользователя", description = "Операции, " +
        "связанные с созданием нового пользователя и выдачей JWT, если он вошёл в систему.")
public class AuthController {

    private final AuthService authService;

    @PutMapping("/signUp")
    @Operation(summary = "Для регистрации нового пользователя.", description = "Логин должен быть уникальным.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно зарегистрирован",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthStatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Если ввести неуникальный логин или невалидные данные.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthStatusResponse> signUp(@RequestBody @Valid UserRegistrationRequest request) {
        AuthStatusResponse response = authService.signUp(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PostMapping("/signIn")
    @Operation(summary = "Для входа и получения JWT.", description = "Нужно передать логин и пароль.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Пользователь успешно прошел аутентификацию.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = AuthStatusResponse.class))),
            @ApiResponse(responseCode = "403", description = "Не получилось пройти аутентификацию.",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, schema = @Schema(implementation = AuthStatusResponse.class))),
            @ApiResponse(responseCode = "400", description = "Если ввести невалидные данные.",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<AuthStatusResponse> signIn(@RequestBody @Valid UserLoginRequest request) {
        AuthStatusResponse response = authService.signIn(request);
        return ResponseEntity.status(response.getCode()).body(response);
    }

}
