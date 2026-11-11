# Transaction History UI

## Added feature

The frontend now includes a dedicated, read-only Transaction History page at `/transactions`.

### Behaviour
- Loads the signed-in user's transaction list from `GET /api/v1/transactions`.
- Uses the current access token from the existing authentication context.
- Supports filtering by transaction type: deposit, withdrawal, transfer, or all activity.
- Supports searching transaction IDs, descriptions, initiating users, and account numbers.
- Shows ledger-entry count, account references, transaction status, amount, and timestamp.
- Includes loading, empty, error, and manual-refresh states.
- Adds a Transactions link to the authenticated navigation.

## Scope and limitations

- The page is read-only; it does not create or alter financial transactions.
- The backend endpoint currently returns up to 100 records.
- Amounts are formatted as USD because the current Prisma schema defaults account currency to USD. Do not change this display to INR without also updating account defaults, validation, seed data, migrations, and transaction tests consistently.
- This feature has not yet been build-verified in the current environment because npm dependencies are not cached and the package registry is unreachable. Run the frontend build after installing dependencies before treating it as complete.
