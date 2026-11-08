# LedgerLine

**Money movement with a ledger you can prove.**

LedgerLine is a financial transaction management system being developed to support secure money movement, prevent duplicate transactions, enforce account-level authorization, and maintain a traceable financial ledger.

## Project Status

**Status:** Under active development  
**Challenge:** Snovasys Fresher Hiring Challenge 2026

The initial PostgreSQL database schema has been implemented and successfully applied using Flyway. Authentication, transaction processing, ledger posting, frontend integration, and deployment remain under development.

## Technology Stack

| Component | Technology |
|---|---|
| Frontend | React, Vite, JavaScript |
| Backend | Java 21, Spring Boot 4 |
| Backend security foundation | Spring Security |
| Database | PostgreSQL 18 |
| Database migrations | Flyway |
| Persistence | Spring Data JPA |
| Validation | Jakarta Bean Validation |
| Testing | JUnit 5, Spring Boot Test |
| Build tool | Maven |

JWT authentication is planned but not yet implemented.

## Planned Features

1. Administrator, account-holder, and auditor authentication.
2. Server-side role-based authorization.
3. Account creation and management.
4. Deposits, withdrawals, and transfers.
5. Double-entry ledger and money-conservation checks.
6. Idempotent transaction processing.
7. Concurrency protection and atomic database operations.
8. Append-only audit trail and transaction statements.
9. Account ownership and auditor access controls.
10. Automated security, transaction, and concurrency tests.
11. Role-based React dashboards and API integration.
12. Deployment, API documentation, seed accounts, and demo video.

## Implemented So Far

- React and Vite frontend foundation.
- Spring Boot backend foundation.
- PostgreSQL database configuration.
- Flyway migration for the core banking schema.
- Database tables for users, accounts, account-opening requests, transactions, ledger entries, audit events, and auditor access.
- Database constraints for positive transaction amounts and non-negative customer account balances.
- Unique idempotency-key constraint per initiating user.
- Database trigger preventing ordinary updates and deletes of audit events.
- Initial external funding and payout system accounts.

These database features do not mean the corresponding application workflows are complete. Application-level transaction processing, authorization, and ledger balancing are still under development.

## Database Design

Monetary values are stored as integer paise to avoid floating-point rounding errors.

Example:

- ₹100.00 = 10000 paise
- ₹250.50 = 25050 paise

PostgreSQL schema changes are managed through Flyway migrations.

## Project Structure

```text
LedgerLine/
├── backend/
│   └── ledgerline/
├── frontend/
├── docs/
├── screenshots/
├── README.md
├── PROMPTS.md
├── THREATS.md
└── .gitignore
```

## Local Development

### Prerequisites

- Java 21
- Maven (Maven Wrapper is included)
- Node.js and npm
- PostgreSQL

### Backend

From the repository root:

```cmd
cd backend\ledgerline
```

Set the database password in your CMD session:

```cmd
set "DB_PASSWORD=YOUR_DATABASE_PASSWORD"
```

Start the backend:

```cmd
mvnw.cmd spring-boot:run
```

The backend uses port `8081`.

### Frontend

From the repository root, open another CMD window:

```cmd
cd frontend
npm install
npm run dev
```

The Vite development server normally uses port `5173`.

### Backend Tests

```cmd
cd backend\ledgerline
mvnw.cmd test
```

Use `mvnw.cmd clean test` when the Windows build directory is not locked by another process.

## Security

Security controls are being implemented incrementally. See [THREATS.md](THREATS.md) for identified threats, current protections, and remaining work.

## AI-Assisted Development

AI-assisted development is documented transparently in [PROMPTS.md](PROMPTS.md), including prompts, generated output, corrections, verification, and related commits.

## Project Repository

https://github.com/komalikotte-ai/ledgerline

## Disclaimer

LedgerLine is a challenge project under development. It is not a production banking system and must not be used to process real customer funds.