
package com.ledgerline.controller;

import com.ledgerline.dto.AccountOpeningRequestResponse;
import com.ledgerline.dto.ReviewAccountRequest;
import com.ledgerline.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/account-requests")
public class AdminAccountController {

    private final AccountService accountService;

    public AdminAccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountOpeningRequestResponse> getPendingRequests() {
        return accountService.getPendingRequests();
    }

    @PostMapping("/{requestId}/approve")
    public AccountOpeningRequestResponse approveRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody(required = false) ReviewAccountRequest body,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String note = body == null ? null : body.reviewNote();

        return accountService.approveRequest(
                requestId,
                getUserId(jwt),
                note
        );
    }

    @PostMapping("/{requestId}/reject")
    public AccountOpeningRequestResponse rejectRequest(
            @PathVariable UUID requestId,
            @Valid @RequestBody(required = false) ReviewAccountRequest body,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String note = body == null ? null : body.reviewNote();

        return accountService.rejectRequest(
                requestId,
                getUserId(jwt),
                note
        );
    }

    private UUID getUserId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }
}
