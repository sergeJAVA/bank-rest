package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateCardRequest {

    @Size(min = 16, max = 16, message = "The card number must be 16 characters long!")
    @NotBlank(message = "The card number must not be blank!")
    private String cardNum;

    @NotNull(message = "The card number must not be null!")
    private Long userId;

}
