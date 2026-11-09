package com.ledgerline.dto;

import java.util.UUID;

public record AuthMeResponse(
        UUID userId,
        String email,
        String role
) {
}