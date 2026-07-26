package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    boolean existsByAbn(String abn);

    boolean existsByEmail(String email);

    List<Company> findByActiveTrue();
}
