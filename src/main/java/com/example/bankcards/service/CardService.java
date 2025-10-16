package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardStatus;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CardService {

    CardDto createCard(CreateCardRequest request);

    void deleteCard(Long cardId);

    CardDto changeStatus(Long cardId, CardStatus cardStatus);

    CardDto findById(Long cardId);

    Page<CardDto> findAllByUserId(Long userId, int page, int size);

    Page<CardDto> findAll();

}
