package com.ledgerline.dto;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {}