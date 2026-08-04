package com.invoice.invoice_api.service;

import com.invoice.invoice_api.enums.WorkerProfileStatus;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.model.embeddable.BankDetails;
import com.invoice.invoice_api.model.embeddable.SuperDetails;

import java.time.LocalDateTime;

public class WorkerProfileRules {
    private WorkerProfileRules() {
    }

    public static void applyProfileInformation(
            WorkerProfile workerProfile,
            String abn,
            Boolean gstRegistered,
            String phone,
            String notes,
            BankDetails bankDetails,
            SuperDetails superDetails
    ) {
        workerProfile.setAbn(
                normalizeAbn(abn)
        );

        workerProfile.setGstRegistered(
                Boolean.TRUE.equals(gstRegistered)
        );

        workerProfile.setPhone(
                normalizeText(phone)
        );

        workerProfile.setNotes(
                normalizeText(notes)
        );

        workerProfile.setBankDetails(
                bankDetails
        );

        workerProfile.setSuperDetails(
                superDetails
        );

        updateCompletionStatus(workerProfile);
    }

    public static void updateCompletionStatus(
            WorkerProfile workerProfile
    ) {
        WorkerProfileStatus calculatedStatus =
                calculateStatus(workerProfile);

        workerProfile.setStatus(
                calculatedStatus
        );

        if (
                calculatedStatus
                        == WorkerProfileStatus.COMPLETE
        ) {
            if (workerProfile.getCompletedAt() == null) {
                workerProfile.setCompletedAt(
                        LocalDateTime.now()
                );
            }

            return;
        }

        workerProfile.setCompletedAt(null);
    }

    public static WorkerProfileStatus calculateStatus(
            WorkerProfile workerProfile
    ) {
        if (isProfileComplete(workerProfile)) {
            return WorkerProfileStatus.COMPLETE;
        }

        return WorkerProfileStatus.INCOMPLETE;
    }

    public static boolean isProfileComplete(
            WorkerProfile workerProfile
    ) {
        return hasValidAbn(
                workerProfile.getAbn()
        )
                && hasText(
                workerProfile.getPhone()
        )
                && isBankDetailsComplete(
                workerProfile.getBankDetails()
        )
                && isSuperDetailsComplete(
                workerProfile.getSuperDetails()
        );
    }

    public static String normalizeAbn(
            String abn
    ) {
        if (abn == null) {
            return null;
        }

        String normalized =
                abn.replaceAll("\\D", "");

        if (normalized.isBlank()) {
            return null;
        }

        return normalized;
    }

    public static String normalizeText(
            String value
    ) {
        if (value == null) {
            return null;
        }

        String normalized =
                value.trim();

        if (normalized.isBlank()) {
            return null;
        }

        return normalized;
    }

    private static boolean hasValidAbn(
            String abn
    ) {
        return abn != null
                && abn.matches("\\d{11}");
    }

    private static boolean isBankDetailsComplete(
            BankDetails bankDetails
    ) {
        if (bankDetails == null) {
            return false;
        }

        return hasText(
                bankDetails.getBankName()
        )
                && hasText(
                bankDetails.getAccountName()
        )
                && hasText(
                bankDetails.getBsb()
        )
                && hasText(
                bankDetails.getAccountNumber()
        );
    }

    private static boolean isSuperDetailsComplete(
            SuperDetails superDetails
    ) {
        if (superDetails == null) {
            return false;
        }

        return hasText(
                superDetails.getFundName()
        )
                && hasText(
                superDetails.getUsi()
        )
                && hasText(
                superDetails.getMemberNumber()
        );
    }

    private static boolean hasText(
            String value
    ) {
        return value != null
                && !value.isBlank();
    }
}
