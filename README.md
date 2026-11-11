# BalanceVault

> Money movement with a ledger you can prove.

BalanceVault is a role-based account management and money-movement application built with React, Express, TypeScript, PostgreSQL and Prisma. It demonstrates account onboarding, account management, deposits, withdrawals, transfers, double-entry ledger records and audit visibility.

## Features

- Registration, login, access-token authentication and HttpOnly refresh-token cookie.
- Roles: `ADMIN`, `ACCOUNT_HOLDER`, `AUDITOR`.
- Account-holder onboarding submission and Admin review.
- Checking and savings account creation, balance lookup and Admin status controls.
- Deposits, withdrawals and transfers with database transactions, account-row locks, idempotency keys and paired debit/credit entries.
- Transaction history with ownership scoping for account holders.
- Read-only Auditor workspace for audit events.
- Audit logging and Redis rate limiting with an in-memory local fallback.

**Demo currency:** the current Prisma schema and seed data use USD with four decimal places. This is a demo configuration, not a multi-currency payment processor. Do not change only the UI currency symbol; changing currency requires coordinated database, validation, ledger and test changes.

## Technology

- Frontend: React 18, TypeScript, Vite, React Router, Lucide
- Backend: Node.js 18+, Express 4, TypeScript, Zod
- Database: PostgreSQL 14+, Prisma 5
- Security: Argon2, JWT access tokens, HttpOnly refresh cookie, role checks, Helmet, rate limiting

## Windows setup (VS Code)

### 1. Prerequisites

Install Node.js 18 or later and PostgreSQL 14 or later. Redis is optional for local development; the backend falls back to an in-memory rate limiter when Redis is unavailable. In-memory rate limits are per-process and are not suitable for multi-instance production deployment.

### 2. Configure the backend

Open the extracted `BalanceVault-main` folder in VS Code. In CMD from that folder:

```cmd
copy backend\.env.example backend\.env
```

Edit `backend\.env` and set `DATABASE_URL` to your local PostgreSQL database. Example:

```env
DATABASE_URL="postgresql://postgres:YOUR_PASSWORD@localhost:5432/balancevault_db?schema=public"
CORS_ORIGIN="http://localhost:5173"
```

Create an empty PostgreSQL database named `balancevault_db` using pgAdmin or your PostgreSQL tool.

### 3. Install packages and prepare the database

Run from the project root:

```cmd
npm install
npm run prisma:generate --workspace=backend
npm run prisma:migrate --workspace=backend
```

To load synthetic demo users and accounts into a **fresh, disposable database only**:

```cmd
npm run db:seed --workspace=backend
```

The seed script refuses to run if the database already contains users. It does not delete existing data.

Demo credentials after seeding:

| Role | Email | Password |
|---|---|---|
| Admin | admin@balancevault.local | DemoPass123! |
| Auditor | auditor@balancevault.local | DemoPass123! |
| Account holder | alice@example.com | DemoPass123! |
| Pending onboarding | bob@example.com | DemoPass123! |

These are public demo credentials. Never use them in a deployed environment.

### 4. Start the backend

Terminal 1:

```cmd
npm run dev:backend
```

API: `http://localhost:5000`

- Health: `http://localhost:5000/api/v1/health`
- Readiness: `http://localhost:5000/api/v1/ready`

### 5. Start the frontend

Terminal 2:

```cmd
npm run dev:frontend
```

Open `http://localhost:5173`.

### 6. Run tests and production builds

```cmd
npm test
npm run build
```

The current Vitest suite connects through `DATABASE_URL` and expects the demo seed data. Point `DATABASE_URL` at a disposable test database before running tests. Do not run the demo seed against a database containing personal or important data.

## Main API routes

| Method | Route | Access |
|---|---|---|
| POST | `/api/v1/auth/register` | Public; creates Account Holder only |
| POST | `/api/v1/auth/login` | Public |
| GET | `/api/v1/auth/me` | Authenticated |
| POST | `/api/v1/onboarding/submit` | Authenticated user |
| GET | `/api/v1/onboarding/status` | Authenticated user |
| GET | `/api/v1/admin/onboardings` | Admin |
| PATCH | `/api/v1/admin/onboardings/:id/review` | Admin |
| GET/POST | `/api/v1/accounts` | Authenticated; scoped by role |
| PATCH | `/api/v1/admin/accounts/:id/status` | Admin |
| POST | `/api/v1/transactions/deposit` | Account owner or Admin |
| POST | `/api/v1/transactions/withdraw` | Account owner or Admin |
| POST | `/api/v1/transactions/transfer` | Account owner or Admin |
| GET | `/api/v1/transactions` | Account Holder scoped; Admin/Auditor read-only global view |
| GET | `/api/v1/auditor/audit-logs` | Auditor or Admin, read-only |

Financial mutation requests accept an `Idempotency-Key` header or an `idempotencyKey` body property. Reuse the same key only when retrying the exact same request.

## Important limitations before production

- This is an educational/demo application. It is not a regulated banking platform and has not undergone an independent security audit.
- The Auditor role currently has read-only platform-wide visibility; per-account auditor grants/revocations are not implemented.
- The audit feed is a read-only list of recorded events; advanced filters and export are not implemented.
- The current access token is returned to the frontend and stored by the supplied frontend in browser local storage. The refresh token is HttpOnly. For production, move to a hardened access-token/session design and add CSRF protections where cookie-authenticated mutations are used.
- Before deployment, rotate all secrets, remove demo credentials, configure least-privilege database roles, use HTTPS, review migrations, add operational monitoring and complete security/concurrency testing.

## Documentation

See `documentation/` for architecture, database schema, bootstrap and development notes. Use `documentation/manual-verification-checklist.md` for the local test sequence. See `SUBMISSION_STATUS.md` for the verification status and known limitations of this submission candidate.
