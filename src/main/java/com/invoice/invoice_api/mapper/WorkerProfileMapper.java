package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.workerProfile.BankDetailsResponseDTO;
import com.invoice.invoice_api.dto.workerProfile.SuperDetailsResponseDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileResponseDTO;
import com.invoice.invoice_api.dto.workerProfile.WorkerProfileSummaryDTO;
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

                workerProfile.getAppUser().getFullName(),

                workerProfile.getAppUser().getEmail(),

                workerProfile.getAbn(),

                workerProfile.getGstRegistered(),

                workerProfile.getPhone(),

                workerProfile.getStatus(),

                workerProfile.getCompletedAt(),

                BankDetailsMapper.toResponseDTO(
                        workerProfile.getBankDetails()
                ),

                SuperDetailsMapper.toResponseDTO(
                        workerProfile.getSuperDetails()
                ),

                workerProfile.getNotes(),

                workerProfile.getCreatedAt(),

                workerProfile.getUpdatedAt()
        );
    }

    public static WorkerProfileSummaryDTO toSummaryDTO(
            WorkerProfile workerProfile
    ) {

        return new WorkerProfileSummaryDTO(

                workerProfile.getId(),

                workerProfile.getAppUser().getId(),

                workerProfile.getAppUser().getFullName(),

                workerProfile.getAppUser().getEmail(),

                workerProfile.getPhone(),

                workerProfile.getAbn(),

                workerProfile.getGstRegistered(),

                workerProfile.getStatus(),

                workerProfile.isComplete()
        );
    }
}
