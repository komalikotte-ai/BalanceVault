package com.ledgerline.dto;

import com.ledgerline.entity.MoneyTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record MoneyTransactionResponse(
    UUID transactionId,
    String transactionType,
    String status,
    BigDecimal amount,
    String currency,
    UUID sourceAccountId,
    UUID destinationAccountId,
    String description,
    Instant createdAt,
    Instant completedAt
) {
    public static MoneyTransactionResponse from(MoneyTransaction transaction) {
        return new MoneyTransactionResponse(
            transaction.getId(),
            transaction.getTransactionType().name(),
            transaction.getStatus().name(),
            BigDecimal.valueOf(transaction.getAmountPaise(), 2),
            transaction.getCurrency(),
            transaction.getSourceAccount().getId(),
            transaction.getDestinationAccount().getId(),
            transaction.getDescription(),
            transaction.getCreatedAt(),
            transaction.getCompletedAt()
        );
    }
}
