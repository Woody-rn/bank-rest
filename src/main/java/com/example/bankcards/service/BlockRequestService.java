package com.example.bankcards.service;

import com.example.bankcards.entity.BlockRequest;
import com.example.bankcards.entity.BlockRequestStatus;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.CardStatus;
import com.example.bankcards.exception.BlockRequestNotFoundException;
import com.example.bankcards.exception.CardAccessDeniedException;
import com.example.bankcards.exception.CardNotFoundException;
import com.example.bankcards.exception.CardValidationException;
import com.example.bankcards.repository.BlockRequestRepository;
import com.example.bankcards.repository.CardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlockRequestService {

    private final BlockRequestRepository blockRequestRepository;
    private final CardRepository cardRepository;

    @Transactional
    public BlockRequest requestBlock(Long cardId, Long userId) {
        Card card = getCard(cardId);

        validateCardOwnership(card, userId);
        validateCardActive(card);
        validateNoDuplicateRequest(cardId);

        BlockRequest request = buildRequest(card);
        request = blockRequestRepository.save(request);

        log.info("Block request created: id={}, cardId={}, userId={}", request.getId(), cardId, userId);
        return request;
    }

    public Page<BlockRequest> getPendingRequests(Pageable pageable) {
        return blockRequestRepository.findByStatus(BlockRequestStatus.PENDING, pageable);
    }

    @Transactional
    public BlockRequest approveRequest(Long requestId) {
        BlockRequest request = getRequestOrThrow(requestId);
        validateRequestPending(request);
        validateCardNotDeleted(request.getCard());

        request.setStatus(BlockRequestStatus.APPROVED);
        request.getCard().setStatus(CardStatus.BLOCKED);

        log.info("Block request approved: id={}, cardId={}", requestId, request.getCard().getId());
        return request;
    }

    @Transactional
    public BlockRequest rejectRequest(Long requestId) {
        BlockRequest request = getRequestOrThrow(requestId);
        validateRequestPending(request);
        validateCardNotDeleted(request.getCard());

        request.setStatus(BlockRequestStatus.REJECTED);

        log.info("Block request rejected: id={}, cardId={}", requestId, request.getCard().getId());
        return request;
    }

    private Card getCard(Long cardId) {
        return cardRepository.findByIdAndIsDeletedFalse(cardId)
                .orElseThrow(() -> new CardNotFoundException("Card not found: " + cardId));
    }

    private BlockRequest getRequestOrThrow(Long requestId) {
        return blockRequestRepository.findById(requestId)
                .orElseThrow(() -> new BlockRequestNotFoundException("Block request not found: " + requestId));
    }

    private void validateCardOwnership(Card card, Long userId) {
        if (!card.getUser().getId().equals(userId)) {
            throw new CardAccessDeniedException("You don't own this card");
        }
    }

    private void validateCardActive(Card card) {
        if (card.getStatus() != CardStatus.ACTIVE) {
            throw new CardValidationException("Only active cards can be blocked. Current status: " + card.getStatus());
        }
    }

    private void validateNoDuplicateRequest(Long cardId) {
        if (blockRequestRepository.existsByCardIdAndStatus(cardId, BlockRequestStatus.PENDING)) {
            throw new CardValidationException("Block request already exists for this card");
        }
    }

    private void validateRequestPending(BlockRequest request) {
        if (request.getStatus() != BlockRequestStatus.PENDING) {
            throw new CardValidationException("Request is not pending: " + request.getStatus());
        }
    }

    private void validateCardNotDeleted(Card card) {
        if (card.getIsDeleted()) {
            throw new CardValidationException("Cannot process request: card has been deleted");
        }
    }

    private BlockRequest buildRequest(Card card) {
        return BlockRequest.builder()
                .card(card)
                .user(card.getUser())
                .status(BlockRequestStatus.PENDING)
                .build();
    }
}