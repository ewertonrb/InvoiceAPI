package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.workerProfile.BankDetailsResponseDTO;
import com.invoice.invoice_api.dto.workerProfile.SuperDetailsResponseDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileResponseDTO;
import com.invoice.invoice_api.model.WorkerProfile;
import com.invoice.invoice_api.model.embeddable.BankDetails;
import com.invoice.invoice_api.model.embeddable.SuperDetails;

public class WorkerProfileMapper {

    private WorkerProfileMapper() {
    }

    public static WorkerProfileResponseDTO toResponseDTO(
            WorkerProfile workerProfile
    ) {
        return new WorkerProfileResponseDTO(
                workerProfile.getId(),

                workerProfile.getAppUser().getId(),
                workerProfile.getAppUser().getName(),
                workerProfile.getAppUser().getEmail(),

                workerProfile.getAbn(),
                workerProfile.getGstRegistered(),
                workerProfile.getPhone(),
                workerProfile.getDefaultHourlyRate(),

                toBankDetailsResponseDTO(
                        workerProfile.getBankDetails()
                ),

                toSuperDetailsResponseDTO(
                        workerProfile.getSuperDetails()
                ),

                workerProfile.getActive(),
                workerProfile.getNotes(),
                workerProfile.getCreatedAt(),
                workerProfile.getUpdatedAt()
        );
    }

    private static BankDetailsResponseDTO toBankDetailsResponseDTO(
            BankDetails bankDetails
    ) {
        if (bankDetails == null) {
            return null;
        }

        return new BankDetailsResponseDTO(
                bankDetails.getBankName(),
                bankDetails.getAccountName(),
                bankDetails.getBsb(),
                bankDetails.getAccountNumber()
        );
    }

    private static SuperDetailsResponseDTO toSuperDetailsResponseDTO(
            SuperDetails superDetails
    ) {
        if (superDetails == null) {
            return null;
        }

        return new SuperDetailsResponseDTO(
                superDetails.getFundName(),
                superDetails.getUsi(),
                superDetails.getMemberNumber()
        );
    }
}
