package com.example.bankcards.service.impl;

import com.example.bankcards.dto.CardDto;
import com.example.bankcards.dto.CardStatus;
import com.example.bankcards.dto.mapper.CardMapper;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.User;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardNumberTakenException;
import com.example.bankcards.exception.ImpossibleMoneyTransferException;
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
import java.util.List;
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
    public Page<CardDto> findAll(int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        return cardRepository.findAll(pageRequest)
                .map(CardMapper::toDto);
    }

    @Transactional
    public void transfer(Long senderId, String firstCardNum, String secondCardNum, BigDecimal amount) {
        if (!cardNumberVerification(senderId, firstCardNum.trim())) {
            throw new ImpossibleMoneyTransferException("Card with number <<" + firstCardNum + ">> doesn't belong to this user!");
        }

        if (!isValidCardNum(firstCardNum.trim()) ||
            !isValidCardNum(secondCardNum.trim())) {
            throw new InvalidCardNumberException("The card number contains characters that are not numbers!");
        }
        Card firstCard = cardRepository.findByCardNum(firstCardNum.trim())
                .orElseThrow(() -> new CardNotFoundException("Card with number <<" + firstCardNum + ">> not found!"));
        Card secondCard = cardRepository.findByCardNum(secondCardNum.trim())
                .orElseThrow(() -> new CardNotFoundException("Card with number <<" + secondCardNum + ">> not found!"));
        balanceTransfer(firstCard, secondCard, amount);
    }

    @Transactional
    public void depositMoney(Long userId, String cardNum, BigDecimal amount) {
        if (!cardNumberVerification(userId, cardNum.trim())) {
            throw new ImpossibleMoneyTransferException("Card with number <<" + cardNum + ">> doesn't belong to this user!");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }

        Card card = cardRepository.findByCardNum(cardNum.trim())
                .orElseThrow(() -> new CardNotFoundException("Card with number <<" + cardNum + ">> not found!"));

        if (!isValidCardStatus(card)) {
            throw new IllegalArgumentException("You cannot deposit money onto this card: This card is blocked or expired.");
        }

        card.setBalance(card.getBalance().add(amount));
    }

    @Transactional
    public void blockCard(Long userId, Long cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card with id <<" + cardId + ">> not found!"));

        if (!card.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Card with id <<" + cardId + ">> doesn't belong to this user!");
        }

        if (!isValidCardStatus(card)) {
            throw new IllegalArgumentException("This card has already been blocked or has expired.");
        }
        card.setStatus(CardStatus.BLOCKED);
    }

    @Transactional(readOnly = true)
    public BigDecimal getBalance(Long userId, String cardNum) {
        if (!isValidCardNum(cardNum)) {
            throw new InvalidCardNumberException("The card number contains characters that are not numbers!");
        }
        Card card = cardRepository.findByCardNumAndUser_Id(cardNum, userId)
                .orElseThrow(() -> new CardNotFoundException("Card with cardNum <<" + cardNum
                        + ">> doesn't belong to this user or not found!"));
        return card.getBalance();
    }

    private boolean cardNumberVerification(Long senderId, String firstCardNum) {
        boolean successVerification = false;

        List<Card> cards = cardRepository.findAllByUserId(senderId);
        if (cards.isEmpty()) {
            throw new CardNotFoundException("The user doesn't have any cards.");
        }

        for (Card card : cards) {
            if (card.getCardNum().equals(firstCardNum)) {
                successVerification = true;
                break;
            }
        }
        return successVerification;
    }

    private void balanceTransfer(Card from, Card to, BigDecimal amount) {
        if (!isValidCardStatus(from) || !isValidCardStatus(to)) {
            throw new ImpossibleMoneyTransferException("One of the cards is blocked or expired.");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Transfer amount must be greater than zero.");
        }

        BigDecimal fromBalance = from.getBalance();
        if (fromBalance.compareTo(amount) < 0) {
            throw new ImpossibleMoneyTransferException("Insufficient funds on the sender's card.");
        }

        from.setBalance(fromBalance.subtract(amount));
        to.setBalance(to.getBalance().add(amount));
    }

    private boolean isValidCardStatus(Card card) {
        return card.getStatus().equals(CardStatus.ACTIVE);
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
