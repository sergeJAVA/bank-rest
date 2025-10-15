package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardStatus;
import com.example.bankcards.dto.mapper.CardMapper;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CardDto createCard(CreateCardRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User with id <<" + request.getUserId() + ">> not found!"));

        Card card = Card.builder()
                .cardNum(request.getCardNum())
                .owner(user.getUsername())
                .expirationDate(LocalDateTime.now().plusYears(10L))
                .status(CardStatus.ACTIVE)
                .balance(new BigDecimal("0"))
                .user(user)
                .build();

        return CardMapper.toDto(cardRepository.save(card));
    }

    @Override
    @Transactional
    public void deleteCard(Long cardId) {
        cardRepository.deleteById(cardId);
    }

    @Override
    @Transactional
    public CardDto changeStatus(Long cardId, CardStatus cardStatus) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new UserNotFoundException("Card with id <<" + cardId + ">> not found!"));
        card.setStatus(cardStatus);
        return CardMapper.toDto(card);
    }

}
