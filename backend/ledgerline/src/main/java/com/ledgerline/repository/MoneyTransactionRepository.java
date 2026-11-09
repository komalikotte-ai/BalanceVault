package com.ledgerline.repository;

import com.ledgerline.entity.MoneyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MoneyTransactionRepository
        extends JpaRepository<MoneyTransaction, UUID> {

    Optional<MoneyTransaction> findByInitiatedBy_IdAndIdempotencyKey(
            UUID userId,
            String idempotencyKey
    );

    @Query("""
        SELECT DISTINCT t
        FROM MoneyTransaction t
        WHERE t.sourceAccount.owner.id = :userId
           OR t.destinationAccount.owner.id = :userId
        ORDER BY t.createdAt DESC
        """)
    List<MoneyTransaction> findCustomerHistory(@Param("userId") UUID userId);
}
