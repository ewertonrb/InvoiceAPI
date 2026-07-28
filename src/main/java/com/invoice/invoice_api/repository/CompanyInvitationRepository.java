package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.enums.InvitationStatus;
import com.invoice.invoice_api.model.CompanyInvitation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyInvitationRepository
        extends JpaRepository<CompanyInvitation, Long> {

    Optional<CompanyInvitation> findByTokenHash(
            String tokenHash
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
}