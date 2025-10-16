package com.example.bankcards.service;

import com.example.bankcards.dto.CardStatus;
import com.example.bankcards.entity.Card;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardStatusScheduler {

    private final CardRepository cardRepository;

    @Scheduled(fixedDelayString = "${scheduling.fixedDelay}")
    @Transactional
    public void updateExpiredCards() {
        LocalDateTime now = LocalDateTime.now();

        List<Card> expiredCards = cardRepository.findByExpirationDateBeforeAndStatusNot(now, CardStatus.EXPIRED);

        for (Card card : expiredCards) {
            card.setStatus(CardStatus.EXPIRED);
        }

        if (!expiredCards.isEmpty()) {
            log.info("Updated {} cards to EXPIRED.", expiredCards.size());
        }
    }

}
