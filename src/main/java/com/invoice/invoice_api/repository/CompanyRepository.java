package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.Company;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByAbn(String abn);

    boolean existsByEmail(String email);

    List<Company> findByActiveTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT company FROM Company company WHERE company.id = :companyId")
    Optional<Company> findByIdForUpdate(
            @Param("companyId") Long companyId
    );
}
