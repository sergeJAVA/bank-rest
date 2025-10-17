package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRequest {

    @NotBlank(message = "The username must not be blank!")
    @Size(min = 4, message = "The username must be at least 4 characters long")
    @Schema(description = "Логин", example = "ADMIN")
    private String username;

    @NotBlank(message = "The password must not be blank!")
    @Size(min = 8, message = "The password must be at least 8 characters long")
    @Schema(description = "Пароль", example = "password123")
    private String password;

}
