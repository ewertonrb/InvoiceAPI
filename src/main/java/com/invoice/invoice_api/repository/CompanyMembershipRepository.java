package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.model.CompanyMembership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {

    Optional<CompanyMembership> findByAppUserIdAndCompanyId(
            Long appUserId,
            Long companyId
    );

    List<CompanyMembership> findByAppUserId(Long appUserId);

    List<CompanyMembership> findByCompanyId(Long companyId);

    List<CompanyMembership> findByCompanyIdAndStatus(Long companyId, MembershipStatus status);

    List<CompanyMembership> findByCompanyIdAndStatusIn(
            Long companyId,
            List<MembershipStatus> statuses
    );
    List<CompanyMembership> findByAppUserIdAndStatus(
            Long appUserId,
            MembershipStatus status
    );
}
