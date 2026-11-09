package com.ledgerline.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class AuditService {

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public AuditService(
            JdbcTemplate jdbcTemplate,
            ObjectMapper objectMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void record(
            UUID actorId,
            String action,
            String entityType,
            UUID entityId,
            Map<String, Object> details
    ) {
        try {
            String json = objectMapper.writeValueAsString(details);

            jdbcTemplate.update(
                    """
                    INSERT INTO audit_events
                        (actor_user_id, action, entity_type, entity_id, details)
                    VALUES (?, ?, ?, ?, CAST(? AS jsonb))
                    """,
                    actorId,
                    action,
                    entityType,
                    entityId,
                    json
            );
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Could not serialize audit event details", ex
            );
        }
    }
}
