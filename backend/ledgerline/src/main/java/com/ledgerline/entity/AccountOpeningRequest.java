
package com.ledgerline.entity;

import com.ledgerline.enums.RequestStatus;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
    name = "account_opening_requests",
    indexes = {
        @Index(
            name = "idx_account_requests_applicant",
            columnList = "applicant_user_id"
        ),
        @Index(
            name = "idx_account_requests_status",
            columnList = "status"
        )
    }
)
public class AccountOpeningRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "applicant_user_id", nullable = false)
    private AppUser applicant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status = RequestStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_user_id")
    private AppUser reviewedBy;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", unique = true)
    private Account account;

    @Column(name = "review_note", length = 500)
    private String reviewNote;

    @Column(name = "requested_at", nullable = false, updatable = false)
    private Instant requestedAt;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    protected AccountOpeningRequest() {
    }

    public AccountOpeningRequest(AppUser applicant) {
        this.applicant = applicant;
        this.status = RequestStatus.PENDING;
    }

    @PrePersist
    protected void onCreate() {
        if (requestedAt == null) {
            requestedAt = Instant.now();
        }
    }

    public UUID getId() {
        return id;
    }

    public AppUser getApplicant() {
        return applicant;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public AppUser getReviewedBy() {
        return reviewedBy;
    }

    public Account getAccount() {
        return account;
    }

    public String getReviewNote() {
        return reviewNote;
    }

    public Instant getRequestedAt() {
        return requestedAt;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void approve(AppUser reviewer, Account account, String reviewNote) {
        this.status = RequestStatus.APPROVED;
        this.reviewedBy = reviewer;
        this.account = account;
        this.reviewNote = reviewNote;
        this.reviewedAt = Instant.now();
    }

    public void reject(AppUser reviewer, String reviewNote) {
        this.status = RequestStatus.REJECTED;
        this.reviewedBy = reviewer;
        this.reviewNote = reviewNote;
        this.reviewedAt = Instant.now();
    }
}
