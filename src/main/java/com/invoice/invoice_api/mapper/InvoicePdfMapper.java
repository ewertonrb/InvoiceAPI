package com.invoice.invoice_api.mapper;


import com.invoice.invoice_api.dto.pdf.InvoicePdfDTO;
import com.invoice.invoice_api.dto.pdf.InvoicePdfItemDTO;
import com.invoice.invoice_api.model.*;
import com.invoice.invoice_api.model.embeddable.BankDetails;
import com.invoice.invoice_api.model.embeddable.workLog.WorkLogFinancialSnapshot;

import java.util.List;

public final class InvoicePdfMapper {

    private InvoicePdfMapper() {
    }

    public static InvoicePdfDTO toPdfDTO(
            Invoice invoice
    ) {
        if (invoice == null) {
            return null;
        }

        Company company =
                invoice.getCompany();

        WorkerProfile workerProfile =
                invoice.getWorkerProfile();

        AppUser appUser =
                workerProfile.getAppUser();

        BankDetails bankDetails =
                workerProfile.getBankDetails();

        List<InvoicePdfItemDTO> items =
                invoice.getItems()
                        .stream()
                        .map(
                                InvoicePdfMapper
                                        ::toItemDTO
                        )
                        .toList();

        boolean gstApplied =
                invoice.getItems()
                        .stream()
                        .map(InvoiceItem::getWorkLog)
                        .map(WorkLog::getFinancialSnapshot)
                        .filter(snapshot ->
                                snapshot != null
                        )
                        .anyMatch(snapshot ->
                                Boolean.TRUE.equals(
                                        snapshot.getGstApplied()
                                )
                        );

        return new InvoicePdfDTO(
                company.getName(),

                company.getAbn(),

                company.getAddress(),

                company.getPhone(),

                company.getEmail(),

                invoice.getInvoiceNumber(),

                invoice.getIssueDate(),

                invoice.getDueDate(),

                invoice.getPeriodStart(),

                invoice.getPeriodEnd(),

                appUser.getFullName(),

                workerProfile.getAbn(),

                workerProfile.getGstRegistered(),

                gstApplied,

                bankDetails == null
                        ? null
                        : bankDetails.getBankName(),

                bankDetails == null
                        ? null
                        : bankDetails.getAccountName(),

                bankDetails == null
                        ? null
                        : bankDetails.getBsb(),

                bankDetails == null
                        ? null
                        : bankDetails.getAccountNumber(),

                invoice.getSubtotalAmount(),

                invoice.getGstAmount(),

                invoice.getTotalAmount(),

                items
        );
    }

    private static InvoicePdfItemDTO toItemDTO(
            InvoiceItem invoiceItem
    ) {
        WorkLog workLog =
                invoiceItem.getWorkLog();

        WorkLogFinancialSnapshot snapshot =
                workLog.getFinancialSnapshot();

        String projectName =
                snapshot == null
                        ? null
                        : snapshot.getProjectName();

        String positionName =
                snapshot == null
                        ? null
                        : snapshot.getPositionName();

        return new InvoicePdfItemDTO(
                workLog.getWorkDate(),

                projectName,

                positionName,

                invoiceItem.getDescription(),

                invoiceItem.getSubtotalAmount(),

                invoiceItem.getGstAmount(),

                invoiceItem.getTotalAmount()
        );
    }
}