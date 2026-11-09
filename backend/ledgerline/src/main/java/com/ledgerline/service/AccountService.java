
package com.ledgerline.service;

import com.ledgerline.dto.AccountOpeningRequestResponse;
import com.ledgerline.dto.AccountResponse;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.AccountOpeningRequest;
import com.ledgerline.entity.AppUser;
import com.ledgerline.enums.AccountType;
import com.ledgerline.enums.RequestStatus;
import com.ledgerline.enums.UserRole;
import com.ledgerline.enums.UserStatus;
import com.ledgerline.repository.AccountOpeningRequestRepository;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final AccountOpeningRequestRepository requestRepository;

    public AccountService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            AccountOpeningRequestRepository requestRepository
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.requestRepository = requestRepository;
    }

    @Transactional
    public AccountOpeningRequestResponse requestAccount(UUID applicantId) {
        AppUser applicant = userRepository.findById(applicantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Applicant not found"
                ));

        if (applicant.getRole() != UserRole.ACCOUNT_HOLDER
                || applicant.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only active account holders can request accounts"
            );
        }

        boolean alreadyPending = requestRepository
                .existsByApplicant_IdAndStatus(
                        applicantId, RequestStatus.PENDING
                );

        if (alreadyPending) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "You already have a pending account request"
            );
        }

        AccountOpeningRequest request =
                requestRepository.save(new AccountOpeningRequest(applicant));

        return toRequestResponse(request);
    }

    @Transactional(readOnly = true)
    public List<AccountOpeningRequestResponse> getMyRequests(UUID applicantId) {
        return requestRepository
                .findByApplicant_IdOrderByRequestedAtDesc(applicantId)
                .stream()
                .map(this::toRequestResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> getMyAccounts(UUID applicantId) {
        return accountRepository
                .findByOwner_IdOrderByCreatedAtDesc(applicantId)
                .stream()
                .map(this::toAccountResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AccountOpeningRequestResponse> getPendingRequests() {
        return requestRepository
                .findByStatusOrderByRequestedAtAsc(RequestStatus.PENDING)
                .stream()
                .map(this::toRequestResponse)
                .toList();
    }

    @Transactional
    public AccountOpeningRequestResponse approveRequest(
            UUID requestId,
            UUID reviewerId,
            String reviewNote
    ) {
        AccountOpeningRequest request = requestRepository
                .findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account request not found"
                ));

        ensurePending(request);

        AppUser reviewer = getActiveAdmin(reviewerId);
        AppUser applicant = request.getApplicant();

        if (applicant.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot approve a request from an inactive user"
            );
        }

        Account account = new Account(
                generateAccountNumber(),
                applicant,
                AccountType.CUSTOMER,
                0L
        );

        accountRepository.save(account);
        request.approve(reviewer, account, reviewNote);
        requestRepository.save(request);

        return toRequestResponse(request);
    }

    @Transactional
    public AccountOpeningRequestResponse rejectRequest(
            UUID requestId,
            UUID reviewerId,
            String reviewNote
    ) {
        AccountOpeningRequest request = requestRepository
                .findByIdForUpdate(requestId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Account request not found"
                ));

        ensurePending(request);

        AppUser reviewer = getActiveAdmin(reviewerId);

        request.reject(reviewer, reviewNote);
        requestRepository.save(request);

        return toRequestResponse(request);
    }

    private AppUser getActiveAdmin(UUID reviewerId) {
        AppUser reviewer = userRepository.findById(reviewerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Reviewer not found"
                ));

        if (reviewer.getRole() != UserRole.ADMIN
                || reviewer.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "An active administrator is required"
            );
        }

        return reviewer;
    }

    private void ensurePending(AccountOpeningRequest request) {
        if (request.getStatus() != RequestStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This account request has already been reviewed"
            );
        }
    }

    private String generateAccountNumber() {
        for (int attempt = 0; attempt < 5; attempt++) {
            StringBuilder number = new StringBuilder("LL");

            for (int i = 0; i < 16; i++) {
                number.append(RANDOM.nextInt(10));
            }

            String candidate = number.toString();

            if (!accountRepository.existsByAccountNumber(candidate)) {
                return candidate;
            }
        }

        throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not generate a unique account number"
        );
    }

    private AccountOpeningRequestResponse toRequestResponse(
            AccountOpeningRequest request
    ) {
        UUID accountId = request.getAccount() == null
                ? null
                : request.getAccount().getId();

        return new AccountOpeningRequestResponse(
                request.getId(),
                request.getStatus().name(),
                request.getReviewNote(),
                accountId,
                request.getRequestedAt(),
                request.getReviewedAt()
        );
    }

    private AccountResponse toAccountResponse(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType().name(),
                account.getCurrency(),
                account.getBalancePaise(),
                account.getStatus().name(),
                account.getCreatedAt()
        );
    }
}
