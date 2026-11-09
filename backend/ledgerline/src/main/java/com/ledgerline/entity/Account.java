package com.ledgerline.entity;

import com.ledgerline.enums.AccountStatus;
import com.ledgerline.enums.AccountType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "accounts",
    indexes = {
        @Index(name = "idx_accounts_owner", columnList = "owner_user_id"),
        @Index(name = "idx_accounts_status", columnList = "status")
    }
)
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_number", nullable = false, unique = true, length = 30)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id")
    private AppUser owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String currency = "INR";

    @Column(name = "balance_paise")
    private Long balancePaise;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AccountStatus status = AccountStatus.ACTIVE;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Account() {}

    public Account(
            String accountNumber,
            AppUser owner,
            AccountType accountType,
            Long balancePaise
    ) {
        this.accountNumber = accountNumber;
        this.owner = owner;
        this.accountType = accountType;
        this.balancePaise = balancePaise;
        this.currency = "INR";
        this.status = AccountStatus.ACTIVE;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void applyBalanceChange(long delta) {
        if (accountType != AccountType.CUSTOMER || balancePaise == null) {
            throw new IllegalStateException("Only customer accounts have mutable balances");
        }

        long updatedBalance = Math.addExact(balancePaise, delta);

        if (updatedBalance < 0) {
            throw new IllegalArgumentException("Insufficient account balance");
        }

        balancePaise = updatedBalance;
    }

    public UUID getId() { return id; }
    public String getAccountNumber() { return accountNumber; }
    public AppUser getOwner() { return owner; }
    public AccountType getAccountType() { return accountType; }
    public String getCurrency() { return currency; }
    public Long getBalancePaise() { return balancePaise; }
    public AccountStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
