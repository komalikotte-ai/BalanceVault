package com.ledgerline.controller;

import com.ledgerline.dto.MoneyMovementRequest;
import com.ledgerline.dto.MoneyTransactionResponse;
import com.ledgerline.service.MoneyMovementService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/accounts")
public class AdminTransactionController {

    private final MoneyMovementService moneyMovementService;

    public AdminTransactionController(
            MoneyMovementService moneyMovementService) {
        this.moneyMovementService = moneyMovementService;
    }

    @PostMapping("/{accountId}/deposits")
    public MoneyTransactionResponse deposit(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId,
            @Valid @RequestBody MoneyMovementRequest request) {

        UUID adminId = UUID.fromString(jwt.getClaimAsString("userId"));

        return moneyMovementService.deposit(adminId, accountId, request);
    }
}
