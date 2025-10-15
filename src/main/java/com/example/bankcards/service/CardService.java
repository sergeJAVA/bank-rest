package com.example.bankcards.service;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardStatus;
import com.example.bankcards.dto.request.CreateCardRequest;

public interface CardService {

    CardDto createCard(CreateCardRequest request);

    void deleteCard(Long cardId);

    CardDto changeStatus(Long cardId, CardStatus cardStatus);

}
