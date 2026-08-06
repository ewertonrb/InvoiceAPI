package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.company.CompanyRequestDTO;
import com.invoice.invoice_api.dto.company.CompanyResponseDTO;
import com.invoice.invoice_api.model.Company;

public class CompanyMapper {
    private CompanyMapper() {
    }

    /*
     * ============================================================
     * REQUEST TO ENTITY
     * ============================================================
     */

    public static Company toEntity(
            CompanyRequestDTO request
    ) {
        Company company =
                new Company();

        company.setName(
                request.name()
        );

        company.setAbn(
                request.abn()
        );

        company.setEmail(
                request.email()
        );

        company.setPhone(
                request.phone()
        );

        company.setAddress(
                request.address()
        );

        /*
         * GST remains enabled by default when the field
         * is omitted during company creation.
         */
        company.setContractorInvoiceGstEnabled(
                request.contractorInvoiceGstEnabled() == null
                        ? true
                        : request.contractorInvoiceGstEnabled()
        );

        company.setActive(
                request.active() == null
                        ? true
                        : request.active()
        );

        return company;
    }

    /*
     * ============================================================
     * ENTITY TO RESPONSE
     * ============================================================
     */

    public static CompanyResponseDTO toResponseDTO(
            Company company
    ) {
        if (company == null) {
            return null;
        }

        return new CompanyResponseDTO(
                company.getId(),

                company.getName(),

                company.getAbn(),

                company.getEmail(),

                company.getPhone(),

                company.getAddress(),

                company.getActive(),

                company.getContractorInvoiceGstEnabled(),

                company.getCreatedAt(),

                company.getUpdatedAt()
        );
    }

    /*
     * ============================================================
     * UPDATE ENTITY
     * ============================================================
     */

    public static void updateEntity(
            Company company,
            CompanyRequestDTO request
    ) {
        company.setName(
                request.name()
        );

        company.setAbn(
                request.abn()
        );

        company.setEmail(
                request.email()
        );

        company.setPhone(
                request.phone()
        );

        company.setAddress(
                request.address()
        );

        /*
         * Null means that the current GST configuration
         * must be preserved.
         */
        if (
                request.contractorInvoiceGstEnabled()
                        != null
        ) {
            company.setContractorInvoiceGstEnabled(
                    request.contractorInvoiceGstEnabled()
            );
        }

        /*
         * Null means that the current active status
         * must be preserved.
         */
        if (request.active() != null) {
            company.setActive(
                    request.active()
            );
        }
    }
}
