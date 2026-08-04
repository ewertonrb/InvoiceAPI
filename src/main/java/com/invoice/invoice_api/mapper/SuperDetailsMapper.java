package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.workerProfile.SuperDetailsRequestDTO;
import com.invoice.invoice_api.dto.workerProfile.SuperDetailsResponseDTO;
import com.invoice.invoice_api.model.embeddable.SuperDetails;

public class SuperDetailsMapper {
    private SuperDetailsMapper() {
    }

    public static SuperDetails toEntity(
            SuperDetailsRequestDTO dto
    ) {

        if (dto == null) {
            return null;
        }

        SuperDetails superDetails =
                new SuperDetails();

        superDetails.setFundName(dto.fundName());
        superDetails.setUsi(dto.usi());
        superDetails.setMemberNumber(dto.memberNumber());

        return superDetails;
    }

    public static SuperDetailsResponseDTO toResponseDTO(
            SuperDetails entity
    ) {

        if (entity == null) {
            return null;
        }

        return new SuperDetailsResponseDTO(
                entity.getFundName(),
                entity.getUsi(),
                entity.getMemberNumber()
        );
    }
}
