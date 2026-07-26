package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectRepository extends JpaRepository<Project,Long> {

    List<Project> findByCompanyId(Long companyId);

    List<Project> findByCompanyIdAndActiveTrue(Long companyId);

    Optional<Project> findByIdAndCompanyId(Long id, Long companyId);

    Optional<Project> findByNameIgnoreCaseAndCompanyId(String name, Long companyId);

    boolean existsByNameIgnoreCaseAndCompanyId(
            String name,
            Long companyId
    );

}
