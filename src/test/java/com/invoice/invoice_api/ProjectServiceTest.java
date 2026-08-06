package com.invoice.invoice_api;

import com.invoice.invoice_api.dto.project.ProjectRequestDTO;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.model.Company;
import com.invoice.invoice_api.model.Project;
import com.invoice.invoice_api.repository.CompanyRepository;
import com.invoice.invoice_api.repository.ProjectRepository;
import com.invoice.invoice_api.security.CompanyContext;
import com.invoice.invoice_api.service.ProjectService;
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
class ProjectServiceTest {

    @Mock ProjectRepository projectRepository;
    @Mock CompanyRepository companyRepository;
    @Mock CompanyContext companyContext;

    private ProjectService service;

    @BeforeEach
    void setUp() {
        service = new ProjectService(projectRepository, companyRepository, companyContext);
    }

    @Test
    void workerCannotCreateProject() {
        when(companyContext.getRole()).thenReturn(CompanyRole.WORKER);

        assertThrows(AccessDeniedBusinessException.class,
                () -> service.create(new ProjectRequestDTO("Restricted")));

        verify(projectRepository, never()).save(any());
    }

    @Test
    void managerCanCreateProjectForSelectedActiveCompany() {
        Company company = company(true);
        when(companyContext.getRole()).thenReturn(CompanyRole.MANAGER);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(companyRepository.findById(7L)).thenReturn(Optional.of(company));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(11L);
            return project;
        });

        var response = service.create(new ProjectRequestDTO("  Harbour  "));

        assertEquals("Harbour", response.name());
        assertEquals(7L, response.companyId());
    }

    @Test
    void projectCannotBeCreatedForInactiveCompany() {
        when(companyContext.getRole()).thenReturn(CompanyRole.OWNER);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(companyRepository.findById(7L)).thenReturn(Optional.of(company(false)));

        assertThrows(AccessDeniedBusinessException.class,
                () -> service.create(new ProjectRequestDTO("Closed")));

        verify(projectRepository, never()).save(any());
    }

    private Company company(boolean active) {
        Company company = new Company();
        company.setId(7L);
        company.setName("Acme");
        company.setActive(active);
        return company;
    }
}
