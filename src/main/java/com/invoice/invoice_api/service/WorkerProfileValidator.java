package com.invoice.invoice_api.service;

import com.invoice.invoice_api.enums.WorkerProfileStatus;
import com.invoice.invoice_api.exception.BusinessException;
import com.invoice.invoice_api.exception.DuplicateResourceException;
import com.invoice.invoice_api.exception.InvalidOperationException;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.repository.WorkerProfileRepository;
import org.springframework.stereotype.Component;

@Component
public class WorkerProfileValidator {
    private final WorkerProfileRepository workerProfileRepository;

    public WorkerProfileValidator(
            WorkerProfileRepository workerProfileRepository
    ) {
        this.workerProfileRepository = workerProfileRepository;
    }

    public String normalizeAndValidateAbn(
            String abn,
            Long currentWorkerProfileId
    ) {
        String normalizedAbn =
                WorkerProfileRules.normalizeAbn(abn);

        if (normalizedAbn == null) {
            return null;
        }

        if (!normalizedAbn.matches("\\d{11}")) {
            throw new BusinessException(
                    "ABN must contain exactly 11 digits."
            );
        }

        validateUniqueAbn(
                normalizedAbn,
                currentWorkerProfileId
        );

        return normalizedAbn;
    }

    public void validateCanBeUpdated(
            WorkerProfile workerProfile
    ) {
        if (
                workerProfile.getStatus()
                        == WorkerProfileStatus.SUSPENDED
        ) {
            throw new InvalidOperationException(
                    "A suspended worker profile cannot be updated."
            );
        }
    }

    private void validateUniqueAbn(
            String normalizedAbn,
            Long currentWorkerProfileId
    ) {
        boolean duplicated;

        if (currentWorkerProfileId == null) {
            duplicated =
                    workerProfileRepository.existsByAbn(
                            normalizedAbn
                    );
        } else {
            duplicated =
                    workerProfileRepository
                            .existsByAbnAndIdNot(
                                    normalizedAbn,
                                    currentWorkerProfileId
                            );
        }

        if (duplicated) {
            throw new DuplicateResourceException(
                    "A worker profile already exists with ABN: "
                            + normalizedAbn
            );
        }
    }
}
