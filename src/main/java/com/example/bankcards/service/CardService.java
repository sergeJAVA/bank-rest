package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardStatus;
import com.example.bankcards.dto.request.CreateCardRequest;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;

public interface CardService {

    CardDto createCard(CreateCardRequest request);

    void deleteCard(Long cardId);

    CardDto changeStatus(Long cardId, CardStatus cardStatus);

    CardDto findById(Long cardId);

    Page<CardDto> findAllByUserId(Long userId, int page, int size);

    Page<CardDto> findAll(int page, int size);

    void transfer(Long userId,String firstCardNum, String secondCardNum, BigDecimal amount);

    void depositMoney(Long userId, String cardNum, BigDecimal amount);

    void blockCard(Long userId, Long cardId);

    BigDecimal getBalance(Long userId, String cardNum);
}
