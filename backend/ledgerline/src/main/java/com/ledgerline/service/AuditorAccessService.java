package com.ledgerline.service;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuditorAccessService {

    private final JdbcTemplate jdbcTemplate;
    private final AuditService auditService;

    public AuditorAccessService(
            JdbcTemplate jdbcTemplate,
            AuditService auditService
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
    }

    @Transactional
    public Map<String, Object> grantAccess(
            UUID adminId,
            UUID auditorId,
            UUID accountId
    ) {
        Map<String, Object> auditor = jdbcTemplate.queryForMap("""
                SELECT role, status
                FROM app_users
                WHERE id = ?
                """, auditorId);

        if (!"AUDITOR".equals(auditor.get("role"))
                || !"ACTIVE".equals(auditor.get("status"))) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The selected user is not an active auditor"
            );
        }

        Integer accountCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM accounts WHERE id = ?
                """, Integer.class, accountId);

        if (accountCount == null || accountCount == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Account not found"
            );
        }

        int inserted = jdbcTemplate.update("""
                INSERT INTO auditor_account_access
                    (auditor_user_id, account_id, granted_by_user_id)
                VALUES (?, ?, ?)
                ON CONFLICT (auditor_user_id, account_id) DO NOTHING
                """, auditorId, accountId, adminId);

        if (inserted == 1) {
            Map<String, Object> details = new LinkedHashMap<>();
            details.put("auditorUserId", auditorId);
            details.put("accountId", accountId);

            auditService.record(
                    adminId,
                    "AUDITOR_ACCOUNT_ACCESS_GRANTED",
                    "AUDITOR_ACCOUNT_ACCESS",
                    accountId,
                    details
            );
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("auditorId", auditorId);
        response.put("accountId", accountId);
        response.put("created", inserted == 1);
        response.put("message", inserted == 1
                ? "Auditor access granted"
                : "Auditor already has access");

        return response;
    }

    @Transactional
    public Map<String, Object> revokeAccess(
            UUID adminId,
            UUID auditorId,
            UUID accountId
    ) {
        int deleted = jdbcTemplate.update("""
                DELETE FROM auditor_account_access
                WHERE auditor_user_id = ?
                  AND account_id = ?
                """, auditorId, accountId);

        if (deleted == 0) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Auditor account assignment not found"
            );
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("auditorUserId", auditorId);
        details.put("accountId", accountId);

        auditService.record(
                adminId,
                "AUDITOR_ACCOUNT_ACCESS_REVOKED",
                "AUDITOR_ACCOUNT_ACCESS",
                accountId,
                details
        );

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("auditorId", auditorId);
        response.put("accountId", accountId);
        response.put("revoked", true);
        response.put("message", "Auditor access revoked");

        return response;
    }
}