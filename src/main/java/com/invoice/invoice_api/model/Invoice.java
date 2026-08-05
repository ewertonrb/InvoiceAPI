package com.invoice.invoice_api.model;


import com.invoice.invoice_api.enums.InvoiceStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "invoices",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_invoice_company_number",
                        columnNames = {
                                "company_id",
                                "invoice_number"
                        }
                )
        },
        indexes = {
                @Index(
                        name = "idx_invoice_company_status",
                        columnList = "company_id, status"
                ),
                @Index(
                        name = "idx_invoice_worker_period",
                        columnList = "worker_profile_id, period_start, period_end"
                ),
                @Index(
                        name = "idx_invoice_issue_date",
                        columnList = "issue_date"
                )
        }
)
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * ============================================================
     * RELATIONSHIPS
     * ============================================================
     */

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "company_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_invoice_company"
            )
    )
    private Company company;

    @ManyToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "worker_profile_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_invoice_worker_profile"
            )
    )
    private WorkerProfile workerProfile;

    @OneToMany(
            mappedBy = "invoice",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("id ASC")
    private List<InvoiceItem> items =
            new ArrayList<>();

    /*
     * ============================================================
     * IDENTIFICATION
     * ============================================================
     */

    @Column(
            name = "invoice_number",
            nullable = false,
            length = 50
    )
    private String invoiceNumber;

    /*
     * ============================================================
     * PERIOD
     * ============================================================
     */

    @Column(
            name = "period_start",
            nullable = false
    )
    private LocalDate periodStart;

    @Column(
            name = "period_end",
            nullable = false
    )
    private LocalDate periodEnd;

    /*
     * ============================================================
     * DATES
     * ============================================================
     */

    @Column(
            name = "issue_date"
    )
    private LocalDate issueDate;

    @Column(
            name = "due_date"
    )
    private LocalDate dueDate;

    @Column(
            name = "issued_at"
    )
    private LocalDateTime issuedAt;

    @Column(
            name = "paid_at"
    )
    private LocalDateTime paidAt;

    @Column(
            name = "cancelled_at"
    )
    private LocalDateTime cancelledAt;

    /*
     * ============================================================
     * TOTALS
     * ============================================================
     */

    @Column(
            name = "subtotal_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal subtotalAmount =
            BigDecimal.ZERO;

    @Column(
            name = "gst_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal gstAmount =
            BigDecimal.ZERO;

    @Column(
            name = "total_amount",
            nullable = false,
            precision = 14,
            scale = 2
    )
    private BigDecimal totalAmount =
            BigDecimal.ZERO;

    /*
     * ============================================================
     * STATUS
     * ============================================================
     */

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private InvoiceStatus status =
            InvoiceStatus.DRAFT;

    /*
     * ============================================================
     * OPTIONAL INFORMATION
     * ============================================================
     */

    @Column(
            name = "notes",
            length = 1000
    )
    private String notes;

    @Column(
            name = "pdf_path",
            length = 500
    )
    private String pdfPath;

    /*
     * ============================================================
     * AUDIT
     * ============================================================
     */

    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private LocalDateTime createdAt;

    @Column(
            name = "updated_at",
            nullable = false
    )
    private LocalDateTime updatedAt;

    public Invoice() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (status == null) {
            status = InvoiceStatus.DRAFT;
        }

        initializeNullAmounts();
    }

    @PreUpdate
    public void preUpdate() {
        updatedAt =
                LocalDateTime.now();

        initializeNullAmounts();
    }

    private void initializeNullAmounts() {
        if (subtotalAmount == null) {
            subtotalAmount =
                    BigDecimal.ZERO;
        }

        if (gstAmount == null) {
            gstAmount =
                    BigDecimal.ZERO;
        }

        if (totalAmount == null) {
            totalAmount =
                    BigDecimal.ZERO;
        }
    }

    /*
     * ============================================================
     * ITEM MANAGEMENT
     * ============================================================
     */

    public void addItem(
            InvoiceItem item
    ) {
        items.add(item);
        item.setInvoice(this);
    }

    public void removeItem(
            InvoiceItem item
    ) {
        items.remove(item);
        item.setInvoice(null);
    }

    public void clearItems() {
        for (InvoiceItem item : items) {
            item.setInvoice(null);
        }

        items.clear();
    }

    /*
     * ============================================================
     * DOMAIN HELPERS
     * ============================================================
     */

    public boolean isDraft() {
        return status == InvoiceStatus.DRAFT;
    }

    public boolean isIssued() {
        return status == InvoiceStatus.ISSUED;
    }

    public boolean isPaid() {
        return status == InvoiceStatus.PAID;
    }

    public boolean isCancelled() {
        return status == InvoiceStatus.CANCELLED;
    }

    public boolean canBeIssued() {
        return status == InvoiceStatus.DRAFT;
    }

    public boolean canBeMarkedAsPaid() {
        return status == InvoiceStatus.ISSUED;
    }

    public boolean canBeCancelled() {
        return status == InvoiceStatus.DRAFT;
    }

    /*
     * ============================================================
     * GETTERS / SETTERS
     * ============================================================
     */

    public Long getId() {
        return id;
    }

    public Company getCompany() {
        return company;
    }

    public void setCompany(
            Company company
    ) {
        this.company = company;
    }

    public WorkerProfile getWorkerProfile() {
        return workerProfile;
    }

    public void setWorkerProfile(
            WorkerProfile workerProfile
    ) {
        this.workerProfile =
                workerProfile;
    }

    public List<InvoiceItem> getItems() {
        return items;
    }

    public void setItems(
            List<InvoiceItem> items
    ) {
        clearItems();

        if (items != null) {
            items.forEach(this::addItem);
        }
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(
            String invoiceNumber
    ) {
        this.invoiceNumber =
                invoiceNumber;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(
            LocalDate periodStart
    ) {
        this.periodStart =
                periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(
            LocalDate periodEnd
    ) {
        this.periodEnd =
                periodEnd;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(
            LocalDate issueDate
    ) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(
            LocalDate dueDate
    ) {
        this.dueDate = dueDate;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(
            LocalDateTime issuedAt
    ) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(
            LocalDateTime paidAt
    ) {
        this.paidAt = paidAt;
    }

    public LocalDateTime getCancelledAt() {
        return cancelledAt;
    }

    public void setCancelledAt(
            LocalDateTime cancelledAt
    ) {
        this.cancelledAt =
                cancelledAt;
    }

    public BigDecimal getSubtotalAmount() {
        return subtotalAmount;
    }

    public void setSubtotalAmount(
            BigDecimal subtotalAmount
    ) {
        this.subtotalAmount =
                subtotalAmount;
    }

    public BigDecimal getGstAmount() {
        return gstAmount;
    }

    public void setGstAmount(
            BigDecimal gstAmount
    ) {
        this.gstAmount = gstAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount
    ) {
        this.totalAmount =
                totalAmount;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public void setStatus(
            InvoiceStatus status
    ) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(
            String notes
    ) {
        this.notes = notes;
    }

    public String getPdfPath() {
        return pdfPath;
    }

    public void setPdfPath(
            String pdfPath
    ) {
        this.pdfPath = pdfPath;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
