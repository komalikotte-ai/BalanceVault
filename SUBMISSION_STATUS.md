# BalanceVault — Submission Candidate Status

This document records what is present in this ZIP and what has actually been verified in the packaging environment.

## Included application areas

- Authentication: registration, login, refresh-token cookie, logout and current-user endpoint.
- Role model: Admin, Account Holder and Auditor.
- Onboarding: user submission and Admin review.
- Accounts: account creation, account listing/details/balance and Admin account status updates.
- Financial operations: deposits, withdrawals and transfers; transaction service uses Prisma transactions, deterministic row locks, idempotency records and paired ledger entries.
- Transaction history: account-holder queries are scoped to accounts owned by the authenticated user; Admin and Auditor can inspect platform activity.
- Audit trail: recorded security, onboarding and transaction events; new Auditor page reads recent audit events.
- Frontend: login/register, dashboard, onboarding, Admin KYC queue, accounts, transaction history and Auditor audit workspace.

## Changes made while preparing this candidate

1. Added a dedicated Auditor dashboard page and `/api/v1/auditor/audit-logs` read-only endpoint.
2. Added navigation to the Auditor workspace for Auditor and Admin roles.
3. Corrected transaction balance calculations to re-read account balances after acquiring row locks for deposits, withdrawals and transfers. This prevents stale pre-lock balance snapshots from overwriting concurrent updates.
4. Added status/currency checks after account locks and prevented transfers to/from system settlement accounts.
5. Restricted public registration to Account Holder. Auditor and Admin privileges must not be self-selected by an anonymous registrant.
6. Restricted customer account creation to the Account Holder role.
7. Removed destructive `deleteMany` calls from the demo seed. The seed now refuses to run if the database already contains users.
8. Corrected the seeded system settlement opening balance to match the seeded ledger deposits.
9. Replaced outdated setup instructions and documented limitations in README.

## Verification report

| Check | Result |
|---|---|
| ZIP extraction and source inventory | Passed |
| TypeScript/TSX parser syntax scan (68 files) | Passed; no syntax parse errors |
| Package JSON parsing | Passed for package manifests |
| Full backend TypeScript build | Not run successfully: npm dependencies could not be installed in the packaging environment because npm registry access timed out |
| Frontend production build | Not run; dependencies unavailable |
| Automated Vitest suite | Not run; dependencies and a configured PostgreSQL test database are required |
| PostgreSQL migration and runtime API tests | Not run in this environment |
| End-to-end browser verification | Not run |

**This ZIP is a submission candidate, not a certified or production-ready banking system.** Run the install, build, database and test steps in README on your Windows machine. Do not submit until the local build and critical API tests pass.

## Known scope limitations

- Auditor access is read-only and platform-wide. Per-account grant/revoke workflows are not implemented.
- Audit event list is limited to the latest 250 records; advanced filtering and export are not implemented.
- Demo currency is USD, using decimal precision of four places. Currency conversion and INR support are not implemented.
- The UI and API are an educational demonstration. Independent security, compliance and production resilience audits have not been performed.
