package com.ledgerline.repository;

import com.ledgerline.entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    List<LedgerEntry> findByTransaction_IdOrderByCreatedAtAsc(UUID transactionId);

    List<LedgerEntry> findByAccount_IdOrderByCreatedAtDesc(UUID accountId);
}
