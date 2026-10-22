package com.example.bankcards.service;

import com.example.bankcards.dto.response.BlockRequestResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BlockRequestService {
    BlockRequestResponse requestBlock(Long cardId, Long userId);

    Page<BlockRequestResponse> getPendingRequests(Pageable pageable);

    BlockRequestResponse approveRequest(Long requestId);

    BlockRequestResponse rejectRequest(Long requestId);
}