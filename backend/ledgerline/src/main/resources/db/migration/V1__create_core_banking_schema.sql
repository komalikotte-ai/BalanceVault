
-- LedgerLine V1: Core banking schema
-- Monetary values are stored as integer paise.
-- PostgreSQL is the source of truth for schema changes.

-- 1. Application users and roles
CREATE TABLE app_users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    full_name VARCHAR(120) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL
        CHECK (role IN ('ADMIN', 'ACCOUNT_HOLDER', 'AUDITOR')),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'PENDING', 'DISABLED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_app_users_email_lowercase CHECK (email = LOWER(email))
);

-- 2. Customer and system accounts
CREATE TABLE accounts (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    account_number VARCHAR(30) NOT NULL UNIQUE,
    owner_user_id UUID REFERENCES app_users(id) ON DELETE RESTRICT,
    account_type VARCHAR(20) NOT NULL
        CHECK (account_type IN ('CUSTOMER', 'SYSTEM')),
    currency CHAR(3) NOT NULL DEFAULT 'INR'
        CHECK (currency = 'INR'),
    balance_paise BIGINT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'
        CHECK (status IN ('ACTIVE', 'FROZEN', 'CLOSED')),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT ck_account_owner_and_balance CHECK (
        (account_type = 'CUSTOMER'
            AND owner_user_id IS NOT NULL
            AND balance_paise IS NOT NULL
            AND balance_paise >= 0)
        OR
        (account_type = 'SYSTEM'
            AND owner_user_id IS NULL
            AND balance_paise IS NULL)
    )
);

CREATE INDEX idx_accounts_owner ON accounts(owner_user_id);
CREATE INDEX idx_accounts_status ON accounts(status);

-- 3. Account-opening requests
CREATE TABLE account_opening_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    applicant_user_id UUID NOT NULL
        REFERENCES app_users(id) ON DELETE RESTRICT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    reviewed_by_user_id UUID
        REFERENCES app_users(id) ON DELETE RESTRICT,
    account_id UUID UNIQUE
        REFERENCES accounts(id) ON DELETE RESTRICT,
    review_note VARCHAR(500),
    requested_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed_at TIMESTAMPTZ,

    CONSTRAINT ck_account_request_review CHECK (
        (status = 'PENDING'
            AND reviewed_by_user_id IS NULL
            AND account_id IS NULL
            AND reviewed_at IS NULL)
        OR
        (status = 'APPROVED'
            AND reviewed_by_user_id IS NOT NULL
            AND account_id IS NOT NULL
            AND reviewed_at IS NOT NULL)
        OR
        (status = 'REJECTED'
            AND reviewed_by_user_id IS NOT NULL
            AND account_id IS NULL
            AND reviewed_at IS NOT NULL)
    )
);

CREATE INDEX idx_account_requests_applicant
    ON account_opening_requests(applicant_user_id);

CREATE INDEX idx_account_requests_status
    ON account_opening_requests(status);

-- 4. Money transactions
CREATE TABLE money_transactions (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_type VARCHAR(20) NOT NULL
        CHECK (transaction_type IN ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER')),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
        CHECK (status IN ('PENDING', 'POSTED', 'FAILED')),
    amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
    currency CHAR(3) NOT NULL DEFAULT 'INR'
        CHECK (currency = 'INR'),
    source_account_id UUID NOT NULL
        REFERENCES accounts(id) ON DELETE RESTRICT,
    destination_account_id UUID NOT NULL
        REFERENCES accounts(id) ON DELETE RESTRICT,
    initiated_by_user_id UUID NOT NULL
        REFERENCES app_users(id) ON DELETE RESTRICT,
    idempotency_key VARCHAR(128) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMPTZ,

    CONSTRAINT uq_transaction_idempotency
        UNIQUE (initiated_by_user_id, idempotency_key),

    CONSTRAINT ck_transaction_different_accounts
        CHECK (source_account_id <> destination_account_id),

    CONSTRAINT ck_transaction_completion CHECK (
        (status = 'POSTED' AND completed_at IS NOT NULL)
        OR
        (status IN ('PENDING', 'FAILED') AND completed_at IS NULL)
    )
);

CREATE INDEX idx_transactions_source
    ON money_transactions(source_account_id, created_at DESC);

CREATE INDEX idx_transactions_destination
    ON money_transactions(destination_account_id, created_at DESC);

CREATE INDEX idx_transactions_initiator
    ON money_transactions(initiated_by_user_id, created_at DESC);

-- 5. Double-entry ledger
CREATE TABLE ledger_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id UUID NOT NULL
        REFERENCES money_transactions(id) ON DELETE RESTRICT,
    account_id UUID NOT NULL
        REFERENCES accounts(id) ON DELETE RESTRICT,
    entry_side VARCHAR(10) NOT NULL
        CHECK (entry_side IN ('DEBIT', 'CREDIT')),
    amount_paise BIGINT NOT NULL CHECK (amount_paise > 0),
    currency CHAR(3) NOT NULL DEFAULT 'INR'
        CHECK (currency = 'INR'),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_ledger_transaction_account_side
        UNIQUE (transaction_id, account_id, entry_side)
);

CREATE INDEX idx_ledger_account
    ON ledger_entries(account_id, created_at DESC);

CREATE INDEX idx_ledger_transaction
    ON ledger_entries(transaction_id);

-- 6. Append-only audit events
CREATE TABLE audit_events (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id UUID
        REFERENCES app_users(id) ON DELETE SET NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(80) NOT NULL,
    entity_id UUID,
    details JSONB NOT NULL DEFAULT '{}'::JSONB,
    ip_address INET,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_actor
    ON audit_events(actor_user_id, created_at DESC);

CREATE INDEX idx_audit_entity
    ON audit_events(entity_type, entity_id);

CREATE INDEX idx_audit_created
    ON audit_events(created_at DESC);

-- Block ordinary UPDATE and DELETE operations on audit events.
CREATE FUNCTION prevent_audit_event_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit events cannot be updated or deleted';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_audit_events_append_only
BEFORE UPDATE OR DELETE ON audit_events
FOR EACH ROW
EXECUTE FUNCTION prevent_audit_event_mutation();

-- 7. Explicit auditor-to-account access assignments
CREATE TABLE auditor_account_access (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    auditor_user_id UUID NOT NULL
        REFERENCES app_users(id) ON DELETE RESTRICT,
    account_id UUID NOT NULL
        REFERENCES accounts(id) ON DELETE RESTRICT,
    granted_by_user_id UUID NOT NULL
        REFERENCES app_users(id) ON DELETE RESTRICT,
    granted_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_auditor_account
        UNIQUE (auditor_user_id, account_id)
);

CREATE INDEX idx_auditor_access_account
    ON auditor_account_access(account_id);

-- 8. System accounts used as external transaction counterparties.
-- System account balances are intentionally NULL.
INSERT INTO accounts (
    account_number,
    owner_user_id,
    account_type,
    currency,
    balance_paise,
    status
)
VALUES
    ('SYS-EXTERNAL-FUNDING', NULL, 'SYSTEM', 'INR', NULL, 'ACTIVE'),
    ('SYS-EXTERNAL-PAYOUT', NULL, 'SYSTEM', 'INR', NULL, 'ACTIVE');
