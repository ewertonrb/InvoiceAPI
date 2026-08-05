/*
 * ============================================================
 * INVOICE MODULE
 *
 * Creates:
 * - invoices
 * - invoice_items
 *
 * Supports:
 * - draft generation by worker and period
 * - issue workflow
 * - payment status
 * - cancellation
 * - one WorkLog per InvoiceItem
 * ============================================================
 */


/*
 * ============================================================
 * INVOICES
 * ============================================================
 */

CREATE TABLE IF NOT EXISTS invoices (
                                        id BIGSERIAL PRIMARY KEY,

                                        company_id BIGINT NOT NULL,

                                        worker_profile_id BIGINT NOT NULL,

                                        invoice_number VARCHAR(50) NOT NULL,

                                        period_start DATE NOT NULL,

                                        period_end DATE NOT NULL,

                                        issue_date DATE,

                                        due_date DATE,

                                        issued_at TIMESTAMP,

                                        paid_at TIMESTAMP,

                                        cancelled_at TIMESTAMP,

                                        subtotal_amount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,

                                        gst_amount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,

                                        total_amount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,

                                        status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

                                        notes VARCHAR(1000),

                                        pdf_path VARCHAR(500),

                                        created_at TIMESTAMP NOT NULL,

                                        updated_at TIMESTAMP NOT NULL,

                                        CONSTRAINT fk_invoice_company
                                            FOREIGN KEY (company_id)
                                                REFERENCES companies (id),

                                        CONSTRAINT fk_invoice_worker_profile
                                            FOREIGN KEY (worker_profile_id)
                                                REFERENCES worker_profiles (id),

                                        CONSTRAINT uk_invoice_company_number
                                            UNIQUE (company_id, invoice_number),

                                        CONSTRAINT chk_invoice_period
                                            CHECK (period_start <= period_end),

                                        CONSTRAINT chk_invoice_amounts
                                            CHECK (
                                                subtotal_amount >= 0
                                                    AND gst_amount >= 0
                                                    AND total_amount >= 0
                                                ),

                                        CONSTRAINT chk_invoice_status
                                            CHECK (
                                                status IN (
                                                           'DRAFT',
                                                           'ISSUED',
                                                           'PAID',
                                                           'CANCELLED'
                                                    )
                                                )
);


/*
 * ============================================================
 * INVOICE ITEMS
 * ============================================================
 */

CREATE TABLE IF NOT EXISTS invoice_items (
                                             id BIGSERIAL PRIMARY KEY,

                                             invoice_id BIGINT NOT NULL,

                                             work_log_id BIGINT NOT NULL,

                                             description VARCHAR(500) NOT NULL,

                                             subtotal_amount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,

                                             gst_amount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,

                                             total_amount NUMERIC(14, 2) NOT NULL DEFAULT 0.00,

                                             created_at TIMESTAMP NOT NULL,

                                             updated_at TIMESTAMP NOT NULL,

                                             CONSTRAINT fk_invoice_item_invoice
                                                 FOREIGN KEY (invoice_id)
                                                     REFERENCES invoices (id),

                                             CONSTRAINT fk_invoice_item_work_log
                                                 FOREIGN KEY (work_log_id)
                                                     REFERENCES work_logs (id),

                                             CONSTRAINT uk_invoice_item_work_log
                                                 UNIQUE (work_log_id),

                                             CONSTRAINT chk_invoice_item_amounts
                                                 CHECK (
                                                     subtotal_amount >= 0
                                                         AND gst_amount >= 0
                                                         AND total_amount >= 0
                                                     )
);


/*
 * ============================================================
 * INDEXES
 * ============================================================
 */

CREATE INDEX IF NOT EXISTS idx_invoice_company_status
    ON invoices (company_id, status);

CREATE INDEX IF NOT EXISTS idx_invoice_worker_period
    ON invoices (
                 worker_profile_id,
                 period_start,
                 period_end
        );

CREATE INDEX IF NOT EXISTS idx_invoice_issue_date
    ON invoices (issue_date);

CREATE INDEX IF NOT EXISTS idx_invoice_item_invoice
    ON invoice_items (invoice_id);


/*
 * ============================================================
 * OPTIONAL QUERY INDEXES
 * ============================================================
 */

CREATE INDEX IF NOT EXISTS idx_invoice_company_period
    ON invoices (
                 company_id,
                 period_start,
                 period_end
        );

CREATE INDEX IF NOT EXISTS idx_invoice_worker_status
    ON invoices (
                 worker_profile_id,
                 status
        );