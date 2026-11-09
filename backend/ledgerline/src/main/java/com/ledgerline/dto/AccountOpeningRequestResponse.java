package com.ledgerline.dto;

import java.time.Instant;
import java.util.UUID;

public record AccountOpeningRequestResponse(
        UUID id,
        String status,
        String reviewNote,
        UUID accountId,
        Instant requestedAt,
        Instant reviewedAt
) {
}