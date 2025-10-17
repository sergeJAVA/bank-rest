package com.example.bankcards.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
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
