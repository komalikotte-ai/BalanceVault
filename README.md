# BalanceVault
### Secure Financial Management Platform with Double-Entry Ledger

BalanceVault is a full-stack financial management platform designed to support secure account operations, reliable financial transactions, and transparent record keeping. It combines role-based access control, account management, and double-entry ledger functionality to improve transaction integrity and accountability.

---

## 🌐 Live Application

| Component | Link |
|---|---|
| Live Application | https://balancevault-beryl.vercel.app/ |
| Backend Health Check | https://balancevault-api-ztfa.onrender.com/api/v1/health |
| GitHub Repository | https://github.com/komalikotte-ai/BalanceVault |

---

## 📑 Table of Contents

1. Project Overview
2. User Roles and Access
3. Core Features
4. Financial Operations and Ledger
5. Application Architecture
6. Security and Reliability
7. Deployment
8. Local Setup
9. Testing and Build
10. Current Limitations
11. Future Enhancements

---

## 1. Project Overview

BalanceVault provides a centralized platform for managing financial accounts, processing transactions, and maintaining traceable financial records.

The application supports three distinct roles with different permissions, allowing users to perform financial operations while administrators manage account workflows and auditors review activity.

## 2. User Roles and Access

| Role | Access and Responsibilities |
|---|---|
| ADMIN | Manage users, review onboarding requests, approve accounts, and oversee platform operations. |
| ACCOUNT_HOLDER | Manage personal accounts, perform permitted financial operations, and view transaction history. |
| AUDITOR | Review audit records and financial activity with read-only access. |

## 3. Core Features

**Authentication and User Management**
- User registration and login.
- JWT-based authentication.
- Argon2 password hashing.
- Protected routes and role-based authorization.

**Account Management**
- Checking and savings account support.
- Account creation and onboarding workflows.
- Administrative account approval.
- Account status and management operations.

**Financial Transactions**
- Deposits and withdrawals.
- Transfers between accounts.
- Transaction history and status tracking.
- Idempotency handling to help prevent duplicate processing.

**Dashboards and Audit**
- Role-specific dashboards.
- Account and transaction summaries.
- Administrative account management.
- Auditor access to audit records.

## 4. Financial Operations and Double-Entry Ledger

BalanceVault uses a double-entry ledger approach to record financial transactions through corresponding debit and credit entries.

| Operation | Description |
|---|---|
| Deposit | Records a deposit into an eligible account and its corresponding ledger entries. |
| Withdrawal | Records a withdrawal and its corresponding ledger entries. |
| Transfer | Records the debit and credit entries associated with a transfer between accounts. |

**Transaction integrity mechanisms include:**

- Double-entry ledger records.
- Database transaction handling.
- Account-row locking to help prevent conflicting concurrent updates.
- Idempotency handling for repeated requests.
- Transaction history for traceability.

## 5. Application Architecture

BalanceVault follows a frontend-backend architecture.

**Frontend**
- React and TypeScript.
- Vite-based development and production builds.
- React Router for navigation.
- Role-specific dashboards and protected application routes.
- Backend API integration.
- Lucide React interface icons.

**Backend**
- Node.js, Express, and TypeScript.
- REST APIs for authentication, accounts, and transactions.
- Authentication and authorization middleware.
- Argon2 password hashing.
- Zod request validation.
- Helmet security headers and CORS configuration.
- Centralized error handling.

**Database and Supporting Services**
- PostgreSQL for persistent data storage.
- Prisma ORM for database operations.
- Database migrations and seed support.
- Redis-based rate limiting with an in-memory fallback.

## 6. Security and Reliability

The application incorporates several security and reliability measures:

- JWT-based authentication.
- Argon2 password hashing.
- Role-based access control.
- Protected API endpoints.
- Request validation using Zod.
- HTTP security headers using Helmet.
- CORS configuration.
- HttpOnly refresh-token cookies.
- Environment-based configuration for sensitive values.
- Database transactions and account-row locking for financial operations.
- Idempotency handling to reduce duplicate transaction processing.

## 7. Deployment

The application is deployed using separate frontend and backend services.

| Component | Platform |
|---|---|
| Frontend | Vercel |
| Backend API | Render |
| Database | PostgreSQL |

The deployed frontend communicates with the backend through the configured production API routing.

## 8. Local Setup

### Prerequisites

- Node.js
- npm
- PostgreSQL

### Step 1: Open the Project

Open the existing BalanceVault project folder in Visual Studio Code.

### Step 2: Configure Environment Variables

Create the backend environment file:

```bash
copy backend\.env.example backend\.env
```

Configure the required environment variables using the provided template, including the PostgreSQL connection URL, JWT secrets, and CORS origin.

### Step 3: Install Dependencies

Run from the project root:

```bash
npm ci
```

### Step 4: Generate Prisma Client

```bash
npm run prisma:generate --workspace=backend
```

### Step 5: Run Database Migrations

```bash
npm run prisma:migrate --workspace=backend
```

### Step 6: Start the Backend

Open a terminal in the project root:

```bash
npm run dev:backend
```

### Step 7: Start the Frontend

Open a second terminal in the project root:

```bash
npm run dev:frontend
```

Open the application at:

http://localhost:5173

## 9. Testing and Build

The project provides npm scripts for running tests and building the application.

**Run tests:**

```bash
npm test
```

**Build the application:**

```bash
npm run build
```

The frontend production build has been verified successfully, and the deployed backend health endpoint has returned a successful response.

## 10. Current Limitations

- Additional end-to-end testing is needed for administrative and auditor workflows.
- The in-memory rate-limit fallback is not suitable for distributed production deployments.
- Advanced audit filtering and export functionality can be expanded.
- Additional automated integration and frontend test coverage is needed.
- Further security hardening and independent security testing are recommended.
- Multi-currency support is not currently implemented.

## 11. Future Enhancements

- Advanced audit reports and export functionality.
- External Redis configuration for distributed rate limiting.
- Expanded automated integration and frontend testing.
- Improved production security and monitoring.
- Multi-currency account support.
