\# LedgerLine — Threat Model



\## 1. Purpose



This document records security and financial-integrity threats identified during LedgerLine development, their potential impact, planned protections, and verification status.



A database constraint or security configuration is not considered a complete mitigation until the relevant application workflow and tests have been implemented.



\## 2. Threats and Mitigations



\### 2.1 Unauthorized Transactions



\*\*Threat:\*\* An unauthenticated user attempts to initiate or access financial transactions.



\*\*Impact:\*\* Unauthorized movement of funds or exposure of financial information.



\*\*Planned protection:\*\*

\- Authentication using securely hashed passwords.

\- JWT-based request authentication.

\- Server-side authorization for protected endpoints.



\*\*Status:\*\* Spring Security foundation exists. Full authentication and JWT implementation remain pending.



\### 2.2 Cross-Account Access



\*\*Threat:\*\* An authenticated account holder attempts to view or operate on another user's account.



\*\*Impact:\*\* Unauthorized access to account information or funds.



\*\*Planned protection:\*\*

\- Verify account ownership on every account-specific operation.

\- Never trust account ownership supplied by the frontend.

\- Apply authorization checks in backend services.



\*\*Status:\*\* Database ownership relationship exists. Application-level ownership checks remain pending.



\### 2.3 Duplicate Transactions



\*\*Threat:\*\* A client retries a request and unintentionally processes the same transaction more than once.



\*\*Impact:\*\* Duplicate deposits, withdrawals, or transfers.



\*\*Planned protection:\*\*

\- Require idempotency keys for money-moving requests.

\- Store the key with the initiating user.

\- Enforce uniqueness in the database.

\- Return the original result for a valid retry instead of processing it again.



\*\*Status:\*\* Database uniqueness constraint implemented. Application-level idempotency handling and tests remain pending.



\### 2.4 Concurrent Withdrawals



\*\*Threat:\*\* Multiple requests attempt to withdraw funds from the same account simultaneously.



\*\*Impact:\*\* Spending more money than the available balance.



\*\*Planned protection:\*\*

\- Use database transactions.

\- Lock affected account rows during money movement.

\- Validate available funds after acquiring locks.

\- Commit balance updates and ledger entries atomically.



\*\*Status:\*\* Not yet implemented in the transaction service.



\### 2.5 Negative Account Balances



\*\*Threat:\*\* A withdrawal or transfer causes a customer account balance to become negative.



\*\*Impact:\*\* Financial loss and incorrect account balances.



\*\*Planned protection:\*\*

\- Validate available funds on the backend.

\- Enforce a database constraint on customer account balances.

\- Perform balance changes atomically.



\*\*Status:\*\* Database constraint implemented. Application-level balance validation remains pending.



\### 2.6 Ledger Imbalance



\*\*Threat:\*\* A transaction produces unequal debit and credit entries.



\*\*Impact:\*\* Money is created or lost in the recorded books.



\*\*Planned protection:\*\*

\- Use double-entry ledger posting.

\- Require equal debit and credit totals for each posted transaction.

\- Roll back the complete transaction if posting fails.

\- Add automated ledger reconciliation tests.



\*\*Status:\*\* Ledger table implemented. Posting logic, balance validation, and reconciliation tests remain pending.



\### 2.7 Audit Record Tampering



\*\*Threat:\*\* An application user attempts to modify or delete historical audit events.



\*\*Impact:\*\* Loss of accountability and unreliable transaction history.



\*\*Planned protection:\*\*

\- Use append-only audit operations.

\- Restrict database permissions.

\- Prevent ordinary updates and deletes.

\- Record actor, action, entity, and timestamp.

\- Test attempts to modify and delete audit events.



\*\*Status:\*\* Database audit table and update/delete trigger implemented. Application audit recording, least-privilege database permissions, and tests remain pending.



\*\*Limitation:\*\* The trigger does not provide cryptographic immutability against a privileged database administrator.



\### 2.8 Privilege Escalation



\*\*Threat:\*\* An account holder attempts to use administrator or auditor privileges.



\*\*Impact:\*\* Unauthorized account creation, approval, or access to financial records.



\*\*Planned protection:\*\*

\- Enforce roles on the backend.

\- Restrict administrative endpoints.

\- Do not accept role changes from ordinary registration requests.

\- Test forbidden requests for every role.



\*\*Status:\*\* User role column exists in the database. Full role-based authorization remains pending.



\### 2.9 Auditor Access Misuse



\*\*Threat:\*\* An auditor accesses accounts outside their authorized scope.



\*\*Impact:\*\* Exposure of financial information beyond assigned responsibilities.



\*\*Planned protection:\*\*

\- Maintain explicit auditor-to-account assignments.

\- Check assignment on every auditor account request.

\- Prevent auditors from performing money-moving operations.



\*\*Status:\*\* Auditor access assignment table implemented. Application-level checks remain pending.



\### 2.10 Password Exposure



\*\*Threat:\*\* Passwords are stored or transmitted insecurely.



\*\*Impact:\*\* Account compromise and unauthorized access.



\*\*Planned protection:\*\*

\- Store password hashes, never plaintext passwords.

\- Use BCrypt or an appropriate password hashing algorithm.

\- Validate login credentials on the backend.

\- Never log passwords or authentication secrets.



\*\*Status:\*\* Password hash column exists. Password hashing and authentication workflows remain pending.



\## 3. Database-Level Protections Implemented



The initial Flyway migration includes:



\- Positive transaction amount constraint.

\- Non-negative customer account balance constraint.

\- Unique transaction idempotency key per initiating user.

\- Foreign-key relationships for core entities.

\- Unique account numbers.

\- Append-only audit event trigger.

\- Explicit auditor-account assignment table.



\## 4. Verification Plan



The following tests must be added before these protections can be considered fully verified:



1\. Unauthenticated requests are rejected.

2\. Each role is restricted to its permitted endpoints.

3\. Users cannot access another customer's account.

4\. Duplicate idempotency keys do not cause duplicate postings.

5\. Concurrent withdrawals cannot overdraw an account.

6\. Failed transactions leave no partial ledger entries.

7\. Every posted transaction has balanced debit and credit entries.

8\. Audit events cannot be updated or deleted by the application role.

9\. Auditors cannot access unassigned accounts.

10\. Invalid transaction amounts are rejected.



\## 5. Current Security Assessment



LedgerLine is under active development.



Database-level protections are partially implemented. Application-level authentication, authorization, money movement, concurrency protection, and automated security verification are still in progress.



No production-security claim is made at this stage.

