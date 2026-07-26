package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.projectRoleRate.ProjectRoleRateRequestDTO;
import com.invoice.invoice_api.dto.projectRoleRate.ProjectRoleRateResponseDTO;
import com.invoice.invoice_api.dto.projectRoleRateItemRequestDTO.ProjectRoleRateItemRequestDTO;
import com.invoice.invoice_api.enums.RateCalculationType;
import com.invoice.invoice_api.enums.RateType;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.ProjectRoleRateMapper;
import com.invoice.invoice_api.model.ProjectPosition;
import com.invoice.invoice_api.model.ProjectRoleRate;
import com.invoice.invoice_api.model.ProjectRoleRateItem;
import com.invoice.invoice_api.repository.ProjectPositionRepository;
import com.invoice.invoice_api.repository.ProjectRoleRateRepository;
import com.invoice.invoice_api.security.CompanyContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ProjectRoleRateService {
    private final ProjectRoleRateRepository rateRepository;
    private final ProjectPositionRepository positionRepository;
    private final CompanyContext companyContext;

    public ProjectRoleRateService(
            ProjectRoleRateRepository rateRepository,
            ProjectPositionRepository positionRepository,
            CompanyContext companyContext
    ) {
        this.rateRepository = rateRepository;
        this.positionRepository = positionRepository;
        this.companyContext = companyContext;
    }

    @Transactional
    public ProjectRoleRateResponseDTO create(
            ProjectRoleRateRequestDTO request
    ) {
        Long companyId = getCurrentCompanyId();

        validateRequest(request);

        ProjectPosition position =
                findPositionInCurrentCompany(
                        request.projectPositionId(),
                        companyId
                );

        validateOverlappingPeriod(
                position.getId(),
                request.effectiveFrom(),
                request.effectiveTo(),
                null,
                companyId
        );

        ProjectRoleRate rate = new ProjectRoleRate();

        rate.setProjectPosition(position);
        rate.setEffectiveFrom(request.effectiveFrom());
        rate.setEffectiveTo(request.effectiveTo());
        rate.setActive(true);

        replaceItems(rate, request.items());

        ProjectRoleRate savedRate =
                rateRepository.save(rate);

        return ProjectRoleRateMapper.toResponseDTO(
                savedRate
        );
    }

    @Transactional(readOnly = true)
    public ProjectRoleRateResponseDTO findById(Long id) {
        Long companyId = getCurrentCompanyId();

        ProjectRoleRate rate =
                findEntityByIdAndCompany(
                        id,
                        companyId
                );

        return ProjectRoleRateMapper.toResponseDTO(rate);
    }

    @Transactional(readOnly = true)
    public List<ProjectRoleRateResponseDTO> findByPosition(
            Long projectPositionId
    ) {
        Long companyId = getCurrentCompanyId();

        findPositionInCurrentCompany(
                projectPositionId,
                companyId
        );

        return rateRepository
                .findAllByProjectPositionIdAndProjectPositionProjectCompanyIdOrderByEffectiveFromDesc(
                        projectPositionId,
                        companyId
                )
                .stream()
                .map(ProjectRoleRateMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public ProjectRoleRateResponseDTO update(
            Long id,
            ProjectRoleRateRequestDTO request
    ) {
        Long companyId = getCurrentCompanyId();

        validateRequest(request);

        ProjectRoleRate rate =
                findEntityByIdAndCompany(
                        id,
                        companyId
                );

        ProjectPosition position =
                findPositionInCurrentCompany(
                        request.projectPositionId(),
                        companyId
                );

        validateOverlappingPeriod(
                position.getId(),
                request.effectiveFrom(),
                request.effectiveTo(),
                id,
                companyId
        );

        rate.setProjectPosition(position);
        rate.setEffectiveFrom(request.effectiveFrom());
        rate.setEffectiveTo(request.effectiveTo());

        replaceItems(rate, request.items());

        return ProjectRoleRateMapper.toResponseDTO(rate);
    }

    @Transactional
    public void deactivate(Long id) {
        Long companyId = getCurrentCompanyId();

        ProjectRoleRate rate =
                findEntityByIdAndCompany(
                        id,
                        companyId
                );

        rate.setActive(false);
    }

    @Transactional
    public ProjectRoleRateResponseDTO reactivate(Long id) {
        Long companyId = getCurrentCompanyId();

        ProjectRoleRate rate =
                findEntityByIdAndCompany(
                        id,
                        companyId
                );

        validateOverlappingPeriod(
                rate.getProjectPosition().getId(),
                rate.getEffectiveFrom(),
                rate.getEffectiveTo(),
                rate.getId(),
                companyId
        );

        rate.setActive(true);

        return ProjectRoleRateMapper.toResponseDTO(rate);
    }

    private void validateRequest(
            ProjectRoleRateRequestDTO request
    ) {
        validateDateRange(
                request.effectiveFrom(),
                request.effectiveTo()
        );

        validateRateItems(request.items());
    }

    private void replaceItems(
            ProjectRoleRate rate,
            List<ProjectRoleRateItemRequestDTO> itemRequests
    ) {
        rate.clearItems();

        for (ProjectRoleRateItemRequestDTO itemRequest
                : itemRequests) {

            ProjectRoleRateItem item =
                    new ProjectRoleRateItem();

            item.setRateType(itemRequest.rateType());
            item.setCalculationType(
                    itemRequest.calculationType()
            );
            item.setValue(itemRequest.value());
            item.setDescription(
                    normalizeOptionalText(
                            itemRequest.description()
                    )
            );
            item.setActive(true);

            rate.addItem(item);
        }
    }

    private void validateRateItems(
            List<ProjectRoleRateItemRequestDTO> items
    ) {
        validateDuplicateRateTypes(items);
        validateRequiredRegularRate(items);

        for (ProjectRoleRateItemRequestDTO item : items) {
            validateCalculationType(
                    item.rateType(),
                    item.calculationType()
            );

            validateRateValue(item);
        }
    }

    private void validateDuplicateRateTypes(
            List<ProjectRoleRateItemRequestDTO> items
    ) {
        Set<RateType> rateTypes = new HashSet<>();

        for (ProjectRoleRateItemRequestDTO item : items) {
            if (!rateTypes.add(item.rateType())) {
                throw new DuplicateResourceException(
                        "Rate type cannot be duplicated: "
                                + item.rateType()
                );
            }
        }
    }

    private void validateRequiredRegularRate(
            List<ProjectRoleRateItemRequestDTO> items
    ) {
        boolean regularRateExists = items.stream()
                .anyMatch(item ->
                        item.rateType() == RateType.REGULAR
                );

        if (!regularRateExists) {
            throw new IllegalArgumentException(
                    "A REGULAR base rate is required"
            );
        }
    }

    private void validateCalculationType(
            RateType rateType,
            RateCalculationType calculationType
    ) {
        RateCalculationType expectedType =
                switch (rateType) {

                    case REGULAR ->
                            RateCalculationType.BASE_RATE;

                    case OVERTIME_1_5,
                         OVERTIME_2_0,
                         SATURDAY,
                         SUNDAY,
                         PUBLIC_HOLIDAY,
                         TRAVEL_TIME ->
                            RateCalculationType.MULTIPLIER;

                    case KILOMETRE ->
                            RateCalculationType.FIXED_RATE;

                    case LAFHA ->
                            RateCalculationType.FIXED_AMOUNT;
                };

        if (calculationType != expectedType) {
            throw new IllegalArgumentException(
                    "Rate type "
                            + rateType
                            + " must use calculation type "
                            + expectedType
            );
        }
    }

    private void validateRateValue(
            ProjectRoleRateItemRequestDTO item
    ) {
        BigDecimal value = item.value();

        if (value == null || value.signum() <= 0) {
            throw new IllegalArgumentException(
                    "Rate value must be greater than zero"
            );
        }

        if (
                item.calculationType()
                        == RateCalculationType.MULTIPLIER
                        && value.compareTo(BigDecimal.ONE) < 0
        ) {
            throw new IllegalArgumentException(
                    "Multiplier cannot be lower than 1.0"
            );
        }
    }

    private void validateOverlappingPeriod(
            Long positionId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            Long currentRateId,
            Long companyId
    ) {
        boolean overlappingExists;

        if (currentRateId == null) {
            overlappingExists =
                    rateRepository
                            .existsOverlappingPeriod(
                                    positionId,
                                    companyId,
                                    effectiveFrom,
                                    effectiveTo
                            );
        } else {
            overlappingExists =
                    rateRepository
                            .existsOverlappingPeriodExcludingId(
                                    positionId,
                                    companyId,
                                    effectiveFrom,
                                    effectiveTo,
                                    currentRateId
                            );
        }

        if (overlappingExists) {
            throw new DuplicateResourceException(
                    "Another rate already exists for this "
                            + "position during the informed period"
            );
        }
    }

    private void validateDateRange(
            LocalDate effectiveFrom,
            LocalDate effectiveTo
    ) {
        if (
                effectiveTo != null
                        && effectiveTo.isBefore(effectiveFrom)
        ) {
            throw new IllegalArgumentException(
                    "Effective to date cannot be before "
                            + "effective from date"
            );
        }
    }

    private ProjectRoleRate findEntityByIdAndCompany(
            Long rateId,
            Long companyId
    ) {
        return rateRepository
                .findByIdAndProjectPositionProjectCompanyId(
                        rateId,
                        companyId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project role rate not found "
                                        + "with ID: "
                                        + rateId
                        )
                );
    }

    private ProjectPosition findPositionInCurrentCompany(
            Long positionId,
            Long companyId
    ) {
        return positionRepository
                .findByIdAndProjectCompanyId(
                        positionId,
                        companyId
                )
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Project position not found "
                                        + "with ID: "
                                        + positionId
                        )
                );
    }

    private Long getCurrentCompanyId() {
        Long companyId = companyContext.getCompanyId();

        if (companyId == null) {
            throw new IllegalStateException(
                    "No company is selected in the current context"
            );
        }

        return companyId;
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value
                .trim()
                .replaceAll("\\s+", " ");
    }
}
