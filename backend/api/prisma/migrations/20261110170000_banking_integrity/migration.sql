
-- ============================================================
-- LEDGERLINE / BALANCEVAULT
-- Migration: Banking integrity and financial data protection
-- ============================================================

-- 1. Account balance and ownership constraints

ALTER TABLE "accounts"
ADD CONSTRAINT "accounts_balance_nonnegative_check"
CHECK ("balance_paise" IS NULL OR "balance_paise" >= 0);

ALTER TABLE "accounts"
ADD CONSTRAINT "accounts_valid_ownership_check"
CHECK (
    (
        "account_type" = 'CUSTOMER'
        AND "owner_user_id" IS NOT NULL
        AND "balance_paise" IS NOT NULL
    )
    OR
    (
        "account_type" = 'SYSTEM'
        AND "owner_user_id" IS NULL
        AND "balance_paise" IS NULL
    )
);

-- 2. Financial transaction constraints

ALTER TABLE "money_transactions"
ADD CONSTRAINT "money_transactions_positive_amount_check"
CHECK ("amount_paise" > 0);

ALTER TABLE "money_transactions"
ADD CONSTRAINT "money_transactions_distinct_accounts_check"
CHECK ("source_account_id" <> "destination_account_id");

ALTER TABLE "ledger_entries"
ADD CONSTRAINT "ledger_entries_positive_amount_check"
CHECK ("amount_paise" > 0);

-- 3. INR-only currency constraints

ALTER TABLE "accounts"
ADD CONSTRAINT "accounts_currency_supported_check"
CHECK ("currency" = 'INR');

ALTER TABLE "money_transactions"
ADD CONSTRAINT "money_transactions_currency_supported_check"
CHECK ("currency" = 'INR');

ALTER TABLE "ledger_entries"
ADD CONSTRAINT "ledger_entries_currency_supported_check"
CHECK ("currency" = 'INR');

-- 4. Only one pending account-opening request per applicant

CREATE UNIQUE INDEX "uq_one_pending_account_request_per_applicant"
ON "account_opening_requests" ("applicant_user_id")
WHERE "status" = 'PENDING';

-- 5. Account-opening request state consistency

ALTER TABLE "account_opening_requests"
ADD CONSTRAINT "account_opening_requests_state_check"
CHECK (
    (
        "status" = 'PENDING'
        AND "reviewer_user_id" IS NULL
        AND "account_id" IS NULL
        AND "reviewed_at" IS NULL
    )
    OR
    (
        "status" = 'APPROVED'
        AND "reviewer_user_id" IS NOT NULL
        AND "account_id" IS NOT NULL
        AND "reviewed_at" IS NOT NULL
    )
    OR
    (
        "status" = 'REJECTED'
        AND "reviewer_user_id" IS NOT NULL
        AND "account_id" IS NULL
        AND "reviewed_at" IS NOT NULL
    )
);

-- 6. Transaction completion state consistency

ALTER TABLE "money_transactions"
ADD CONSTRAINT "money_transactions_completion_check"
CHECK (
    (
        "status" = 'POSTED'
        AND "completed_at" IS NOT NULL
    )
    OR
    (
        "status" IN ('PENDING', 'FAILED')
        AND "completed_at" IS NULL
    )
);

-- 7. Audit events are append-only

CREATE OR REPLACE FUNCTION prevent_audit_event_mutation()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Audit events are append-only';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "audit_events_no_update"
BEFORE UPDATE OR DELETE ON "audit_events"
FOR EACH ROW
EXECUTE FUNCTION prevent_audit_event_mutation();

-- 8. Validate a posted transaction's double-entry ledger

CREATE OR REPLACE FUNCTION assert_posted_transaction_ledger(
    p_tx_id UUID
)
RETURNS VOID AS $$
DECLARE
    tx_status transaction_status;
    expected_source UUID;
    expected_destination UUID;
    expected_amount BIGINT;
    expected_currency CHAR(3);
    source_currency CHAR(3);
    destination_currency CHAR(3);

    entry_count INTEGER;
    debit_count INTEGER;
    credit_count INTEGER;
    total_amount NUMERIC;
    invalid_entries INTEGER;
