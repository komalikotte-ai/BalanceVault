package com.ledgerline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ledgerline.dto.MoneyMovementRequest;
import com.ledgerline.dto.MoneyTransactionResponse;
import com.ledgerline.entity.Account;
import com.ledgerline.entity.AppUser;
import com.ledgerline.entity.LedgerEntry;
import com.ledgerline.entity.MoneyTransaction;
import com.ledgerline.enums.AccountStatus;
import com.ledgerline.enums.AccountType;
import com.ledgerline.enums.LedgerSide;
import com.ledgerline.enums.TransactionType;
import com.ledgerline.enums.UserRole;
import com.ledgerline.enums.UserStatus;
import com.ledgerline.repository.AccountRepository;
import com.ledgerline.repository.LedgerEntryRepository;
import com.ledgerline.repository.MoneyTransactionRepository;
import com.ledgerline.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class MoneyMovementService {

    private static final String FUNDING_ACCOUNT = "SYS-EXTERNAL-FUNDING";
    private static final String PAYOUT_ACCOUNT = "SYS-EXTERNAL-PAYOUT";

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final MoneyTransactionRepository transactionRepository;
    private final LedgerEntryRepository ledgerRepository;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public MoneyMovementService(
            AccountRepository accountRepository,
            UserRepository userRepository,
            MoneyTransactionRepository transactionRepository,
            LedgerEntryRepository ledgerRepository,
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public MoneyTransactionResponse deposit(
            UUID adminId,
            UUID customerAccountId,
            MoneyMovementRequest request
    ) {
        AppUser actor = lockActiveUser(adminId, UserRole.ADMIN);
        long amount = toPaise(request.amount());

        Account funding = getSystemAccount(FUNDING_ACCOUNT);

        return execute(
            actor,
            TransactionType.DEPOSIT,
            funding.getId(),
            customerAccountId,
            amount,
            request
        );
    }

    @Transactional
    public MoneyTransactionResponse withdraw(
            UUID customerId,
            UUID sourceAccountId,
            MoneyMovementRequest request
    ) {
        AppUser actor = lockActiveUser(customerId, UserRole.ACCOUNT_HOLDER);
        long amount = toPaise(request.amount());

        Account payout = getSystemAccount(PAYOUT_ACCOUNT);

        return execute(
            actor,
            TransactionType.WITHDRAWAL,
            sourceAccountId,
            payout.getId(),
            amount,
            request
        );
    }

    @Transactional
    public MoneyTransactionResponse transfer(
            UUID customerId,
            UUID sourceAccountId,
            MoneyMovementRequest request
    ) {
        if (request.destinationAccountId() == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Destination account is required"
            );
        }

        AppUser actor = lockActiveUser(customerId, UserRole.ACCOUNT_HOLDER);
        long amount = toPaise(request.amount());

        return execute(
            actor,
            TransactionType.TRANSFER,
            sourceAccountId,
            request.destinationAccountId(),
            amount,
            request
        );
    }

    @Transactional(readOnly = true)
    public List<MoneyTransactionResponse> history(UUID customerId) {
        return transactionRepository.findCustomerHistory(customerId)
            .stream()
            .map(MoneyTransactionResponse::from)
            .toList();
    }

    private MoneyTransactionResponse execute(
            AppUser actor,
            TransactionType type,
            UUID sourceId,
            UUID destinationId,
            long amount,
            MoneyMovementRequest request
    ) {
        String key = request.idempotencyKey().trim();

        if (key.isEmpty() || key.length() > 128) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid idempotency key"
            );
        }

        if (sourceId.equals(destinationId)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Source and destination accounts must differ"
            );
        }

        var previous = transactionRepository
            .findByInitiatedBy_IdAndIdempotencyKey(actor.getId(), key);

        if (previous.isPresent()) {
            MoneyTransaction old = previous.get();

            boolean sameRequest =
                old.getTransactionType() == type
                && old.getAmountPaise() == amount
                && old.getSourceAccount().getId().equals(sourceId)
                && old.getDestinationAccount().getId().equals(destinationId)
                && java.util.Objects.equals(
                    old.getDescription(),
                    normalizeDescription(request.description())
                );

            if (!sameRequest) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Idempotency key was already used for a different request"
                );
            }

            return MoneyTransactionResponse.from(old);
        }

        List<UUID> ids = sourceId.compareTo(destinationId) < 0
            ? List.of(sourceId, destinationId)
            : List.of(destinationId, sourceId);

        List<Account> locked = accountRepository.findAllByIdForUpdate(ids);

        if (locked.size() != 2) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "One or both accounts were not found"
            );
        }

        Map<UUID, Account> byId = locked.stream()
            .collect(java.util.stream.Collectors.toMap(Account::getId, a -> a));

        Account source = byId.get(sourceId);
        Account destination = byId.get(destinationId);

        validateAccounts(actor, type, source, destination);

        MoneyTransaction transaction = new MoneyTransaction(
            type,
            amount,
            source,
            destination,
            actor,
            key,
            normalizeDescription(request.description())
        );

        transactionRepository.saveAndFlush(transaction);

        try {
            if (type == TransactionType.DEPOSIT) {
                destination.applyBalanceChange(amount);
            } else if (type == TransactionType.WITHDRAWAL) {
                source.applyBalanceChange(-amount);
            } else {
                source.applyBalanceChange(-amount);
                destination.applyBalanceChange(amount);
            }
        } catch (ArithmeticException | IllegalArgumentException ex) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                ex.getMessage()
            );
        }

        ledgerRepository.save(
            new LedgerEntry(transaction, source, LedgerSide.DEBIT, amount)
        );

        ledgerRepository.save(
            new LedgerEntry(transaction, destination, LedgerSide.CREDIT, amount)
        );

        transaction.markPosted();
        transactionRepository.save(transaction);

        writeAuditEvent(actor.getId(), transaction, type);

        return MoneyTransactionResponse.from(transaction);
    }

    private void validateAccounts(
            AppUser actor,
            TransactionType type,
            Account source,
            Account destination
    ) {
        if (source.getStatus() != AccountStatus.ACTIVE
                || destination.getStatus() != AccountStatus.ACTIVE) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Both accounts must be active"
            );
        }

        if (!"INR".equals(source.getCurrency())
                || !"INR".equals(destination.getCurrency())) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only INR transactions are supported"
            );
        }

        if (type == TransactionType.DEPOSIT) {
            if (source.getAccountType() != AccountType.SYSTEM
                    || destination.getAccountType() != AccountType.CUSTOMER) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid deposit account combination"
                );
            }
            return;
        }

        if (type == TransactionType.WITHDRAWAL) {
            if (source.getAccountType() != AccountType.CUSTOMER
                    || destination.getAccountType() != AccountType.SYSTEM
                    || !source.getOwner().getId().equals(actor.getId())) {
                throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "You cannot withdraw from this account"
                );
            }
            return;
        }

        if (source.getAccountType() != AccountType.CUSTOMER
                || destination.getAccountType() != AccountType.CUSTOMER
                || !source.getOwner().getId().equals(actor.getId())
                || !destination.getOwner().getId().equals(actor.getId())) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Transfers are currently limited to your own accounts"
            );
        }
    }

    private AppUser lockActiveUser(UUID userId, UserRole expectedRole) {
        AppUser user = userRepository.findByIdForUpdate(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User not found"
            ));

        if (user.getStatus() != UserStatus.ACTIVE
                || user.getRole() != expectedRole) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "User is not permitted to perform this operation"
            );
        }

        return user;
    }

    private Account getSystemAccount(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Required system account is missing"
            ));
    }

    private long toPaise(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Amount must be greater than zero"
            );
        }

        try {
            return amount.movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Amount must have at most two decimal places and fit the supported range"
            );
        }
    }

    private String normalizeDescription(String description) {
        if (description == null || description.isBlank()) {
            return null;
        }
        return description.trim();
    }

    private void writeAuditEvent(
            UUID actorId,
            MoneyTransaction transaction,
            TransactionType type
    ) {
        try {
            String details = objectMapper.writeValueAsString(Map.of(
                "transactionType", type.name(),
                "amountPaise", transaction.getAmountPaise(),
                "sourceAccountId", transaction.getSourceAccount().getId().toString(),
                "destinationAccountId", transaction.getDestinationAccount().getId().toString(),
                "status", transaction.getStatus().name()
            ));

            jdbcTemplate.update(
                """
                INSERT INTO audit_events
                    (actor_user_id, action, entity_type, entity_id, details)
                VALUES (?, ?, ?, ?, CAST(? AS jsonb))
                """,
                actorId,
                "MONEY_TRANSACTION_POSTED",
                "MONEY_TRANSACTION",
                transaction.getId(),
                details
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Could not serialize audit details", ex);
        }
    }
}
