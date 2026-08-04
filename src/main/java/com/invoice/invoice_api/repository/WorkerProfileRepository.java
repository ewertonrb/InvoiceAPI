package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.CompanyMembership;
import com.invoice.invoice_api.model.WorkerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WorkerProfileRepository extends JpaRepository<WorkerProfile, Long> {

    Optional<WorkerProfile> findByAppUserId(
            Long appUserId
    );

    Optional<WorkerProfile> findByIdAndAppUserId(
            Long workerProfileId,
            Long appUserId
    );

    boolean existsByAppUserId(
            Long appUserId
    );

    boolean existsByAbn(
            String abn
    );

    boolean existsByAbnAndIdNot(
            String abn,
            Long workerProfileId
    );

    @Query("""
            SELECT workerProfile
            FROM WorkerProfile workerProfile
            JOIN CompanyMembership membership
                ON membership.appUser.id = workerProfile.appUser.id
            WHERE membership.company.id = :companyId
              AND membership.role = com.invoice.invoice_api.enums.CompanyRole.WORKER
              AND membership.status = com.invoice.invoice_api.enums.MembershipStatus.ACTIVE
            ORDER BY workerProfile.appUser.name ASC,
                     workerProfile.appUser.surname ASC
            """)
    List<WorkerProfile> findActiveWorkersByCompanyId(
            @Param("companyId") Long companyId
    );
}
