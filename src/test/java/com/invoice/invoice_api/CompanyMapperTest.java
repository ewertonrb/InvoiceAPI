package com.invoice.invoice_api;

import com.invoice.invoice_api.dto.company.CompanyResponseDTO;
import com.invoice.invoice_api.mapper.CompanyMapper;
import com.invoice.invoice_api.model.Company;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyMapperTest {

    @Test
    void mapsActiveAndContractorInvoiceGstEnabledInDtoFieldOrder() {
        Company company = new Company();
        company.setActive(false);
        company.setContractorInvoiceGstEnabled(true);

        CompanyResponseDTO response = CompanyMapper.toResponseDTO(company);

        assertFalse(response.active());
        assertTrue(response.contractorInvoiceGstEnabled());
    }
}
