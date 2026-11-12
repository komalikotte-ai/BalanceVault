# BalanceVault

### Secure Account Management & Double-Entry Financial Ledger Platform

**Live Demo:** https://balancevault-beryl.vercel.app/  
**GitHub Repository:** https://github.com/komalikotte-ai/BalanceVault

BalanceVault is a full-stack financial account management application designed to demonstrate secure money movement, double-entry ledger accounting, role-based access control, and auditable transaction processing.

The platform provides account onboarding, deposits, withdrawals, transfers, transaction history, and audit visibility through dedicated account-holder, administrator, and auditor workflows.

## Overview

Financial applications need reliable transaction processing, controlled access, accurate financial records, and traceable activities.

BalanceVault addresses these requirements through database transactions, account-row locking, idempotency keys, and paired debit and credit ledger entries.

## Key Features

**Authentication & Access Control**
- User registration and login.
- JWT-based access-token authentication.
- HttpOnly refresh-token cookies.
- Role-based authorization.
- Protected application routes.

**Account Management**
- Checking and savings accounts.
- Account creation and balance lookup.
- Account status management.
- Account ownership validation.

**Customer Onboarding**
- Account-holder onboarding submissions.
- Onboarding status tracking.
- Administrator review and approval workflows.

**Financial Transactions**
- Deposits and withdrawals.
- Account-to-account transfers.
- Transaction history.
- Idempotency support.
- Double-entry debit and credit ledger records.
- Database transactions and account-row locking.

**Audit & Administration**
- Administrator account-management workspace.
- Onboarding review workflows.
- Read-only auditor workspace.
- Audit event visibility.

**Operational Features**
- API health and readiness endpoints.
- Request validation using Zod.
- Security headers with Helmet.
- Redis rate limiting with an in-memory fallback.

## Application Roles

| Role | Responsibilities |
|---|---|
| ADMIN | Manage accounts, review onboarding requests, and access administrative operations. |
| ACCOUNT_HOLDER | Submit onboarding requests, access permitted accounts, and perform authorized transactions. |
| AUDITOR | Read audit records and access permitted platform-wide transaction information. |

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
       API Requests
          |
          v
   Express REST API
    (Render Hosting)
          |
          v
     Prisma ORM
          |
          v
      PostgreSQL
          |
    +-----+------+
    |     |      |
    v     v      v
 Accounts Transactions Audit Logs
```

## Financial Transaction Design

BalanceVault uses a double-entry ledger approach for financial operations.

Every successful transfer is represented by corresponding debit and credit entries.

Example: transferring 100 units between two accounts.

| Account | Entry | Amount |
|---|---|---:|
| Source Account | Debit | 100 |
| Destination Account | Credit | 100 |

The application uses:
- Database transactions to keep related changes atomic.
- Account-row locking to reduce concurrent balance-update conflicts.
- Idempotency keys to help prevent duplicate processing.
- Paired ledger entries for transaction traceability.

The current demo configuration uses USD with four decimal places.

## Getting Started

Follow these instructions to run BalanceVault locally on Windows.

### Prerequisites

Install:
- Node.js 18 or later.
- npm.
- PostgreSQL 14 or later.
- Git (optional).
- Visual Studio Code (recommended).

Redis is optional for local development because the application includes an in-memory fallback.

### 1. Download the Project

1. Open the [BalanceVault GitHub repository](https://github.com/komalikotte-ai/BalanceVault).
2. Click the green **Code** button.
3. Select **Download ZIP**.
4. Extract the downloaded ZIP file.
5. Open the extracted `BalanceVault` folder in Visual Studio Code.

### 2. Configure Environment Variables

Open a terminal in the extracted project folder.

Copy the backend environment template:

```cmd
copy backend\.env.example backend\.env
```

Open `backend/.env` and configure the required environment variables.

Example database configuration:

```env
DATABASE_URL="postgresql://postgres:YOUR_PASSWORD@localhost:5432/balancevault_db?schema=public"
CORS_ORIGIN="http://localhost:5173"
```

Replace `YOUR_PASSWORD` with your PostgreSQL password and configure the remaining variables using `backend/.env.example`.

Create a PostgreSQL database named:

```text
balancevault_db
```

### 3. Install Dependencies

Run this command from the project root:

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

## Demo Credentials

After successfully loading the demo seed data:

| Role | Email | Password |
|---|---|---|
| Admin | admin@balancevault.local | DemoPass123! |
| Auditor | auditor@balancevault.local | DemoPass123! |
| Account Holder | alice@example.com | DemoPass123! |
| Pending Onboarding | bob@example.com | DemoPass123! |

These are public demonstration credentials. Never use them in a production environment.

## API Overview

**Local API Base URL:**

```text
http://localhost:5000/api/v1
```

| Method | Endpoint | Purpose |
|---|
