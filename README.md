# BalanceVault

### Secure Account Management & Double-Entry Financial Ledger Platform

[![Live Demo](https://img.shields.io/badge/Live%20Demo-Visit%20BalanceVault-2563EB?style=for-the-badge)](https://balancevault-beryl.vercel.app/)
[![GitHub](https://img.shields.io/badge/GitHub-Source%20Code-181717?style=for-the-badge&logo=github)](https://github.com/komalikotte-ai/BalanceVault)

BalanceVault is a full-stack financial account management application designed to demonstrate secure money movement, double-entry ledger accounting, role-based access control, and auditable transaction processing.

The platform provides account onboarding, deposits, withdrawals, transfers, transaction history, and audit visibility through dedicated user, administrator, and auditor workflows.

**Live Application:** https://balancevault-beryl.vercel.app/

---

## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Application Roles](#application-roles)
- [Technology Stack](#technology-stack)
- [System Architecture](#system-architecture)
- [Financial Transaction Design](#financial-transaction-design)
- [Getting Started](#getting-started)
- [Demo Credentials](#demo-credentials)
- [API Overview](#api-overview)
- [Security](#security)
- [Project Structure](#project-structure)
- [Testing](#testing)
- [Limitations](#limitations)
- [Future Improvements](#future-improvements)
- [Disclaimer](#disclaimer)

---

## Overview

Financial applications must do more than display account balances. They need reliable transaction processing, controlled access, accurate financial records, and the ability to trace important activities.

BalanceVault addresses these concerns through a centralized account management and ledger platform.

The application uses database transactions, account-row locking, idempotency keys, and paired debit and credit ledger entries to support consistent financial operations.

It is built as a full-stack application with a React frontend, Express REST API, PostgreSQL database, and Prisma ORM.

## Key Features

### Authentication & Access Control
- User registration and login.
- JWT-based access-token authentication.
- HttpOnly refresh-token cookies.
- Role-based authorization.
- Protected application routes.

### Account Management
- Checking and savings account support.
- Account creation and balance lookup.
- Account status management by administrators.
- Account ownership validation.

### Customer Onboarding
- Account-holder onboarding submissions.
- Onboarding status tracking.
- Administrator review and approval workflows.

### Financial Transactions
- Deposits.
- Withdrawals.
- Account-to-account transfers.
- Transaction history.
- Idempotency support for financial mutation requests.
- Double-entry debit and credit ledger records.
- Database transactions and account-row locking.

### Audit & Administration
- Administrator account-management workspace.
- Onboarding review workflows.
- Read-only auditor workspace.
- Audit event visibility.
- Role-based access to administrative operations.

### Operational Features
- API health and readiness endpoints.
- Request validation using Zod.
- Security headers with Helmet.
- Rate limiting with Redis and an in-memory fallback.

---

## Application Roles

| Role | Responsibilities |
|---|---|
| ADMIN | Manage accounts, review onboarding requests, and access administrative operations. |
| ACCOUNT_HOLDER | Submit onboarding requests, access permitted accounts, and perform authorized transactions. |
| AUDITOR | Read audit records and access permitted platform-wide transaction information. |

Each role has a different level of access enforced by backend authorization.

---

## Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | React 18, TypeScript, Vite |
| Routing | React Router |
| UI Icons | Lucide React |
| Backend | Node.js, Express 4, TypeScript |
| Database | PostgreSQL |
| ORM | Prisma 5 |
| Authentication | JWT, Argon2, HttpOnly Cookies |
| Validation | Zod |
| Security | Helmet, CORS, Rate Limiting |
| Cache / Rate Limiting | Redis with in-memory fallback |
| Deployment | Vercel (Frontend), Render (Backend) |

---

## System Architecture

```text
             USER / ADMIN / AUDITOR
                       |
                       v
              React Frontend
               (Vite + TS)
                       |
                       v
                Vercel Hosting
                       |
                 /api requests
                       |
                       v
               Express REST API
                (Render Hosting)
                       |
          +------------+------------+
          |            |            |
          v            v            v
     Authentication  Business    Validation
       & Security     Logic        (Zod)
          |            |            |
          +------------+------------+
                       |
                       v
                 Prisma ORM
                       |
                       v
                 PostgreSQL
                       |
          +------------+------------+
          |            |            |
          v            v            v
       Accounts     Transactions   Audit Logs
```

---

## Financial Transaction Design

BalanceVault uses a double-entry ledger approach for financial operations.

Every successful transfer is represented by corresponding debit and credit entries.

For example, when an account transfers 100 units to another account:

| Account | Entry | Amount |
|---|---|---:|
| Source Account | Debit | 100 |
| Destination Account | Credit | 100 |

The application uses:

- Database transactions to keep related changes atomic.
- Account-row locking to reduce concurrent balance-update conflicts.
- Idempotency keys to help prevent duplicate processing when requests are retried.
- Paired ledger entries to maintain transaction traceability.

The current demo configuration uses USD with four decimal places.

---

## Getting Started

Follow these instructions to run BalanceVault locally on Windows.

### Prerequisites

Install the following:

- Node.js 18 or later.
- npm.
- PostgreSQL 14 or later.
- Git.
- Visual Studio Code (recommended).

Redis is optional for local development.

### 1. Clone the Repository

```bash
git clone https://github.com/komalikotte-ai/BalanceVault.git
cd BalanceVault
```

### 2. Configure Environment Variables

Copy the backend environment template:

**Windows CMD:**

```cmd
copy backend\.env.example backend\.env
```

Open `backend/.env` and configure the required values.

Example database configuration:

```env
DATABASE_URL="postgresql://postgres:YOUR_PASSWORD@localhost:5432/balancevault_db?schema=public"
CORS_ORIGIN="http://localhost:5173"
```

Configure all other required variables using `backend/.env.example`.

Create a PostgreSQL database named:

```text
balancevault_db
```

### 3. Install Dependencies

Run from the project root:

```bash
npm ci
```

### 4. Generate Prisma Client

```bash
npm run prisma:generate --workspace=backend
```

### 5. Run Database Migrations

```bash
npm run prisma:migrate --workspace=backend
```

### 6. Load Demo Data (Optional)

For a fresh, disposable database only:

```bash
npm run db:seed --workspace=backend
```

The seed script refuses to run if users already exist in the database.

**Important:** Never run demo seeding against a database containing personal or important data.

### 7. Start the Backend

Open Terminal 1:

```bash
npm run dev:backend
```

Backend API:

```text
http://localhost:5000
```

### 8. Start the Frontend

Open Terminal 2:

```bash
npm run dev:frontend
```

Frontend:

```text
http://localhost:5173
```

The frontend uses the Vite development proxy to communicate with the local backend.

---

## Demo Credentials

After successfully loading the demo seed data, use the following accounts:

| Role | Email | Password |
|---|---|---|
| Admin | admin@balancevault.local | DemoPass123! |
| Auditor | auditor@balancevault.local | DemoPass123! |
| Account Holder | alice@example.com | DemoPass123! |
| Pending Onboarding | bob@example.com | DemoPass123! |

These are public demonstration credentials.

**Never use these credentials in a production environment.**

---

## API Overview

Base URL (local):

```text
http://localhost:5000/api/v1
```

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/auth/register` | Register an account holder |
| POST | `/auth/login` | Authenticate a user |
| GET | `/auth/me` | Retrieve current user information |
| POST | `/onboarding/submit` | Submit onboarding information |
| GET | `/onboarding/status` | Retrieve onboarding status |
| GET | `/admin/onboardings` | View onboarding requests |
| PATCH | `/admin/onboardings/:id/review` | Review an onboarding request |
| GET / POST | `/accounts` | Retrieve or create accounts |
| PATCH | `/admin/accounts/:id/status` | Update account status |
| POST | `/transactions/deposit` | Process a deposit |
| POST | `/transactions/withdraw` | Process a withdrawal |
| POST | `/transactions/transfer` | Process an account transfer |
| GET | `/transactions` | Retrieve permitted transaction history |
| GET | `/auditor/audit-logs` | Retrieve audit events |
| GET | `/health` | Check API health |
| GET | `/ready` | Check API readiness |

Access to endpoints is controlled by authentication, role authorization, and account ownership rules.

Financial mutation requests accept an `Idempotency-Key` header or an `idempotencyKey` body property.

---

## Security

BalanceVault incorporates several security-related mechanisms:

- Password hashing using Argon2.
- JWT access-token authentication.
- HttpOnly refresh-token cookies.
- Role-based authorization.
- Account ownership checks.
- Request validation with Zod.
- HTTP security headers through Helmet.
- CORS configuration.
- Rate limiting.
- Database transaction handling.
- Idempotency support for financial operations.
- Audit event recording.

Security configuration and deployment secrets must be managed separately from source code.

---

## Project Structure

```text
BalanceVault/
│
├── backend/
│   ├── prisma/
│   └── src/
│       ├── config/
│       ├── controllers/
│       ├── middleware/
│       ├── routes/
│       ├── services/
│       └── server.ts
│
├── frontend/
│   ├── public/
│   └── src/
│       ├── components/
│       ├── context/
│       ├── pages/
│       └── main.tsx
│
├── documentation/
├── .env.example
├── package.json
├── package-lock.json
├── README.md
├── SUBMISSION_STATUS.md
├── THREATS.md
└── PROMPTS.md
```

---

## Testing

Run backend tests:

```bash
npm test
```

Build the complete project:

```bash
npm run build
```

The current backend test suite connects through `DATABASE_URL` and expects demo seed data.

Use a disposable test database when running tests. Do not use a database containing personal or important information.

---

## Limitations

BalanceVault is an educational and demonstration project. Some capabilities remain limited:

- The auditor role currently has read-only platform-wide visibility; per-account auditor permissions are not implemented.
- Advanced audit-log filtering and export are not implemented.
- Redis is optional in local development; the in-memory fallback is not suitable for distributed production rate limiting.
- The supplied frontend stores access tokens in browser local storage. A hardened session design and additional CSRF protections are recommended.
- The application has not undergone an independent security audit.
- The demo currency configuration is USD and is not a multi-currency payment system.

---

## Future Improvements

- Automated end-to-end testing.
- Advanced audit filtering and export.
- Improved session and token security.
- Persistent distributed rate limiting.
- Expanded monitoring and operational logging.
- Stronger concurrency and security testing.
- Multi-currency support with coordinated ledger and validation changes.

---

## Disclaimer

BalanceVault is an educational software project demonstrating financial application concepts.

It is not a regulated banking platform, payment processor, or production-ready financial service. Do not use it to manage real funds or sensitive financial information.

---

## Author

**Komali Kotte**

GitHub: [@komalikotte-ai](https://github.com/komalikotte-ai)

Project: [BalanceVault](https://github.com/komalikotte-ai/BalanceVault)

Live Demo: [balancevault-beryl.vercel.app](https://balancevault-beryl.vercel.app/)
