package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.workerProfile.BankDetailsRequestDTO;
import com.invoice.invoice_api.dto.workerProfile.BankDetailsResponseDTO;
import com.invoice.invoice_api.model.embeddable.BankDetails;

public class BankDetailsMapper {
    private BankDetailsMapper() {
    }

    public static BankDetails toEntity(
            BankDetailsRequestDTO dto
    ) {

        if (dto == null) {
            return null;
        }

        BankDetails bankDetails =
                new BankDetails();

        bankDetails.setBankName(dto.bankName());
        bankDetails.setAccountName(dto.accountName());
        bankDetails.setBsb(dto.bsb());
        bankDetails.setAccountNumber(dto.accountNumber());

        return bankDetails;
    }

    public static BankDetailsResponseDTO toResponseDTO(
            BankDetails entity
    ) {

        if (entity == null) {
            return null;
        }

        return new BankDetailsResponseDTO(
                entity.getBankName(),
                entity.getAccountName(),
                entity.getBsb(),
                entity.getAccountNumber()
        );
    }
}
