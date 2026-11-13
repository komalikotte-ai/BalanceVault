# BalanceVault

**A Secure Financial Management Platform with Double-Entry Ledger**

BalanceVault is a full-stack financial management application designed to support secure account operations, reliable money transfers, and transparent transaction tracking. It provides role-based access, account management, financial transactions, and ledger-based transaction records.

## Live Application

| Component | Link |
|---|---|
| Frontend | https://balancevault-beryl.vercel.app/ |
| Backend API | https://balancevault-api-ztfa.onrender.com/ |
| API Health | https://balancevault-api-ztfa.onrender.com/api/v1/health |


## Table of Contents

- Project Overview
- User Roles
- Core Features
- Financial Operations
- Double-Entry Ledger
- Application Architecture
- Security and Reliability
- Deployment
- Local Setup
- Testing and Build
- Current Limitations
- Future Enhancements

## Project Overview

BalanceVault is designed to demonstrate secure financial operations through a structured full-stack application. It supports account creation, account approval, deposits, withdrawals, fund transfers, transaction history, and audit visibility.

The platform uses a double-entry ledger approach to maintain corresponding debit and credit records for financial transactions.

## User Roles

| Role | Access |
|---|---|
| Admin | User and account management, account approval, and administrative operations |
| Account Holder | Account access, deposits, withdrawals, transfers, and transaction history |
| Auditor | Read-only access to transaction and audit information |

## Core Features

### Authentication and User Management
- User registration and login
- JWT-based authentication
- Protected application routes
- Role-based access control

### Account Management
- Checking and savings account support
- Account creation
- Account approval workflow
- Account balance tracking

### Financial Transactions
- Deposit operations
- Withdrawal operations
- Fund transfers between accounts
- Transaction history
- Transaction status tracking
- Idempotency support for transaction requests

### Dashboard and Audit
- Role-based dashboard views
- Account and transaction information
- Financial activity visibility
- Audit-oriented transaction records

## Financial Operations

| Operation | Description |
|---|---|
| Deposit | Adds funds to an account |
| Withdrawal | Deducts funds from an account |
| Transfer | Moves funds between accounts |
| Account Approval | Allows administrative approval of accounts |
| Transaction History | Displays recorded financial activities |

## Double-Entry Ledger

BalanceVault uses a double-entry ledger approach to record financial activity. Each supported financial transaction is represented through corresponding debit and credit entries.

### Transaction Integrity

- Database transactions for financial operations
- Account-level locking to help prevent conflicting balance updates
- Idempotency handling to reduce duplicate transaction processing
- Structured transaction records for traceability

## Application Architecture

### Frontend
- React
- TypeScript
- Vite
- React Router
- Lucide React

### Backend
- Node.js
- Express.js
- TypeScript
- REST API
- JWT authentication
- Argon2 password hashing
- Zod validation
- Helmet security middleware
- CORS configuration

### Database and Supporting Services
- PostgreSQL
- Prisma ORM
- Database migrations
- Database seeding
- Redis integration with an in-memory fallback

## Security and Reliability

- JWT-based authentication
- Password hashing
- Role-based authorization
- Protected API endpoints
- Request validation
- Security headers through Helmet
- CORS configuration
- Database-backed transaction processing
- Idempotency handling for financial operations

## Deployment

| Service | Platform |
|---|---|
| Frontend | Vercel |
| Backend API | Render |
| Database | PostgreSQL |

The frontend and backend are deployed separately and communicate through the backend REST API.

## Local Setup

### Prerequisites

- Node.js
- npm
- PostgreSQL
- Git
- VS Code or another code editor

### Environment Configuration

Create the backend environment file using the provided example:

```cmd
copy backend\.env.example backend\.env
```

Configure the required environment variables in `backend/.env`, including:

```env
DATABASE_URL=
JWT_SECRET=
JWT_REFRESH_SECRET=
ADMIN_BOOTSTRAP_SECRET=
CORS_ORIGIN=
```

Use appropriate local database credentials and secure secret values.

### Installation

Install project dependencies:

```bash
npm ci
```

Generate the Prisma client:

```bash
npm run prisma:generate --workspace=backend
```

Run database migrations:

```bash
npm run prisma:migrate --workspace=backend
```

### Start the Application

Start the backend:

```bash
npm run dev:backend
```

Start the frontend in a separate terminal:

```bash
npm run dev:frontend
```

### Local Application URLs

| Component | URL |
|---|---|
| Frontend | http://localhost:5173 |
| Backend | http://localhost:5000 |
| API Health | http://localhost:5000/api/v1/health |
| API Readiness | http://localhost:5000/api/v1/ready |

## Testing and Build

Run the project tests:

```bash
npm test
```

Build the application:

```bash
npm run build
```

The frontend production build has been verified, and the deployed backend health endpoint has returned a successful response.

## Current Limitations

- Additional automated and end-to-end test coverage is needed.
- The in-memory fallback is not a replacement for shared Redis in a distributed production deployment.
- Advanced audit filtering and export capabilities can be expanded.
- Additional security hardening and independent security testing are recommended.
- Multi-currency support is not currently included.

## Conclusion

BalanceVault brings together secure authentication, role-based access, account management, financial transactions, and double-entry ledger functionality in a full-stack financial management application. The project demonstrates the integration of modern web technologies with database-driven financial operations, focusing on transaction integrity, security, and transparency.
