# BalanceVault Manual Verification Checklist

Use a disposable local PostgreSQL database. The demo seed refuses to run when any users already exist.

## Startup

- [ ] `npm install` completes from the project root.
- [ ] `npm run prisma:generate --workspace=backend` completes.
- [ ] `npm run prisma:migrate --workspace=backend` completes.
- [ ] `npm run db:seed --workspace=backend` completes on a fresh database.
- [ ] `npm run dev:backend` starts on port 5000.
- [ ] `npm run dev:frontend` starts on port 5173.
- [ ] `/api/v1/health` returns success.
- [ ] `/api/v1/ready` reports PostgreSQL connected.

## Authentication and roles

- [ ] Admin demo login works.
- [ ] Account Holder demo login works.
- [ ] Auditor demo login works.
- [ ] Invalid password is rejected.
- [ ] Public registration creates only an Account Holder.
- [ ] Public registration with `role: ADMIN` is rejected.
- [ ] Public registration with `role: AUDITOR` is rejected.
- [ ] Auditor cannot create accounts or perform financial mutations.

## Account onboarding

- [ ] New Account Holder submits onboarding.
- [ ] Admin sees the pending onboarding request.
- [ ] Admin approves the request.
- [ ] Approved user can create a Checking or Savings account.
- [ ] Account Holder cannot view another user's account.

## Financial operations

- [ ] Deposit creates one POSTED transaction and exactly two equal ledger entries.
- [ ] Withdrawal with sufficient funds succeeds.
- [ ] Withdrawal exceeding available funds is rejected without changing balances.
- [ ] Transfer between two active same-currency customer accounts succeeds.
- [ ] Transfer to the same account is rejected.
- [ ] Transfer to a system settlement account is rejected.
- [ ] Reusing an idempotency key with the same request does not duplicate the transaction.
- [ ] Reusing an idempotency key with a different payload is rejected.
- [ ] Transaction history is scoped to Account Holder-owned accounts.

## Auditor and Admin

- [ ] Auditor sees the read-only audit workspace.
- [ ] Auditor can view recent audit events.
- [ ] Auditor cannot access Admin onboarding review actions.
- [ ] Admin can review onboarding requests and manage account status.

## Automated checks

- [ ] `npm test` passes against the seeded disposable database.
- [ ] `npm run build` completes for both backend and frontend.

Record failures with the exact command, HTTP status and response body. Do not use a production or personal database for demo tests.
