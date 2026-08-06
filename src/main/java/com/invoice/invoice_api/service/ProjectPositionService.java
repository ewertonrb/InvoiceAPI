package com.invoice.invoice_api.service;


import com.invoice.invoice_api.dto.projectPosition.ProjectPositionRequestDTO;
import com.invoice.invoice_api.dto.projectPosition.ProjectPositionResponseDTO;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.ProjectPositionMapper;
import com.invoice.invoice_api.model.Project;
import com.invoice.invoice_api.model.ProjectPosition;
import com.invoice.invoice_api.repository.ProjectPositionRepository;
import com.invoice.invoice_api.repository.ProjectRepository;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectPositionService {

    final ProjectPositionRepository projectPositionRepository;
    private final ProjectRepository projectRepository;
    private final CompanyContext companyContext;

    public ProjectPositionService(
            ProjectPositionRepository projectPositionRepository,
            ProjectRepository projectRepository,
            CompanyContext companyContext
    ) {
        this.projectPositionRepository = projectPositionRepository;
        this.projectRepository = projectRepository;
        this.companyContext = companyContext;
    }

    @Transactional
    public ProjectPositionResponseDTO create(
            ProjectPositionRequestDTO request
    ) {
        requirePositionManager();
        Long companyId = getCurrentCompanyId();

        Project project = findProjectInCurrentCompany(
                request.projectId(),
                companyId
        );
        requireActiveProject(project);

        String normalizedPositionName =
                normalizeRequiredText(request.positionName());

        validateDuplicatePositionName(
                normalizedPositionName,
                project.getId(),
                companyId,
                null
        );

        ProjectPosition position = new ProjectPosition();

        position.setPositionName(normalizedPositionName);
        position.setProject(project);
        position.setActive(true);

        ProjectPosition savedPosition =
                projectPositionRepository.save(position);

        return ProjectPositionMapper.toResponseDTO(savedPosition);
    }

    @Transactional(readOnly = true)
    public ProjectPositionResponseDTO findById(Long id) {
        Long companyId = getCurrentCompanyId();

        ProjectPosition position =
                findEntityByIdAndCompany(id, companyId);

        return ProjectPositionMapper.toResponseDTO(position);
    }

    @Transactional(readOnly = true)
    public List<ProjectPositionResponseDTO> findByProject(
            Long projectId,
            boolean activeOnly
    ) {
        Long companyId = getCurrentCompanyId();

        findProjectInCurrentCompany(projectId, companyId);

        List<ProjectPosition> positions;

        if (activeOnly) {
            positions = projectPositionRepository
                    .findAllByProjectIdAndProjectCompanyIdAndActiveTrueAndProjectActiveTrueOrderByPositionNameAsc(
                            projectId,
                            companyId
                    );
        } else {
            positions = projectPositionRepository
                    .findAllByProjectIdAndProjectCompanyIdOrderByPositionNameAsc(
                            projectId,
                            companyId
                    );
        }

        return positions.stream()
                .map(ProjectPositionMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public ProjectPositionResponseDTO update(
            Long id,
            ProjectPositionRequestDTO request
    ) {
        requirePositionManager();
        Long companyId = getCurrentCompanyId();

        ProjectPosition position =
                findEntityByIdAndCompany(id, companyId);

        Project project = findProjectInCurrentCompany(
                request.projectId(),
                companyId
        );
        requireActiveProject(project);

        String normalizedPositionName =
                normalizeRequiredText(request.positionName());

        validateDuplicatePositionName(
                normalizedPositionName,
                project.getId(),
                companyId,
                id
        );

        position.setPositionName(normalizedPositionName);
        position.setProject(project);

        ProjectPosition updatedPosition =
                projectPositionRepository.save(position);

        return ProjectPositionMapper.toResponseDTO(updatedPosition);
    }

    @Transactional
    public void deactivate(Long id) {
        requirePositionManager();
        Long companyId = getCurrentCompanyId();

        ProjectPosition position =
                findEntityByIdAndCompany(id, companyId);

        position.setActive(false);
    }

    @Transactional
    public ProjectPositionResponseDTO reactivate(Long id) {
        requirePositionManager();
        Long companyId = getCurrentCompanyId();

        ProjectPosition position =
                findEntityByIdAndCompany(id, companyId);
        requireActiveProject(position.getProject());

        position.setActive(true);

        return ProjectPositionMapper.toResponseDTO(position);
    }

    private ProjectPosition findEntityByIdAndCompany(
            Long positionId,
            Long companyId
    ) {
        return projectPositionRepository
                .findByIdAndProjectCompanyId(
                        positionId,
                        companyId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project position not found with ID: "
                                        + positionId
                        )
                );
    }

    private Project findProjectInCurrentCompany(
            Long projectId,
            Long companyId
    ) {
        return projectRepository
                .findByIdAndCompanyId(
                        projectId,
                        companyId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project not found with ID: "
                                        + projectId
                        )
                );
    }

    private void validateDuplicatePositionName(
            String positionName,
            Long projectId,
            Long companyId,
            Long currentPositionId
    ) {
        boolean duplicateExists;

        if (currentPositionId == null) {
            duplicateExists = projectPositionRepository
                    .existsByProjectIdAndPositionNameIgnoreCaseAndProjectCompanyId(
                            projectId,
                            positionName,
                            companyId
                    );
        } else {
            duplicateExists = projectPositionRepository
                    .existsByProjectIdAndPositionNameIgnoreCaseAndProjectCompanyIdAndIdNot(
                            projectId,
                            positionName,
                            companyId,
                            currentPositionId
                    );
        }

        if (duplicateExists) {
            throw new DuplicateResourceException(
                    "A project position with this name already exists"
            );
        }
    }

    private String normalizeRequiredText(String value) {
        return value.trim().replaceAll("\\s+", " ");
    }

    private Long getCurrentCompanyId() {
        return companyContext.getCompanyId();
    }

    private void requirePositionManager() {
        CompanyRole role = companyContext.getRole();

        if (role != CompanyRole.OWNER
                && role != CompanyRole.ADMIN
                && role != CompanyRole.MANAGER) {
            throw new AccessDeniedBusinessException(
                    "Only company owners, administrators and managers can manage project positions"
            );
        }
    }

    private void requireActiveProject(Project project) {
        if (!Boolean.TRUE.equals(project.getActive())) {
            throw new AccessDeniedBusinessException(
                    "Project positions cannot be assigned to an inactive project"
            );
        }
    }

    @Transactional(readOnly = true)
    public List<ProjectPositionResponseDTO> findAllByCurrentCompany(
            boolean activeOnly
    ) {
        Long companyId = getCurrentCompanyId();

        List<ProjectPosition> positions;

        if (activeOnly) {
            positions = projectPositionRepository
                    .findAllByProjectCompanyIdAndActiveTrueAndProjectActiveTrueOrderByPositionNameAsc(
                            companyId
                    );
        } else {
            positions = projectPositionRepository
                    .findAllByProjectCompanyIdOrderByPositionNameAsc(
                            companyId
                    );
        }

        return positions.stream()
                .map(ProjectPositionMapper::toResponseDTO)
                .toList();
    }
}