BEGIN
    SELECT
        mt.status,
        mt.source_account_id,
        mt.destination_account_id,
        mt.amount_paise,
        mt.currency,
        src.currency,
        dst.currency
    INTO
        tx_status,
        expected_source,
        expected_destination,
        expected_amount,
        expected_currency,
        source_currency,
        destination_currency
    FROM money_transactions mt
    JOIN accounts src
        ON src.id = mt.source_account_id
    JOIN accounts dst
        ON dst.id = mt.destination_account_id
    WHERE mt.id = p_tx_id;

    IF NOT FOUND OR tx_status <> 'POSTED' THEN
        RETURN;
    END IF;

    SELECT
        COUNT(*),
        COUNT(*) FILTER (
            WHERE entry_side = 'DEBIT'
        ),
        COUNT(*) FILTER (
            WHERE entry_side = 'CREDIT'
        ),
        COALESCE(SUM(amount_paise), 0),
        COUNT(*) FILTER (
            WHERE
                (
                    entry_side = 'DEBIT'
                    AND account_id <> expected_source
                )
                OR
                (
                    entry_side = 'CREDIT'
                    AND account_id <> expected_destination
                )
                OR amount_paise <> expected_amount
                OR currency <> expected_currency
        )
    INTO
        entry_count,
        debit_count,
        credit_count,
        total_amount,
        invalid_entries
    FROM ledger_entries
    WHERE transaction_id = p_tx_id;

    IF expected_currency <> source_currency
       OR expected_currency <> destination_currency
       OR entry_count <> 2
       OR debit_count <> 1
       OR credit_count <> 1
       OR invalid_entries <> 0
       OR total_amount <> (expected_amount::NUMERIC * 2)
    THEN
        RAISE EXCEPTION
            'Posted transaction % has invalid or unbalanced ledger entries',
            p_tx_id;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- 9. Deferred trigger function for ledger validation

CREATE OR REPLACE FUNCTION validate_posted_transaction_ledger()
RETURNS TRIGGER AS $$
BEGIN
    IF TG_TABLE_NAME = 'money_transactions' THEN

        PERFORM assert_posted_transaction_ledger(NEW.id);

    ELSIF TG_OP = 'INSERT' THEN

        PERFORM assert_posted_transaction_ledger(NEW.transaction_id);

    ELSIF TG_OP = 'DELETE' THEN

        PERFORM assert_posted_transaction_ledger(OLD.transaction_id);

    ELSE

        PERFORM assert_posted_transaction_ledger(OLD.transaction_id);

        IF NEW.transaction_id IS DISTINCT FROM OLD.transaction_id THEN
            PERFORM assert_posted_transaction_ledger(NEW.transaction_id);
        END IF;

    END IF;

    RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 10. Validate transaction integrity at transaction commit

CREATE CONSTRAINT TRIGGER "money_transactions_ledger_integrity"
AFTER INSERT OR UPDATE ON "money_transactions"
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION validate_posted_transaction_ledger();

CREATE CONSTRAINT TRIGGER "ledger_entries_transaction_integrity"
AFTER INSERT OR UPDATE OR DELETE ON "ledger_entries"
DEFERRABLE INITIALLY DEFERRED
FOR EACH ROW
EXECUTE FUNCTION validate_posted_transaction_ledger();

-- 11. Prevent changes to ledger entries belonging to posted transactions

CREATE OR REPLACE FUNCTION prevent_posted_ledger_mutation()
RETURNS TRIGGER AS $$
DECLARE
    current_status transaction_status;
BEGIN
    IF TG_OP = 'INSERT' THEN

        SELECT status
        INTO current_status
        FROM money_transactions
        WHERE id = NEW.transaction_id;

        IF current_status = 'POSTED' THEN
            RAISE EXCEPTION
                'Cannot insert ledger entries for posted transactions';
        END IF;

        RETURN NEW;

    ELSIF TG_OP = 'DELETE' THEN

        SELECT status
        INTO current_status
        FROM money_transactions
        WHERE id = OLD.transaction_id;

        IF current_status = 'POSTED' THEN
            RAISE EXCEPTION
                'Ledger entries for posted transactions are immutable';
        END IF;

        RETURN OLD;

    ELSE

        SELECT status
        INTO current_status
        FROM money_transactions
        WHERE id = OLD.transaction_id;

        IF current_status = 'POSTED' THEN
            RAISE EXCEPTION
                'Ledger entries for posted transactions are immutable';
        END IF;

        IF NEW.transaction_id IS DISTINCT FROM OLD.transaction_id THEN

            SELECT status
            INTO current_status
            FROM money_transactions
            WHERE id = NEW.transaction_id;

            IF current_status = 'POSTED' THEN
                RAISE EXCEPTION
                    'Cannot move ledger entries into posted transactions';
            END IF;

        END IF;

        RETURN NEW;

    END IF;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "ledger_entries_immutable_after_posting"
BEFORE INSERT OR UPDATE OR DELETE ON "ledger_entries"
FOR EACH ROW
EXECUTE FUNCTION prevent_posted_ledger_mutation();

-- 12. Prevent changes to posted transactions

CREATE OR REPLACE FUNCTION prevent_posted_transaction_mutation()
RETURNS TRIGGER AS $$
BEGIN
    IF OLD.status = 'POSTED' THEN
        RAISE EXCEPTION
            'Posted transactions are immutable';
    END IF;

    IF TG_OP = 'DELETE' THEN
        RETURN OLD;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER "money_transactions_immutable_after_posting"
BEFORE UPDATE OR DELETE ON "money_transactions"
FOR EACH ROW
EXECUTE FUNCTION prevent_posted_transaction_mutation();

-- ============================================================
-- END OF BANKING INTEGRITY MIGRATION
-- ============================================================
