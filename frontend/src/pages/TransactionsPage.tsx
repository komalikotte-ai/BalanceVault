import { useCallback, useEffect, useMemo, useState } from 'react';
import { ArrowDownLeft, ArrowLeftRight, ArrowUpRight, RefreshCw, Search, WalletCards } from 'lucide-react';
import { useAuth } from '../context/AuthContext.tsx';

type LedgerEntry = {
  id: string;
  accountId: string;
  accountNumber: string;
  accountType: string;
  accountHolderName?: string | null;
  direction: 'DEBIT' | 'CREDIT';
  amount: string;
  balanceAfter: string;
};

type LedgerTransaction = {
  id: string;
  type: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER';
  status: string;
  description: string;
  createdAt: string;
  initiatedBy: { id: string; fullName: string; email: string; role: string };
  entries: LedgerEntry[];
};

type Filter = 'ALL' | LedgerTransaction['type'];

const money = (value: string) => new Intl.NumberFormat('en-US', {
  style: 'currency', currency: 'USD', minimumFractionDigits: 2, maximumFractionDigits: 4,
}).format(Number(value));

const dateTime = (value: string) => new Intl.DateTimeFormat(undefined, {
  dateStyle: 'medium', timeStyle: 'short',
}).format(new Date(value));

const transactionIcon = (type: LedgerTransaction['type']) => {
  if (type === 'DEPOSIT') return <ArrowDownLeft size={17} />;
  if (type === 'WITHDRAWAL') return <ArrowUpRight size={17} />;
  return <ArrowLeftRight size={17} />;
};

export function TransactionsPage() {
  const { token, user } = useAuth();
  const [transactions, setTransactions] = useState<LedgerTransaction[]>([]);
  const [filter, setFilter] = useState<Filter>('ALL');
  const [search, setSearch] = useState('');
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const loadTransactions = useCallback(async () => {
    if (!token) return;
    setLoading(true);
    setError('');
    try {
      const response = await fetch('/api/v1/transactions', {
        headers: { Authorization: `Bearer ${token}` },
      });
      const payload = await response.json();
      if (!response.ok || !payload.success) {
        throw new Error(payload.error?.message || 'Unable to load transaction history.');
      }
      setTransactions(Array.isArray(payload.data) ? payload.data : []);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Unable to load transaction history.');
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => { void loadTransactions(); }, [loadTransactions]);

  const visibleTransactions = useMemo(() => {
    const query = search.trim().toLowerCase();
    return transactions.filter((transaction) => {
      const matchesType = filter === 'ALL' || transaction.type === filter;
      const searchable = [
        transaction.id,
        transaction.description,
        transaction.initiatedBy?.fullName,
        ...transaction.entries.map((entry) => entry.accountNumber),
      ].join(' ').toLowerCase();
      return matchesType && (!query || searchable.includes(query));
    });
  }, [transactions, filter, search]);

  return (
    <section className="transactions-page">
      <div className="transactions-heading">
        <div>
          <p className="eyebrow">FINANCIAL ACTIVITY</p>
          <h1>Transaction history</h1>
          <p className="page-description">
            {user?.role === 'ACCOUNT_HOLDER'
              ? 'Review activity across your accounts.'
              : 'Review posted financial activity and its ledger records.'}
          </p>
        </div>
        <button className="secondary-action" onClick={() => void loadTransactions()} disabled={loading}>
          <RefreshCw size={16} className={loading ? 'spin' : ''} /> Refresh
        </button>
      </div>

      <div className="transaction-summary-grid">
        <article className="transaction-summary-card">
          <span>Visible records</span>
          <strong>{visibleTransactions.length}</strong>
          <small>Showing up to 100 latest transactions</small>
        </article>
        <article className="transaction-summary-card">
          <span>Deposits</span>
          <strong>{transactions.filter((item) => item.type === 'DEPOSIT').length}</strong>
          <small>Credit activity</small>
        </article>
        <article className="transaction-summary-card">
          <span>Withdrawals & transfers</span>
          <strong>{transactions.filter((item) => item.type !== 'DEPOSIT').length}</strong>
          <small>Outgoing or internal movement</small>
        </article>
      </div>

      <div className="transaction-toolbar">
        <div className="transaction-filters" role="group" aria-label="Filter transactions">
          {(['ALL', 'DEPOSIT', 'WITHDRAWAL', 'TRANSFER'] as Filter[]).map((item) => (
            <button key={item} className={filter === item ? 'filter-chip active' : 'filter-chip'} onClick={() => setFilter(item)}>
              {item === 'ALL' ? 'All activity' : item.charAt(0) + item.slice(1).toLowerCase()}
            </button>
          ))}
        </div>
        <label className="transaction-search">
          <Search size={16} />
          <input value={search} onChange={(event) => setSearch(event.target.value)} placeholder="Search description, account or ID" />
        </label>
      </div>

      <div className="transaction-table-card">
        {loading ? (
          <div className="transaction-state"><RefreshCw className="spin" size={22} /><span>Loading transaction history…</span></div>
        ) : error ? (
          <div className="transaction-state error-state"><p>{error}</p><button className="secondary-action" onClick={() => void loadTransactions()}>Try again</button></div>
        ) : visibleTransactions.length === 0 ? (
          <div className="transaction-state empty-state"><WalletCards size={30} /><strong>No transactions found</strong><span>Try another filter or check back after your next transaction.</span></div>
        ) : (
          <div className="table-scroll">
            <table className="transactions-table">
              <thead><tr><th>Transaction</th><th>Ledger accounts</th><th>Amount</th><th>Status</th><th>Date</th></tr></thead>
              <tbody>
                {visibleTransactions.map((transaction) => {
                  const amount = transaction.entries[0]?.amount ?? '0';
                  const accountLabels = [...new Set(transaction.entries.map((entry) => entry.accountNumber))];
                  return (
                    <tr key={transaction.id}>
                      <td>
                        <div className="transaction-type-cell">
                          <span className={`transaction-type-icon ${transaction.type.toLowerCase()}`}>{transactionIcon(transaction.type)}</span>
                          <span><strong>{transaction.type.charAt(0) + transaction.type.slice(1).toLowerCase()}</strong><small>{transaction.description}</small></span>
                        </div>
                      </td>
                      <td><span className="account-number-list">{accountLabels.join(' → ')}</span><small className="entry-count">{transaction.entries.length} ledger entries</small></td>
                      <td className="amount-cell">{money(amount)}</td>
                      <td><span className={`status-pill ${transaction.status.toLowerCase()}`}>{transaction.status}</span></td>
                      <td className="date-cell">{dateTime(transaction.createdAt)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
      <p className="transaction-footnote">Each posted transaction is represented by ledger entries. This view is read-only.</p>
    </section>
  );
}
