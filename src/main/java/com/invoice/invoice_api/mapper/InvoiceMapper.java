package com.invoice.invoice_api.mapper;

import com.invoice.invoice_api.dto.invoice.InvoiceItemResponseDTO;
import com.invoice.invoice_api.dto.invoice.InvoiceResponseDTO;
import com.invoice.invoice_api.dto.invoice.InvoiceSummaryResponseDTO;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;

import java.util.List;

public final class InvoiceMapper {
    private InvoiceMapper() {
    }

    public static InvoiceResponseDTO toResponseDTO(Invoice invoice) {
        if (invoice == null) {return null;}

        WorkerProfile workerProfile = invoice.getWorkerProfile();

        AppUser appUser = workerProfile.getAppUser();

        List<InvoiceItemResponseDTO> items =
                invoice.getItems()
                        .stream()
                        .map(InvoiceMapper::toItemResponseDTO)
                        .toList();

        return new InvoiceResponseDTO(
                invoice.getId(),

                invoice.getInvoiceNumber(),

                invoice.getCompany().getId(),

                invoice.getCompany().getName(),

                workerProfile.getId(),

                appUser.getId(),

                appUser.getFullName(),

                appUser.getEmail(),

                workerProfile.getAbn(),

                workerProfile.getGstRegistered(),

                invoice.getPeriodStart(),

                invoice.getPeriodEnd(),

                invoice.getIssueDate(),

                invoice.getDueDate(),

                invoice.getSubtotalAmount(),

                invoice.getGstAmount(),

                invoice.getTotalAmount(),

                invoice.getStatus(),

                invoice.getNotes(),

                invoice.getPdfPath(),

                invoice.getIssuedAt(),

                invoice.getPaidAt(),

                invoice.getCancelledAt(),

                items.size(),

                items,

                invoice.getCreatedAt(),

                invoice.getUpdatedAt()
        );
    }

    public static InvoiceSummaryResponseDTO toSummaryResponseDTO(Invoice invoice) {
        if (invoice == null) {
            return null;
        }

        return new InvoiceSummaryResponseDTO(
                invoice.getId(),

                invoice.getInvoiceNumber(),

                invoice.getWorkerProfile().getId(),

                invoice
                        .getWorkerProfile()
                        .getAppUser()
                        .getFullName(),

                invoice.getPeriodStart(),

                invoice.getPeriodEnd(),

                invoice.getIssueDate(),

                invoice.getDueDate(),

                invoice.getSubtotalAmount(),

                invoice.getGstAmount(),

                invoice.getTotalAmount(),

                invoice.getStatus(),

                invoice.getItems().size(),

                invoice.getCreatedAt()
        );
    }

    private static InvoiceItemResponseDTO toItemResponseDTO(InvoiceItem item) {
        WorkLog workLog = item.getWorkLog();

        WorkLogFinancialSnapshot snapshot = workLog.getFinancialSnapshot();

        String projectName = snapshot == null ? null : snapshot.getProjectName();

        String positionName = snapshot == null ? null : snapshot.getPositionName();

        return new InvoiceItemResponseDTO(

                item.getId(),

                workLog.getId(),

                workLog.getWorkDate(),

                projectName,

                positionName,

                item.getDescription(),

                item.getSubtotalAmount(),

                item.getGstAmount(),

                item.getTotalAmount(),

                item.getCreatedAt(),

                item.getUpdatedAt()
        );
    }
}
