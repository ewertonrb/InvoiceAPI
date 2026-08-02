package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.enums.JoinLinkStatus;
import com.invoice.invoice_api.model.CompanyJoinLink;
import org.springframework.data.jpa.repository.JpaRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyJoinLinkRepository extends JpaRepository<CompanyJoinLink, Long> {

    Optional<CompanyJoinLink> findByTokenHash(String tokenHash);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT joinLink
        FROM CompanyJoinLink joinLink
        JOIN FETCH joinLink.company
        WHERE joinLink.tokenHash = :tokenHash
       """)
    Optional<CompanyJoinLink> findByTokenHashForUpdate(@Param("tokenHash") String tokenHash);

    List<CompanyJoinLink> findByCompanyIdOrderByCreatedAtDesc(Long companyId);

    List<CompanyJoinLink> findByCompanyIdAndStatusOrderByCreatedAtDesc(Long companyId, JoinLinkStatus status);


}
