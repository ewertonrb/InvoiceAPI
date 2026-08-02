package com.invoice.invoice_api.dto.joinLink;

public record CompanyJoinLinkCreatedResponseDTO(

        CompanyJoinLinkResponseDTO joinLink,

        String joinUrl
) {
}
