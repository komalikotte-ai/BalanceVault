# BalanceVault

**A Secure Financial Management Platform with Double-Entry Ledger**

BalanceVault is a full-stack financial management application designed to support secure account operations, reliable money transfers, and transparent transaction tracking.

## Features

- User registration and JWT-based authentication
- Role-based access for Admin, Account Holder, and Auditor
- Account creation and approval
- Deposits, withdrawals, and fund transfers
- Transaction history and audit visibility
- Double-entry ledger for financial record accuracy
- Idempotent transaction handling
- Protected API endpoints and input validation

## Technology Stack

| Layer | Technologies |
|---|---|
| Frontend | React, TypeScript, Vite |
| Backend | Node.js, Express, TypeScript |
| Database | PostgreSQL, Prisma |
| Security | JWT, Argon2, Zod |
| Deployment | Vercel, Render |

## Run Locally

**Requirements:** Node.js, npm, and PostgreSQL.

1. Open the BalanceVault project folder in VS Code.
2. Configure the environment variables using `.env.example`.
3. Install dependencies:

   ```bash
   npm ci
   ```

4. Generate Prisma Client and run database migrations:

   ```bash
   npm run prisma:generate --workspace=backend
   npm run prisma:migrate --workspace=backend
   ```

5. Start the backend and frontend in separate terminals:

   ```bash
   npm run dev:backend
   ```

   ```bash
   npm run dev:frontend
   ```

## Live Application

- **Frontend:** https://balancevault-beryl.vercel.app/
- **Backend Health:** https://balancevault-api-ztfa.onrender.com/api/v1/health

## Project Scope

BalanceVault demonstrates full-stack development, role-based access control, secure financial operations, database transaction handling, and ledger-based transaction tracking.

## Future Improvements

- Advanced audit reports and exports
- Enhanced production security
- Multi-currency support
- Distributed rate limiting

---

**Note:** This project is intended for demonstration and educational purposes. It has not undergone an independent security audit and should not be used to manage real funds.

**Developed by Komali Kotte**
