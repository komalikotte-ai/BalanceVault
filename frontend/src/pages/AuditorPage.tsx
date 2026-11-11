import { useCallback, useEffect, useState } from 'react';
import { Activity, BookOpenCheck, RefreshCw, ShieldCheck } from 'lucide-react';
import { useAuth } from '../context/AuthContext.tsx';

type AuditLog = {
  id: string;
  action: string;
  actorId: string | null;
  actorRole: string | null;
  targetResource: string;
  targetId: string | null;
  metadata: unknown;
  createdAt: string;
  actor?: { fullName: string; email: string } | null;
};

const formatDate = (value: string) => new Intl.DateTimeFormat(undefined, { dateStyle: 'medium', timeStyle: 'short' }).format(new Date(value));

export function AuditorPage() {
  const { token, user } = useAuth();
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  const load = useCallback(async () => {
    if (!token) { setLoading(false); return; }
    setLoading(true);
    setError('');
    try {
      const response = await fetch('/api/v1/auditor/audit-logs?limit=200', { headers: { Authorization: `Bearer ${token}` } });
      const payload = await response.json();
      if (!response.ok || !payload.success) throw new Error(payload.error?.message || 'Could not load audit events.');
      setLogs(Array.isArray(payload.data) ? payload.data : []);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Could not load audit events.');
    } finally {
      setLoading(false);
    }
  }, [token]);

  useEffect(() => { void load(); }, [load]);

  if (!user) {
    return <section className="auditor-page"><div className="transaction-state"><strong>Sign in required</strong><span>Sign in with an Auditor or Admin account to view compliance events.</span></div></section>;
  }

  if (user.role !== 'AUDITOR' && user.role !== 'ADMIN') {
    return <section className="auditor-page"><div className="transaction-state error-state"><strong>Access denied</strong><span>This workspace is restricted to Auditor and Admin roles.</span></div></section>;
  }

  return (
    <section className="auditor-page">
      <div className="auditor-heading">
        <div>
          <p className="eyebrow">COMPLIANCE WORKSPACE</p>
          <h1>Auditor dashboard</h1>
          <p className="page-description">Read-only view of recorded administrative, security and financial events.</p>
        </div>
        <span className="auditor-readonly"><ShieldCheck size={15} /> Read-only access</span>
      </div>
      <div className="auditor-stats">
        <article><span><Activity size={16}/> Events loaded</span><strong>{logs.length}</strong><small>Latest audit records</small></article>
        <article><span><BookOpenCheck size={16}/> Signed in as</span><strong>{user?.fullName || 'Auditor'}</strong><small>{user?.email}</small></article>
      </div>
      <div className="auditor-toolbar">
        <div><h2>Audit event trail</h2><p>Events are displayed newest first. This workspace does not provide mutation controls.</p></div>
        <button className="secondary-action" onClick={() => void load()} disabled={loading}><RefreshCw size={16} className={loading ? 'spin' : ''}/> Refresh</button>
      </div>
      <div className="auditor-table-card">
        {loading ? <div className="transaction-state"><RefreshCw className="spin"/><span>Loading audit events…</span></div>
          : error ? <div className="transaction-state error-state"><p>{error}</p><button className="secondary-action" onClick={() => void load()}>Try again</button></div>
          : logs.length === 0 ? <div className="transaction-state empty-state"><BookOpenCheck size={28}/><strong>No audit events recorded</strong><span>Events will appear here as platform actions occur.</span></div>
          : <div className="table-scroll"><table className="transactions-table"><thead><tr><th>Action</th><th>Actor</th><th>Resource</th><th>Reference</th><th>Timestamp</th></tr></thead><tbody>
            {logs.map((log) => <tr key={log.id}><td><strong>{log.action.replace(/_/g, ' ')}</strong><small className="entry-count">{log.actorRole || 'SYSTEM'}</small></td><td>{log.actor?.fullName || 'System'}<small className="entry-count">{log.actor?.email || log.actorId || 'Automated event'}</small></td><td>{log.targetResource}</td><td className="account-number-list">{log.targetId || '—'}</td><td className="date-cell">{formatDate(log.createdAt)}</td></tr>)}
          </tbody></table></div>}
      </div>
      <p className="transaction-footnote">Showing up to 200 most recent audit records. Sensitive credentials and raw identity documents are not displayed.</p>
    </section>
  );
}
