package com.example.bankcards.service;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.List;

@Slf4j
@Component
@EnableScheduling
@RequiredArgsConstructor
public class CardExpiryScheduler {

    private final CardRepository cardRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void expireCards() {
        YearMonth now = YearMonth.now();

        List<Card> expiredCards = cardRepository.findByStatusAndIsDeletedFalse(CardStatus.ACTIVE)
                .stream()
                .filter(card -> isExpired(card, now))
                .peek(card -> card.setStatus(CardStatus.EXPIRED))
                .toList();

        if (!expiredCards.isEmpty()) {
            cardRepository.saveAll(expiredCards);
            log.info("Expired {} cards", expiredCards.size());
        }
    }

    private boolean isExpired(Card card, YearMonth now) {
        YearMonth cardExpiry = YearMonth.of(card.getExpiryYear(), card.getExpiryMonth());
        return cardExpiry.isBefore(now);
    }
}