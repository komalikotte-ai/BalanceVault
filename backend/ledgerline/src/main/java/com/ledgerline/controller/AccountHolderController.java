
package com.ledgerline.controller;

import com.ledgerline.dto.AccountOpeningRequestResponse;
import com.ledgerline.dto.AccountResponse;
import com.ledgerline.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/account-holder")
public class AccountHolderController {

    private final AccountService accountService;

    public AccountHolderController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping("/account-requests")
    public ResponseEntity<AccountOpeningRequestResponse> requestAccount(
            @AuthenticationPrincipal Jwt jwt
    ) {
        UUID applicantId = getUserId(jwt);

        AccountOpeningRequestResponse response =
                accountService.requestAccount(applicantId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/account-requests")
    public List<AccountOpeningRequestResponse> getMyRequests(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return accountService.getMyRequests(getUserId(jwt));
    }

    @GetMapping("/accounts")
    public List<AccountResponse> getMyAccounts(
            @AuthenticationPrincipal Jwt jwt
    ) {
        return accountService.getMyAccounts(getUserId(jwt));
    }

    private UUID getUserId(Jwt jwt) {
        return UUID.fromString(jwt.getClaimAsString("userId"));
    }
}
