package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CardDto {

    private Long id;

    private String cardNum;

    private String owner;

    private LocalDateTime expirationDate;

    private String status;

    private BigDecimal balance;
}
