package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.enums.MembershipStatus;
import com.invoice.invoice_api.model.CompanyMembership;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyMembershipRepository extends JpaRepository<CompanyMembership, Long> {

    Optional<CompanyMembership> findByAppUserIdAndCompanyId(
            Long appUserId,
            Long companyId
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT membership
            FROM CompanyMembership membership
            WHERE membership.id = :membershipId
              AND membership.company.id = :companyId
            """)
    Optional<CompanyMembership> findByIdAndCompanyIdForUpdate(
            @Param("membershipId") Long membershipId,
            @Param("companyId") Long companyId
    );

    List<CompanyMembership> findByAppUserId(Long appUserId);

    List<CompanyMembership> findByCompanyId(Long companyId);

    List<CompanyMembership> findByCompanyIdAndStatus(Long companyId, MembershipStatus status);

    List<CompanyMembership> findByCompanyIdAndStatusIn(
            Long companyId,
            List<MembershipStatus> statuses
    );
    @EntityGraph(attributePaths = "company")
    List<CompanyMembership> findByAppUserIdAndStatus(
            Long appUserId,
            MembershipStatus status
    );

    @EntityGraph(attributePaths = "company")
    Optional<CompanyMembership> findByAppUserIdAndCompanyIdAndStatus(
            Long appUserId,
            Long companyId,
            MembershipStatus status
    );
}
