package com.ledgerline.controller;

import com.ledgerline.service.AuditorService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auditor")
public class AuditorController {

    private final AuditorService auditorService;

    public AuditorController(AuditorService auditorService) {
        this.auditorService = auditorService;
    }

    @GetMapping("/accounts")
    public List<Map<String, Object>> getAssignedAccounts(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return auditorService.assignedAccounts(getUserId(jwt));
    }

    @GetMapping("/accounts/{accountId}/transactions")
    public List<Map<String, Object>> getAccountTransactions(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return auditorService.accountTransactions(
                getUserId(jwt), accountId
        );
    }

    @GetMapping("/accounts/{accountId}/audit-events")
    public List<Map<String, Object>> getAccountAuditEvents(
            @PathVariable UUID accountId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return auditorService.accountAuditEvents(
                getUserId(jwt), accountId
        );
    }

    private UUID getUserId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }
}