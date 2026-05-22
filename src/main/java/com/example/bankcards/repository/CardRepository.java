package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Page<Card> findByUserIdAndStatusAndIsDeletedFalse(Long userId, CardStatus status, Pageable pageable);

    Page<Card> findAllByIsDeletedFalse(Pageable pageable);

    Optional<Card> findByIdAndIsDeletedFalse(Long id);

    Optional<Card> findByCardNumberHash(String cardNumberHash);

    boolean existsByCardNumberHash(String cardNumberHash);
}
