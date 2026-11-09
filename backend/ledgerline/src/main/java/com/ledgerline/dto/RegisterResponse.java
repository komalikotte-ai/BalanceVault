package com.ledgerline.dto;

import java.util.UUID;

public record RegisterResponse(
        UUID id,
        String fullName,
        String email,
        String role,
        String status
) {}