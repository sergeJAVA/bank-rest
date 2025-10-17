package com.example.bankcards.dto.request;

import com.example.bankcards.dto.CardStatus;
import jakarta.validation.constraints.NotNull;
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
public class ChangeCardStatusRequest {

    @NotNull(message = "The cardId must not be null!")
    private Long cardId;

    @NotNull(message = "The card status must not be null!")
    private CardStatus cardStatus;

}
