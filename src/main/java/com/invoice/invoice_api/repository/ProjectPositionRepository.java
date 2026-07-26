package com.invoice.invoice_api.repository;

import com.invoice.invoice_api.model.ProjectPosition;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProjectPositionRepository extends JpaRepository<ProjectPosition, Long> {

    Optional<ProjectPosition>
    findByIdAndProjectCompanyId(
            Long projectPositionId,
            Long companyId
    );

    List<ProjectPosition>
    findAllByProjectIdAndProjectCompanyIdOrderByPositionNameAsc(
            Long projectId,
            Long companyId
    );

    List<ProjectPosition>
    findAllByProjectIdAndProjectCompanyIdAndActiveTrueOrderByPositionNameAsc(
            Long projectId,
            Long companyId
    );

    List<ProjectPosition>
    findAllByProjectCompanyIdOrderByPositionNameAsc(
            Long companyId
    );

    List<ProjectPosition>
    findAllByProjectCompanyIdAndActiveTrueOrderByPositionNameAsc(
            Long companyId
    );

    boolean existsByProjectIdAndPositionNameIgnoreCaseAndProjectCompanyId(
            Long projectId,
            String positionName,
            Long companyId
    );

    boolean existsByProjectIdAndPositionNameIgnoreCaseAndProjectCompanyIdAndIdNot(
            Long projectId,
            String positionName,
            Long companyId,
            Long id
    );
}
