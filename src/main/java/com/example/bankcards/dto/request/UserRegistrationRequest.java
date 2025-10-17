package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserRegistrationRequest {

    @NotEmpty(message = "fullName must not be empty!")
    @Schema(description = "ФИО/ФИ")
    private String fullName;

    @NotEmpty(message = "username must not be empty!")
    @Size(min = 4, message = "The username must be at least 4 characters long")
    @Schema(description = "Логин")
    private String username;

    @NotEmpty(message = "password must not be empty!")
    @Size(min = 8, message = "The password must be at least 8 characters long")
    @Schema(description = "Пароль")
    private String password;

}
