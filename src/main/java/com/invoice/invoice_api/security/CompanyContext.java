package com.invoice.invoice_api.security;

import com.invoice.invoice_api.enums.CompanyRole;
import com.invoice.invoice_api.exception.AccessDeniedBusinessException;
import org.springframework.stereotype.Component;

@Component
public class CompanyContext {
    private static final ThreadLocal<Long> COMPANY_ID =
            new ThreadLocal<>();

    private static final ThreadLocal<CompanyRole> ROLE =
            new ThreadLocal<>();

    public void set(
            Long companyId,
            CompanyRole role
    ) {
        COMPANY_ID.set(companyId);
        ROLE.set(role);
    }

    public Long getCompanyId() {
        Long companyId = COMPANY_ID.get();

        if (companyId == null) {
            throw new AccessDeniedBusinessException(
                    "No company has been selected"
            );
        }

        return companyId;
    }

    public CompanyRole getRole() {
        CompanyRole role = ROLE.get();

        if (role == null) {
            throw new AccessDeniedBusinessException(
                    "No company role is available"
            );
        }

        return role;
    }

    public boolean hasCompanySelected() {
        return COMPANY_ID.get() != null;
    }

    public void clear() {
        COMPANY_ID.remove();
        ROLE.remove();
    }
}
