package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.enums.InvitationStatus;
import com.invoice.invoice_api.model.CompanyInvitation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyInvitationRepository
        extends JpaRepository<CompanyInvitation, Long> {

    Optional<CompanyInvitation> findByTokenHash(
            String tokenHash
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT invitation
            FROM CompanyInvitation invitation
            WHERE invitation.tokenHash = :tokenHash
            """)
    Optional<CompanyInvitation> findByTokenHashForUpdate(
            @Param("tokenHash") String tokenHash
    );

    @Query(
            value = "SELECT pg_advisory_xact_lock(" +
                    "hashtextextended(lower(:email), 0))",
            nativeQuery = true
    )
    Object acquireInvitationEmailLock(
            @Param("email") String email
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<CompanyInvitation> findByIdAndCompanyId(
            Long invitationId,
            Long companyId
    );

    Optional<CompanyInvitation>
    findByCompanyIdAndEmailIgnoreCaseAndStatus(
            Long companyId,
            String email,
            InvitationStatus status
    );

    boolean existsByCompanyIdAndEmailIgnoreCaseAndStatus(
            Long companyId,
            String email,
            InvitationStatus status
    );

    List<CompanyInvitation> findByCompanyId(
            Long companyId
    );

    List<CompanyInvitation> findByCompanyIdAndStatus(
            Long companyId,
            InvitationStatus status
    );
    List<CompanyInvitation>
    findByCompanyIdOrderByCreatedAtDesc(
            Long companyId
    );

    List<CompanyInvitation>
    findByCompanyIdAndStatusOrderByCreatedAtDesc(
            Long companyId,
            InvitationStatus status
    );

}
