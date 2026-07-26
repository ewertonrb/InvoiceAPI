package com.invoice.invoice_api.service;

import com.invoice.invoice_api.dto.workerProfile.BankDetailsRequestDTO;
import com.invoice.invoice_api.dto.workerProfile.SuperDetailsRequestDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileRequestDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileResponseDTO;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.ResourceNotFoundException;
import com.invoice.invoice_api.mapper.WorkerProfileMapper;
import com.invoice.invoice_api.model.AppUser;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.model.embeddable.BankDetails;
import com.invoice.invoice_api.model.embeddable.SuperDetails;
import com.invoice.invoice_api.repository.AppUserRepository;
import com.invoice.invoice_api.repository.WorkerProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class WorkerProfileService {
    private final WorkerProfileRepository workerProfileRepository;
    private final AppUserRepository appUserRepository;

    public WorkerProfileService(
            WorkerProfileRepository workerProfileRepository,
            AppUserRepository appUserRepository
    ) {
        this.workerProfileRepository = workerProfileRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional
    public WorkerProfileResponseDTO create(
            WorkerProfileRequestDTO request
    ) {
        if (workerProfileRepository.existsByAppUserId(
                request.appUserId()
        )) {
            throw new DuplicateResourceException(
                    "This app user already has a worker profile"
            );
        }

        String normalizedAbn = normalizeAbn(request.abn());

        validateUniqueAbn(normalizedAbn, null);

        AppUser appUser = findAppUserById(request.appUserId());

        WorkerProfile workerProfile = new WorkerProfile();

        workerProfile.setAppUser(appUser);
        applyRequest(workerProfile, request, normalizedAbn);
        workerProfile.setActive(true);

        WorkerProfile savedProfile =
                workerProfileRepository.save(workerProfile);

        return WorkerProfileMapper.toResponseDTO(savedProfile);
    }

    @Transactional(readOnly = true)
    public WorkerProfileResponseDTO findById(Long id) {
        return WorkerProfileMapper.toResponseDTO(
                findEntityById(id)
        );
    }

    @Transactional(readOnly = true)
    public WorkerProfileResponseDTO findByAppUserId(
            Long appUserId
    ) {
        WorkerProfile workerProfile =
                workerProfileRepository
                        .findByAppUserId(appUserId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Worker profile not found for app user ID: "
                                                + appUserId
                                )
                        );

        return WorkerProfileMapper.toResponseDTO(workerProfile);
    }

    @Transactional(readOnly = true)
    public List<WorkerProfileResponseDTO> findAll() {
        return workerProfileRepository.findAll()
                .stream()
                .map(WorkerProfileMapper::toResponseDTO)
                .toList();
    }

    @Transactional
    public WorkerProfileResponseDTO update(
            Long id,
            WorkerProfileRequestDTO request
    ) {
        WorkerProfile workerProfile = findEntityById(id);

        if (!workerProfile.getAppUser()
                .getId()
                .equals(request.appUserId())) {

            throw new IllegalArgumentException(
                    "The app user associated with a worker profile cannot be changed"
            );
        }

        String normalizedAbn = normalizeAbn(request.abn());

        validateUniqueAbn(normalizedAbn, id);

        applyRequest(workerProfile, request, normalizedAbn);

        WorkerProfile updatedProfile =
                workerProfileRepository.save(workerProfile);

        return WorkerProfileMapper.toResponseDTO(updatedProfile);
    }

    @Transactional
    public void deactivate(Long id) {
        WorkerProfile workerProfile = findEntityById(id);

        workerProfile.setActive(false);

        workerProfileRepository.save(workerProfile);
    }

    @Transactional
    public WorkerProfileResponseDTO reactivate(Long id) {
        WorkerProfile workerProfile = findEntityById(id);

        workerProfile.setActive(true);

        return WorkerProfileMapper.toResponseDTO(
                workerProfileRepository.save(workerProfile)
        );
    }

    private void applyRequest(
            WorkerProfile workerProfile,
            WorkerProfileRequestDTO request,
            String normalizedAbn
    ) {
        workerProfile.setAbn(normalizedAbn);

        workerProfile.setGstRegistered(
                Boolean.TRUE.equals(request.gstRegistered())
        );

        workerProfile.setPhone(
                normalizeOptionalText(request.phone())
        );

        workerProfile.setDefaultHourlyRate(
                request.defaultHourlyRate()
        );

        workerProfile.setNotes(
                normalizeOptionalText(request.notes())
        );

        workerProfile.setBankDetails(
                toBankDetails(request.bankDetails())
        );

        workerProfile.setSuperDetails(
                toSuperDetails(request.superDetails())
        );
    }

    private BankDetails toBankDetails(
            BankDetailsRequestDTO request
    ) {
        if (request == null) {
            return null;
        }

        BankDetails bankDetails = new BankDetails();

        bankDetails.setBankName(
                normalizeOptionalText(request.bankName())
        );

        bankDetails.setAccountName(
                normalizeOptionalText(request.accountName())
        );

        bankDetails.setBsb(
                normalizeDigits(request.bsb())
        );

        bankDetails.setAccountNumber(
                normalizeDigits(request.accountNumber())
        );

        return bankDetails;
    }

    private SuperDetails toSuperDetails(
            SuperDetailsRequestDTO request
    ) {
        if (request == null) {
            return null;
        }

        SuperDetails superDetails = new SuperDetails();

        superDetails.setFundName(
                normalizeOptionalText(request.fundName())
        );

        superDetails.setUsi(
                normalizeOptionalText(request.usi())
        );

        superDetails.setMemberNumber(
                normalizeOptionalText(request.memberNumber())
        );

        return superDetails;
    }

    private void validateUniqueAbn(
            String abn,
            Long currentProfileId
    ) {
        if (abn == null) {
            return;
        }

        workerProfileRepository.findByAbn(abn)
                .filter(existingProfile ->
                        currentProfileId == null
                                || !existingProfile.getId()
                                .equals(currentProfileId)
                )
                .ifPresent(existingProfile -> {
                    throw new DuplicateResourceException(
                            "A worker profile already exists with ABN: "
                                    + abn
                    );
                });
    }

    private WorkerProfile findEntityById(Long id) {
        return workerProfileRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Worker profile not found with ID: "
                                        + id
                        )
                );
    }

    private AppUser findAppUserById(Long id) {
        return appUserRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "App user not found with ID: "
                                        + id
                        )
                );
    }

    private String normalizeAbn(String abn) {
        return normalizeDigits(abn);
    }

    private String normalizeDigits(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.replaceAll("\\D", "");
    }

    private String normalizeOptionalText(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
