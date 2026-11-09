package com.ledgerline.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditorService {

    private final JdbcTemplate jdbcTemplate;

    public AuditorService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Map<String, Object>> assignedAccounts(UUID auditorId) {
        return jdbcTemplate.queryForList("""
                SELECT a.id AS account_id,
                       a.account_number,
                       a.account_type,
                       a.currency,
                       a.status,
                       a.created_at,
                       u.full_name AS owner_name
                FROM auditor_account_access access
                JOIN accounts a ON a.id = access.account_id
                LEFT JOIN app_users u ON u.id = a.owner_user_id
                WHERE access.auditor_user_id = ?
                ORDER BY access.granted_at DESC
                """, auditorId);
    }

    public List<Map<String, Object>> accountTransactions(
            UUID auditorId,
            UUID accountId
    ) {
        requireAssignment(auditorId, accountId);

        return jdbcTemplate.queryForList("""
                SELECT id AS transaction_id,
                       transaction_type,
                       status,
                       amount_paise,
                       currency,
                       source_account_id,
                       destination_account_id,
                       initiated_by_user_id,
                       description,
                       created_at,
                       completed_at
                FROM money_transactions
                WHERE source_account_id = ?
                   OR destination_account_id = ?
                ORDER BY created_at DESC
                """, accountId, accountId);
    }

    public List<Map<String, Object>> accountAuditEvents(
            UUID auditorId,
            UUID accountId
    ) {
        requireAssignment(auditorId, accountId);

        return jdbcTemplate.queryForList("""
                SELECT id AS event_id,
                       actor_user_id,
                       action,
                       entity_type,
                       entity_id,
                       details,
                       created_at
                FROM audit_events
                WHERE entity_id IN (
                    SELECT id
                    FROM money_transactions
                    WHERE source_account_id = ?
                       OR destination_account_id = ?
                )
                OR details->>'accountId' = CAST(? AS text)
                ORDER BY created_at DESC
                """, accountId, accountId, accountId);
    }

    private void requireAssignment(UUID auditorId, UUID accountId) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM auditor_account_access
                WHERE auditor_user_id = ?
                  AND account_id = ?
                """, Integer.class, auditorId, accountId);

        if (count == null || count == 0) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "This account is not assigned to the auditor"
            );
        }
    }
}