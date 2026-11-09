package com.ledgerline.entity;

import com.ledgerline.enums.LedgerSide;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private MoneyTransaction transaction;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_side", nullable = false, length = 10)
    private LedgerSide entrySide;

    @Column(name = "amount_paise", nullable = false)
    private long amountPaise;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(nullable = false, columnDefinition = "CHAR(3)")
    private String currency = "INR";

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected LedgerEntry() {}

    public LedgerEntry(
            MoneyTransaction transaction,
            Account account,
            LedgerSide entrySide,
            long amountPaise
    ) {
        this.transaction = transaction;
        this.account = account;
        this.entrySide = entrySide;
        this.amountPaise = amountPaise;
        this.currency = "INR";
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public UUID getId() { return id; }
    public MoneyTransaction getTransaction() { return transaction; }
    public Account getAccount() { return account; }
    public LedgerSide getEntrySide() { return entrySide; }
    public long getAmountPaise() { return amountPaise; }
    public String getCurrency() { return currency; }
    public Instant getCreatedAt() { return createdAt; }
}
