package com.invoice.invoice_api;

import com.invoice.invoice_api.dto.projectPosition.ProjectPositionRequestDTO;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.Project;
import com.invoice.invoice_api.model.ProjectPosition;
import com.invoice.invoice_api.repository.ProjectPositionRepository;
import com.invoice.invoice_api.repository.ProjectRepository;
import com.invoice.invoice_api.security.CompanyContext;
import com.invoice.invoice_api.service.ProjectPositionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProjectPositionServiceTest {
    @Mock ProjectPositionRepository positionRepository;
    @Mock ProjectRepository projectRepository;
    @Mock CompanyContext companyContext;
    private ProjectPositionService service;

    @BeforeEach
    void setUp() {
        service = new ProjectPositionService(positionRepository, projectRepository, companyContext);
    }

    @Test
    void financeCannotCreatePosition() {
        when(companyContext.getRole()).thenReturn(CompanyRole.FINANCE);
        assertThrows(AccessDeniedBusinessException.class,
                () -> service.create(new ProjectPositionRequestDTO(5L, "Installer")));
        verify(positionRepository, never()).save(any());
    }

    @Test
    void managerCanCreatePositionInSelectedActiveProject() {
        Project project = project(true);
        when(companyContext.getRole()).thenReturn(CompanyRole.MANAGER);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(projectRepository.findByIdAndCompanyId(5L, 7L)).thenReturn(Optional.of(project));
        when(positionRepository.save(any(ProjectPosition.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        var response = service.create(new ProjectPositionRequestDTO(5L, "  Senior   Installer "));
        assertEquals("Senior Installer", response.positionName());
        assertEquals(7L, response.companyId());
    }

    @Test
    void cannotReactivatePositionUnderInactiveProject() {
        ProjectPosition position = new ProjectPosition();
        position.setProject(project(false));
        position.setActive(false);
        when(companyContext.getRole()).thenReturn(CompanyRole.OWNER);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(positionRepository.findByIdAndProjectCompanyId(9L, 7L)).thenReturn(Optional.of(position));

        assertThrows(AccessDeniedBusinessException.class, () -> service.reactivate(9L));
    }

    private Project project(boolean active) {
        Company company = new Company();
        company.setId(7L);
        company.setName("Acme");
        Project project = new Project();
        project.setId(5L);
        project.setName("Harbour");
        project.setCompany(company);
        project.setActive(active);
        return project;
    }
}
