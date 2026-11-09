
package com.ledgerline.repository;

import com.ledgerline.entity.AccountOpeningRequest;
import com.ledgerline.enums.RequestStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountOpeningRequestRepository
        extends JpaRepository<AccountOpeningRequest, UUID> {

    List<AccountOpeningRequest> findByApplicant_IdOrderByRequestedAtDesc(
            UUID applicantId
    );

    List<AccountOpeningRequest> findByStatusOrderByRequestedAtAsc(
            RequestStatus status
    );

    boolean existsByApplicant_IdAndStatus(
            UUID applicantId,
            RequestStatus status
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT request
            FROM AccountOpeningRequest request
            WHERE request.id = :requestId
            """)
    Optional<AccountOpeningRequest> findByIdForUpdate(
            @Param("requestId") UUID requestId
    );
}
