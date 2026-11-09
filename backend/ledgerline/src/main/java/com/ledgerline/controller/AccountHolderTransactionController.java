package com.ledgerline.controller;

import com.ledgerline.dto.MoneyMovementRequest;
import com.ledgerline.dto.MoneyTransactionResponse;
import com.ledgerline.service.MoneyMovementService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account-holder")
public class AccountHolderTransactionController {

    private final MoneyMovementService moneyMovementService;

    public AccountHolderTransactionController(
            MoneyMovementService moneyMovementService) {
        this.moneyMovementService = moneyMovementService;
    }

    @PostMapping("/accounts/{accountId}/withdrawals")
    public MoneyTransactionResponse withdraw(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId,
            @Valid @RequestBody MoneyMovementRequest request) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return moneyMovementService.withdraw(userId, accountId, request);
    }

    @PostMapping("/accounts/{accountId}/transfers")
    public MoneyTransactionResponse transfer(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID accountId,
            @Valid @RequestBody MoneyMovementRequest request) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return moneyMovementService.transfer(userId, accountId, request);
    }

    @GetMapping("/transactions")
    public List<MoneyTransactionResponse> history(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = UUID.fromString(jwt.getClaimAsString("userId"));

        return moneyMovementService.history(userId);
    }
}
