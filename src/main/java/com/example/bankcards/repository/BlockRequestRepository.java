package com.example.bankcards.repository;

import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.BlockRequestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockRequestRepository extends JpaRepository<BlockRequest, Long> {

    Page<BlockRequest> findByStatus(BlockRequestStatus status, Pageable pageable);

    List<BlockRequest> findByCardId(Long cardId);

    boolean existsByCardIdAndStatus(Long cardId, BlockRequestStatus status);
}
