package com.ledgerline.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record MoneyMovementRequest(

    @NotNull
    @DecimalMin("0.01")
    @Digits(integer = 15, fraction = 2)
    BigDecimal amount,

    UUID destinationAccountId,

    @NotBlank
    @Size(max = 128)
    String idempotencyKey,

    @Size(max = 255)
    String description
) {}
