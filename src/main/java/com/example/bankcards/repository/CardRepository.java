package com.example.bankcards.repository;

import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CardRepository extends JpaRepository<Card, Long> {

    Page<Card> findByUserIdAndIsDeletedFalse(Long userId, Pageable pageable);

    Page<Card> findByUserIdAndStatusAndIsDeletedFalse(Long userId, CardStatus status, Pageable pageable);

    Page<Card> findAllByIsDeletedFalse(Pageable pageable);

    Optional<Card> findByIdAndIsDeletedFalse(Long id);

    Optional<Card> findByCardNumberHash(String cardNumberHash);

    boolean existsByCardNumberHash(String cardNumberHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Card c WHERE c.id = :id AND c.isDeleted = false")
    Optional<Card> findByIdWithLock(@Param("id") Long id);

    List<Card> findByStatusAndIsDeletedFalse(CardStatus status);
}
