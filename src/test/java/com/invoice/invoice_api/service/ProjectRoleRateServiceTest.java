package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.projectRoleRate.ProjectRoleRateRequestDTO;
import com.invoice.invoice_api.dto.projectRoleRateItemRequestDTO.ProjectRoleRateItemRequestDTO;
import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.enums.RateCalculationType;
import com.invoice.invoice_api.enums.RateType;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.model.Project;
import com.invoice.invoice_api.model.ProjectPosition;
import com.invoice.invoice_api.model.ProjectRoleRate;
import com.invoice.invoice_api.model.ProjectRoleRateItem;
import com.invoice.invoice_api.repository.ProjectPositionRepository;
import com.invoice.invoice_api.repository.ProjectRoleRateRepository;
import com.invoice.invoice_api.security.CompanyContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectRoleRateServiceTest {
    @Mock ProjectRoleRateRepository rateRepository;
    @Mock ProjectPositionRepository positionRepository;
    @Mock CompanyContext companyContext;
    private ProjectRoleRateService service;

    @BeforeEach
    void setUp() {
        service = new ProjectRoleRateService(rateRepository, positionRepository, companyContext);
    }

    @Test
    void financeCannotCreateRates() {
        when(companyContext.getRole()).thenReturn(CompanyRole.FINANCE);

        assertThrows(AccessDeniedBusinessException.class, () -> service.create(validRequest()));

        verifyNoInteractions(rateRepository, positionRepository);
    }

    @Test
    void workerCannotDeactivateRates() {
        when(companyContext.getRole()).thenReturn(CompanyRole.WORKER);

        assertThrows(AccessDeniedBusinessException.class, () -> service.deactivate(44L));

        verifyNoInteractions(rateRepository);
    }

    @Test
    void detailLookupIsScopedToCurrentCompany() {
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(rateRepository.findByIdAndProjectPositionProjectCompanyId(44L, 7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.findById(44L));

        verify(rateRepository).findByIdAndProjectPositionProjectCompanyId(44L, 7L);
        verify(rateRepository, never()).findById(44L);
    }

    @Test
    void createCannotUsePositionOutsideCurrentCompany() {
        when(companyContext.getRole()).thenReturn(CompanyRole.MANAGER);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(positionRepository.findByIdAndCompanyIdForUpdate(9L, 7L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.create(validRequest()));

        verify(positionRepository).findByIdAndCompanyIdForUpdate(9L, 7L);
        verify(rateRepository, never()).save(any());
    }

    @Test
    void createRejectsInactivePositionBeforePersistence() {
        ProjectPosition position = mock(ProjectPosition.class);
        when(position.getActive()).thenReturn(false);
        when(companyContext.getRole()).thenReturn(CompanyRole.OWNER);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(positionRepository.findByIdAndCompanyIdForUpdate(9L, 7L)).thenReturn(Optional.of(position));

        assertThrows(AccessDeniedBusinessException.class, () -> service.create(validRequest()));

        verify(rateRepository, never()).save(any());
    }

    @Test
    void createRejectsInactiveProjectBeforePersistence() {
        ProjectPosition position = mock(ProjectPosition.class);
        Project project = mock(Project.class);
        when(position.getActive()).thenReturn(true);
        when(position.getProject()).thenReturn(project);
        when(project.getActive()).thenReturn(false);
        when(companyContext.getRole()).thenReturn(CompanyRole.ADMIN);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(positionRepository.findByIdAndCompanyIdForUpdate(9L, 7L)).thenReturn(Optional.of(position));

        assertThrows(AccessDeniedBusinessException.class, () -> service.create(validRequest()));

        verify(rateRepository, never()).save(any());
    }

    @Test
    void duplicateRateTypesAreRejectedBeforeRepositoryLookup() {
        when(companyContext.getRole()).thenReturn(CompanyRole.MANAGER);
        when(companyContext.getCompanyId()).thenReturn(7L);
        ProjectRoleRateItemRequestDTO regular = regularItem();
        ProjectRoleRateRequestDTO duplicate = new ProjectRoleRateRequestDTO(9L, LocalDate.of(2026, 8, 1), null, List.of(regular, regular));

        assertThrows(DuplicateResourceException.class, () -> service.create(duplicate));

        verifyNoInteractions(positionRepository, rateRepository);
    }

    @Test
    void createLocksPositionAndRejectsOverlappingPeriod() {
        ProjectPosition position = activePosition(9L);
        when(companyContext.getRole()).thenReturn(CompanyRole.MANAGER);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(positionRepository.findByIdAndCompanyIdForUpdate(9L, 7L)).thenReturn(Optional.of(position));
        when(rateRepository.existsOverlappingPeriod(9L, 7L, LocalDate.of(2026, 8, 1), null)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.create(validRequest()));

        verify(positionRepository).findByIdAndCompanyIdForUpdate(9L, 7L);
        verify(rateRepository).existsOverlappingPeriod(9L, 7L, LocalDate.of(2026, 8, 1), null);
        verify(rateRepository, never()).save(any());
    }

    @Test
    void updateLocksRateThenPositionsInStableIdOrderAndExcludesCurrentRateFromOverlap() {
        ProjectRoleRate rate = mock(ProjectRoleRate.class);
        ProjectPosition current = mock(ProjectPosition.class);
        ProjectPosition requested = activePosition(5L);
        when(rate.getProjectPosition()).thenReturn(current);
        when(current.getId()).thenReturn(12L);
        when(companyContext.getRole()).thenReturn(CompanyRole.OWNER);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(rateRepository.findByIdAndCompanyIdForUpdate(44L, 7L)).thenReturn(Optional.of(rate));
        when(positionRepository.findByIdAndCompanyIdForUpdate(5L, 7L)).thenReturn(Optional.of(requested));
        when(positionRepository.findByIdAndCompanyIdForUpdate(12L, 7L)).thenReturn(Optional.of(current));
        when(rateRepository.existsOverlappingPeriodExcludingId(5L, 7L, LocalDate.of(2026, 8, 1), null, 44L)).thenReturn(true);
        ProjectRoleRateRequestDTO moving = new ProjectRoleRateRequestDTO(5L, LocalDate.of(2026, 8, 1), null, List.of(regularItem()));

        assertThrows(DuplicateResourceException.class, () -> service.update(44L, moving));

        var order = inOrder(rateRepository, positionRepository);
        order.verify(rateRepository).findByIdAndCompanyIdForUpdate(44L, 7L);
        order.verify(positionRepository).findByIdAndCompanyIdForUpdate(5L, 7L);
        order.verify(positionRepository).findByIdAndCompanyIdForUpdate(12L, 7L);
        verify(rateRepository).existsOverlappingPeriodExcludingId(5L, 7L, LocalDate.of(2026, 8, 1), null, 44L);
    }

    @Test
    void reactivateLocksRateAndPositionAndRejectsOverlap() {
        ProjectRoleRate rate = mock(ProjectRoleRate.class);
        ProjectPosition position = activePosition(9L);
        LocalDate from = LocalDate.of(2026, 8, 1);
        when(rate.getProjectPosition()).thenReturn(position);
        when(rate.getId()).thenReturn(44L);
        when(rate.getEffectiveFrom()).thenReturn(from);
        when(rate.getEffectiveTo()).thenReturn(null);
        when(companyContext.getRole()).thenReturn(CompanyRole.ADMIN);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(rateRepository.findByIdAndCompanyIdForUpdate(44L, 7L)).thenReturn(Optional.of(rate));
        when(positionRepository.findByIdAndCompanyIdForUpdate(9L, 7L)).thenReturn(Optional.of(position));
        when(rateRepository.existsOverlappingPeriodExcludingId(9L, 7L, from, null, 44L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> service.reactivate(44L));

        verify(rateRepository).findByIdAndCompanyIdForUpdate(44L, 7L);
        verify(positionRepository).findByIdAndCompanyIdForUpdate(9L, 7L);
        verify(rateRepository).existsOverlappingPeriodExcludingId(9L, 7L, from, null, 44L);
        verify(rate, never()).setActive(true);
    }

    @Test
    void deactivateUsesCompanyScopedLocks() {
        ProjectRoleRate rate = mock(ProjectRoleRate.class);
        ProjectPosition position = mock(ProjectPosition.class);
        when(rate.getProjectPosition()).thenReturn(position);
        when(position.getId()).thenReturn(9L);
        when(companyContext.getRole()).thenReturn(CompanyRole.MANAGER);
        when(companyContext.getCompanyId()).thenReturn(7L);
        when(rateRepository.findByIdAndCompanyIdForUpdate(44L, 7L)).thenReturn(Optional.of(rate));
        when(positionRepository.findByIdAndCompanyIdForUpdate(9L, 7L)).thenReturn(Optional.of(position));

        service.deactivate(44L);

        verify(rateRepository).findByIdAndCompanyIdForUpdate(44L, 7L);
        verify(positionRepository).findByIdAndCompanyIdForUpdate(9L, 7L);
        verify(rate).setActive(false);
    }

    @Test
    void replacingItemsUpdatesExistingRateTypeInsteadOfReinsertingIt()
            throws Exception {
        ProjectRoleRate rate = new ProjectRoleRate();
        ProjectRoleRateItem existing = new ProjectRoleRateItem();
        existing.setRateType(RateType.KILOMETRE);
        existing.setCalculationType(RateCalculationType.FIXED_RATE);
        existing.setValue(new BigDecimal("0.8500"));
        existing.setDescription("Old value");
        rate.addItem(existing);

        ProjectRoleRateItemRequestDTO replacement =
                new ProjectRoleRateItemRequestDTO(
                        RateType.KILOMETRE,
                        RateCalculationType.FIXED_RATE,
                        new BigDecimal("0.9500"),
                        "Updated value"
                );

        Method replaceItems = ProjectRoleRateService.class
                .getDeclaredMethod(
                        "replaceItems",
                        ProjectRoleRate.class,
                        List.class
                );
        replaceItems.setAccessible(true);
        replaceItems.invoke(service, rate, List.of(replacement));

        assertEquals(1, rate.getItems().size());
        assertSame(existing, rate.getItems().getFirst());
        assertEquals(
                new BigDecimal("0.9500"),
                existing.getValue()
        );
        assertEquals("Updated value", existing.getDescription());
    }

    private ProjectRoleRateRequestDTO validRequest() {
        return new ProjectRoleRateRequestDTO(9L, LocalDate.of(2026, 8, 1), null, List.of(regularItem()));
    }

    private ProjectRoleRateItemRequestDTO regularItem() {
        return new ProjectRoleRateItemRequestDTO(RateType.REGULAR, RateCalculationType.BASE_RATE, new BigDecimal("42.50"), "Base rate");
    }

    private ProjectPosition activePosition(Long id) {
        ProjectPosition position = mock(ProjectPosition.class);
        Project project = mock(Project.class);
        when(position.getId()).thenReturn(id);
        when(position.getActive()).thenReturn(true);
        when(position.getProject()).thenReturn(project);
        when(project.getActive()).thenReturn(true);
        return position;
    }
}
