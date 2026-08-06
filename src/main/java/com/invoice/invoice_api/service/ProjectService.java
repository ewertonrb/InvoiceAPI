package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.project.ProjectRequestDTO;
import com.invoice.invoice_api.dto.project.ProjectResponseDTO;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.ProjectMapper;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.Project;
import com.invoice.invoice_api.repository.CompanyRepository;
import com.invoice.invoice_api.repository.ProjectRepository;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final CompanyRepository companyRepository;
    private final CompanyContext companyContext;

    public ProjectService(
            ProjectRepository projectRepository,
            CompanyRepository companyRepository,
            CompanyContext companyContext
    ) {
        this.projectRepository = projectRepository;
        this.companyRepository = companyRepository;
        this.companyContext = companyContext;
    }

    @Transactional
    public ProjectResponseDTO create(
            ProjectRequestDTO request
    ) {
        requireProjectManager();
        Long companyId = companyContext.getCompanyId();

        String normalizedName =
                normalizeRequiredText(request.name());

        validateDuplicateName(
                normalizedName,
                companyId,
                null
        );

        Company company = findCurrentCompany(companyId);

        if (!Boolean.TRUE.equals(company.getActive())) {
            throw new AccessDeniedBusinessException(
                    "Projects cannot be created for an inactive company"
            );
        }

        Project project = new Project();

        project.setName(normalizedName);
        project.setCompany(company);
        project.setActive(true);

        Project savedProject =
                projectRepository.save(project);

        return ProjectMapper.toResponseDTO(savedProject);
    }

    @Transactional(readOnly = true)
    public ProjectResponseDTO findById(Long id) {
        Long companyId = companyContext.getCompanyId();

        Project project = findEntityByIdAndCompany(
                id,
                companyId
        );

        return ProjectMapper.toResponseDTO(project);
    }

    @Transactional(readOnly = true)
    public List<ProjectResponseDTO> findAll(
            boolean activeOnly
    ) {
        Long companyId = companyContext.getCompanyId();

        List<Project> projects;

        if (activeOnly) {
            projects = projectRepository
                    .findByCompanyIdAndActiveTrue(companyId);
        } else {
            projects = projectRepository
                    .findByCompanyId(companyId);
        }

        return projects
                .stream()
                .map(ProjectMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public ProjectResponseDTO update(
            Long id,
            ProjectRequestDTO request
    ) {
        requireProjectManager();
        Long companyId = companyContext.getCompanyId();

        Project project = findEntityByIdAndCompany(
                id,
                companyId
        );

        String normalizedName =
                normalizeRequiredText(request.name());

        validateDuplicateName(
                normalizedName,
                companyId,
                id
        );

        project.setName(normalizedName);

        Project updatedProject =
                projectRepository.save(project);

        return ProjectMapper.toResponseDTO(updatedProject);
    }

    @Transactional
    public void deactivate(Long id) {
        requireProjectManager();
        Long companyId = companyContext.getCompanyId();

        Project project = findEntityByIdAndCompany(
                id,
                companyId
        );

        project.setActive(false);

        projectRepository.save(project);
    }

    @Transactional
    public ProjectResponseDTO reactivate(Long id) {
        requireProjectManager();
        Long companyId = companyContext.getCompanyId();

        Project project = findEntityByIdAndCompany(
                id,
                companyId
        );

        project.setActive(true);

        return ProjectMapper.toResponseDTO(
                projectRepository.save(project)
        );
    }

    private Project findEntityByIdAndCompany(
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

    private Company findCurrentCompany(Long companyId) {
        return companyRepository
                .findById(companyId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Selected company was not found"
                        )
                );
    }

    private void validateDuplicateName(
            String name,
            Long companyId,
            Long currentProjectId
    ) {
        projectRepository
                .findByNameIgnoreCaseAndCompanyId(
                        name,
                        companyId
                )
                .filter(existingProject ->
                        currentProjectId == null
                                || !existingProject
                                .getId()
                                .equals(currentProjectId)
                )
                .ifPresent(existingProject -> {
                    throw new DuplicateResourceException(
                            "A project with this name already exists"
                    );
                });
    }

    private String normalizeRequiredText(String value) {
        return value.trim();
    }

    private void requireProjectManager() {
        CompanyRole role = companyContext.getRole();

        if (role != CompanyRole.OWNER
                && role != CompanyRole.ADMIN
                && role != CompanyRole.MANAGER) {
            throw new AccessDeniedBusinessException(
                    "Only company owners, administrators and managers can manage projects"
            );
        }
    }

}
