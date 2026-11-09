package com.ledgerline.entity;

import com.ledgerline.enums.TransactionStatus;
import com.ledgerline.enums.TransactionType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "money_transactions")
public class MoneyTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionStatus status = TransactionStatus.PENDING;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String currency = "INR";

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_account_id", nullable = false)
    private Account sourceAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "destination_account_id", nullable = false)
    private Account destinationAccount;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "initiated_by_user_id", nullable = false)
    private AppUser initiatedBy;

    @Column(name = "idempotency_key", nullable = false, length = 128)
    private String idempotencyKey;

    @Column(length = 255)
    private String description;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    protected MoneyTransaction() {}

    public MoneyTransaction(
            TransactionType transactionType,
            long amountPaise,
            Account sourceAccount,
            Account destinationAccount,
            AppUser initiatedBy,
            String idempotencyKey,
            String description
    ) {
        this.transactionType = transactionType;
        this.amountPaise = amountPaise;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.initiatedBy = initiatedBy;
        this.idempotencyKey = idempotencyKey;
        this.description = description;
        this.status = TransactionStatus.PENDING;
        this.currency = "INR";
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public void markPosted() {
        this.status = TransactionStatus.POSTED;
        this.completedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public TransactionType getTransactionType() { return transactionType; }
    public TransactionStatus getStatus() { return status; }
    public long getAmountPaise() { return amountPaise; }
    public String getCurrency() { return currency; }
    public Account getSourceAccount() { return sourceAccount; }
    public Account getDestinationAccount() { return destinationAccount; }
    public AppUser getInitiatedBy() { return initiatedBy; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getCompletedAt() { return completedAt; }
}
