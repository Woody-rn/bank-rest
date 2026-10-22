package com.example.bankcards.service;

import com.example.bankcards.dto.request.TransferRequest;
import com.example.bankcards.dto.response.TransferResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransferService {
    TransferResponse transfer(Long userId, TransferRequest request);

    Page<TransferResponse> getTransferHistory(Long cardId, Long userId, Pageable pageable);
}