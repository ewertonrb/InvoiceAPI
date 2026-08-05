package com.invoice.invoice_api.model;


import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "invoice_items",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_invoice_item_work_log",
                        columnNames = "work_log_id"
                )
        },
        indexes = {
                @Index(
                        name = "idx_invoice_item_invoice",
                        columnList = "invoice_id"
                )
        }
)
public class InvoiceItem {
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
            name = "invoice_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_invoice_item_invoice"
            )
    )
    private Invoice invoice;

    @OneToOne(
            fetch = FetchType.LAZY,
            optional = false
    )
    @JoinColumn(
            name = "work_log_id",
            nullable = false,
            unique = true,
            foreignKey = @ForeignKey(
                    name = "fk_invoice_item_work_log"
            )
    )
    private WorkLog workLog;

    /*
     * ============================================================
     * SNAPSHOT VALUES
     * ============================================================
     */

    @Column(
            name = "description",
            nullable = false,
            length = 500
    )
    private String description;

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

    public InvoiceItem() {
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime now =
                LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

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

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(
            Invoice invoice
    ) {
        this.invoice = invoice;
    }

    public WorkLog getWorkLog() {
        return workLog;
    }

    public void setWorkLog(
            WorkLog workLog
    ) {
        this.workLog = workLog;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(
            String description
    ) {
        this.description = description;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
