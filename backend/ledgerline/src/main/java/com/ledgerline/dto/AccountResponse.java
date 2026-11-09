package com.ledgerline.dto;

import java.time.Instant;
import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber,
        String accountType,
        String currency,
        Long balancePaise,
        String status,
        Instant createdAt
) {
}