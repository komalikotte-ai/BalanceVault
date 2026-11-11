-- CreateEnum
CREATE TYPE "user_role" AS ENUM ('ADMIN', 'ACCOUNT_HOLDER', 'AUDITOR');

-- CreateEnum
CREATE TYPE "user_status" AS ENUM ('ACTIVE', 'PENDING', 'DISABLED');

-- CreateEnum
CREATE TYPE "account_type" AS ENUM ('CUSTOMER', 'SYSTEM');

-- CreateEnum
CREATE TYPE "account_status" AS ENUM ('ACTIVE', 'FROZEN', 'CLOSED');

-- CreateEnum
CREATE TYPE "request_status" AS ENUM ('PENDING', 'APPROVED', 'REJECTED');

-- CreateEnum
CREATE TYPE "transaction_type" AS ENUM ('DEPOSIT', 'WITHDRAWAL', 'TRANSFER');

-- CreateEnum
CREATE TYPE "transaction_status" AS ENUM ('PENDING', 'POSTED', 'FAILED');

-- CreateEnum
CREATE TYPE "ledger_side" AS ENUM ('DEBIT', 'CREDIT');

-- CreateTable
CREATE TABLE "app_users" (
    "id" UUID NOT NULL,
    "full_name" VARCHAR(120) NOT NULL,
    "email" VARCHAR(255) NOT NULL,
    "password_hash" VARCHAR(255) NOT NULL,
    "role" "user_role" NOT NULL,
    "status" "user_status" NOT NULL DEFAULT 'ACTIVE',
    "created_at" TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updated_at" TIMESTAMPTZ(6) NOT NULL,

    CONSTRAINT "app_users_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "accounts" (
    "id" UUID NOT NULL,
    "account_number" VARCHAR(32) NOT NULL,
    "owner_user_id" UUID,
    "account_type" "account_type" NOT NULL,
    "currency" CHAR(3) NOT NULL DEFAULT 'INR',
    "balance_paise" BIGINT,
    "status" "account_status" NOT NULL DEFAULT 'ACTIVE',
    "created_at" TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "accounts_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "account_opening_requests" (
    "id" UUID NOT NULL,
    "applicant_user_id" UUID NOT NULL,
    "status" "request_status" NOT NULL DEFAULT 'PENDING',
    "reviewer_user_id" UUID,
    "account_id" UUID,
    "review_note" VARCHAR(500),
    "requested_at" TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "reviewed_at" TIMESTAMPTZ(6),

    CONSTRAINT "account_opening_requests_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "money_transactions" (
    "id" UUID NOT NULL,
    "transaction_type" "transaction_type" NOT NULL,
    "status" "transaction_status" NOT NULL DEFAULT 'PENDING',
    "amount_paise" BIGINT NOT NULL,
    "currency" CHAR(3) NOT NULL DEFAULT 'INR',
    "source_account_id" UUID NOT NULL,
    "destination_account_id" UUID NOT NULL,
    "initiated_by_user_id" UUID NOT NULL,
    "idempotency_key" VARCHAR(128) NOT NULL,
    "description" VARCHAR(255),
    "created_at" TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "completed_at" TIMESTAMPTZ(6),

    CONSTRAINT "money_transactions_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "ledger_entries" (
    "id" UUID NOT NULL,
    "transaction_id" UUID NOT NULL,
    "account_id" UUID NOT NULL,
    "entry_side" "ledger_side" NOT NULL,
    "amount_paise" BIGINT NOT NULL,
    "currency" CHAR(3) NOT NULL DEFAULT 'INR',
    "created_at" TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "ledger_entries_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "audit_events" (
    "id" UUID NOT NULL,
    "actor_user_id" UUID,
    "action" VARCHAR(100) NOT NULL,
    "entity_type" VARCHAR(100) NOT NULL,
    "entity_id" UUID,
    "details" JSONB,
    "ip_address" INET,
    "created_at" TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "audit_events_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "auditor_account_access" (
    "id" UUID NOT NULL,
    "auditor_user_id" UUID NOT NULL,
    "account_id" UUID NOT NULL,
    "granted_by_user_id" UUID NOT NULL,
    "granted_at" TIMESTAMPTZ(6) NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT "auditor_account_access_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "app_users_email_key" ON "app_users"("email");

-- CreateIndex
CREATE INDEX "app_users_role_status_idx" ON "app_users"("role", "status");

-- CreateIndex
CREATE UNIQUE INDEX "accounts_account_number_key" ON "accounts"("account_number");

-- CreateIndex
CREATE INDEX "accounts_owner_user_id_status_idx" ON "accounts"("owner_user_id", "status");

-- CreateIndex
CREATE INDEX "accounts_account_type_status_idx" ON "accounts"("account_type", "status");

-- CreateIndex
CREATE UNIQUE INDEX "account_opening_requests_account_id_key" ON "account_opening_requests"("account_id");

-- CreateIndex
CREATE INDEX "account_opening_requests_applicant_user_id_status_idx" ON "account_opening_requests"("applicant_user_id", "status");

-- CreateIndex
CREATE INDEX "account_opening_requests_status_requested_at_idx" ON "account_opening_requests"("status", "requested_at");

-- CreateIndex
CREATE INDEX "money_transactions_source_account_id_created_at_idx" ON "money_transactions"("source_account_id", "created_at");

-- CreateIndex
CREATE INDEX "money_transactions_destination_account_id_created_at_idx" ON "money_transactions"("destination_account_id", "created_at");

-- CreateIndex
CREATE INDEX "money_transactions_status_created_at_idx" ON "money_transactions"("status", "created_at");

-- CreateIndex
CREATE UNIQUE INDEX "money_transactions_initiated_by_user_id_idempotency_key_key" ON "money_transactions"("initiated_by_user_id", "idempotency_key");

-- CreateIndex
CREATE INDEX "ledger_entries_account_id_created_at_idx" ON "ledger_entries"("account_id", "created_at");

-- CreateIndex
CREATE INDEX "ledger_entries_transaction_id_idx" ON "ledger_entries"("transaction_id");

-- CreateIndex
CREATE UNIQUE INDEX "ledger_entries_transaction_id_account_id_entry_side_key" ON "ledger_entries"("transaction_id", "account_id", "entry_side");

-- CreateIndex
CREATE INDEX "audit_events_actor_user_id_created_at_idx" ON "audit_events"("actor_user_id", "created_at");

-- CreateIndex
CREATE INDEX "audit_events_entity_type_entity_id_idx" ON "audit_events"("entity_type", "entity_id");

-- CreateIndex
CREATE INDEX "audit_events_created_at_idx" ON "audit_events"("created_at");

-- CreateIndex
CREATE INDEX "auditor_account_access_account_id_idx" ON "auditor_account_access"("account_id");

-- CreateIndex
CREATE UNIQUE INDEX "auditor_account_access_auditor_user_id_account_id_key" ON "auditor_account_access"("auditor_user_id", "account_id");

-- AddForeignKey
ALTER TABLE "accounts" ADD CONSTRAINT "accounts_owner_user_id_fkey" FOREIGN KEY ("owner_user_id") REFERENCES "app_users"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "account_opening_requests" ADD CONSTRAINT "account_opening_requests_applicant_user_id_fkey" FOREIGN KEY ("applicant_user_id") REFERENCES "app_users"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "account_opening_requests" ADD CONSTRAINT "account_opening_requests_reviewer_user_id_fkey" FOREIGN KEY ("reviewer_user_id") REFERENCES "app_users"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "account_opening_requests" ADD CONSTRAINT "account_opening_requests_account_id_fkey" FOREIGN KEY ("account_id") REFERENCES "accounts"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "money_transactions" ADD CONSTRAINT "money_transactions_source_account_id_fkey" FOREIGN KEY ("source_account_id") REFERENCES "accounts"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "money_transactions" ADD CONSTRAINT "money_transactions_destination_account_id_fkey" FOREIGN KEY ("destination_account_id") REFERENCES "accounts"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "money_transactions" ADD CONSTRAINT "money_transactions_initiated_by_user_id_fkey" FOREIGN KEY ("initiated_by_user_id") REFERENCES "app_users"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ledger_entries" ADD CONSTRAINT "ledger_entries_transaction_id_fkey" FOREIGN KEY ("transaction_id") REFERENCES "money_transactions"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "ledger_entries" ADD CONSTRAINT "ledger_entries_account_id_fkey" FOREIGN KEY ("account_id") REFERENCES "accounts"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "audit_events" ADD CONSTRAINT "audit_events_actor_user_id_fkey" FOREIGN KEY ("actor_user_id") REFERENCES "app_users"("id") ON DELETE SET NULL ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auditor_account_access" ADD CONSTRAINT "auditor_account_access_auditor_user_id_fkey" FOREIGN KEY ("auditor_user_id") REFERENCES "app_users"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auditor_account_access" ADD CONSTRAINT "auditor_account_access_account_id_fkey" FOREIGN KEY ("account_id") REFERENCES "accounts"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "auditor_account_access" ADD CONSTRAINT "auditor_account_access_granted_by_user_id_fkey" FOREIGN KEY ("granted_by_user_id") REFERENCES "app_users"("id") ON DELETE RESTRICT ON UPDATE CASCADE;
