package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.company.CompanyRequestDTO;
import com.invoice.invoice_api.dto.company.CompanyResponseDTO;
import com.invoice.invoice_api.model.Company;

public class CompanyMapper {
    public static Company toEntity(CompanyRequestDTO request) {
        Company company = new Company();

        company.setName(request.name());
        company.setAbn(request.abn());
        company.setEmail(request.email());
        company.setPhone(request.phone());
        company.setAddress(request.address());
        company.setActive(request.active() != null ? request.active() : true);

        return company;
    }

    public static CompanyResponseDTO toResponseDTO(Company company) {
        return new CompanyResponseDTO(
                company.getId(),
                company.getName(),
                company.getAbn(),
                company.getEmail(),
                company.getPhone(),
                company.getAddress(),
                company.getActive(),
                company.getCreatedAt(),
                company.getUpdatedAt()
        );
    }

    public static void updateEntity(Company company, CompanyRequestDTO request) {
        company.setName(request.name());
        company.setAbn(request.abn());
        company.setEmail(request.email());
        company.setPhone(request.phone());
        company.setAddress(request.address());
        company.setActive(request.active() != null ? request.active() : true);
    }
}
