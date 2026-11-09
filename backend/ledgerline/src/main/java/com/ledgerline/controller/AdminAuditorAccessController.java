package com.ledgerline.controller;

import com.ledgerline.service.AuditorAccessService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/auditors")
public class AdminAuditorAccessController {

    private final AuditorAccessService accessService;

    public AdminAuditorAccessController(
            AuditorAccessService accessService
    ) {
        this.accessService = accessService;
    }

    @PostMapping("/{auditorId}/accounts/{accountId}")
    public Map<String, Object> grantAccess(
            @PathVariable UUID auditorId,
            @PathVariable UUID accountId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return accessService.grantAccess(
                getUserId(jwt), auditorId, accountId
        );
    }

    @DeleteMapping("/{auditorId}/accounts/{accountId}")
    public Map<String, Object> revokeAccess(
            @PathVariable UUID auditorId,
            @PathVariable UUID accountId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        return accessService.revokeAccess(
                getUserId(jwt), auditorId, accountId
        );
    }

    private UUID getUserId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }
}