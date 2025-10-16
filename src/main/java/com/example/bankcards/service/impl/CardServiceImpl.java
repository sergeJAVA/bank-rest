package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardStatus;
import com.example.bankcards.dto.mapper.CardMapper;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardNumberTakenException;
import com.example.bankcards.exception.InvalidCardNumberException;
import com.example.bankcards.exception.UserNotFoundException;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.service.CardService;
import com.example.bankcards.util.CardSpecifications;
import com.example.bankcards.util.NumberEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public CardDto createCard(CreateCardRequest request) {
        if (!isValidCardNum(request.getCardNum().trim())) {
            throw new InvalidCardNumberException("The card number contains characters that are not numbers!");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new UserNotFoundException("User with id <<" + request.getUserId() + ">> not found!"));

        existCardByCardNum(request.getCardNum());

        Card card = Card.builder()
                .cardNum(request.getCardNum().trim())
                .owner(user.getFullName())
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

    @Override
    @Transactional(readOnly = true)
    public CardDto findById(Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with id <<" + cardId + ">> not found!"));
        CardDto cardDto = CardMapper.toDto(card);
        cardDto.setCardNum(NumberEncryptionUtil.encryptCardNumber(cardDto.getCardNum()));
        return cardDto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> findAllByUserId(Long userId, int page, int size) {
        Specification<Card> spec = CardSpecifications.userId(userId);
        Page<Card> cards = cardRepository.findAll(spec, PageRequest.of(page, size));
        return cards.map(CardMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardDto> findAll() {
        return null;
    }

    private void existCardByCardNum(String cardNum) {
        Optional<Card> card = cardRepository.findByCardNum(cardNum);
        if (card.isPresent()) {
            throw new CardNumberTakenException("Card with number <<" + cardNum + ">> already exists!");
        }
    }

    private boolean isValidCardNum(String cardNum) {
        return cardNum.matches("\\d+");
    }

}
