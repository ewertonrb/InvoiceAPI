/*
 * ============================================================
 * CONTRACTOR INVOICE GST CONFIGURATION
 *
 * Adds:
 * - GST configuration for contractor invoices per company
 * - GST applied flag in WorkLog financial snapshots
 * ============================================================
 */


/*
 * ============================================================
 * COMPANY GST SETTING
 * ============================================================
 */

ALTER TABLE companies
    ADD COLUMN IF NOT EXISTS
        contractor_invoice_gst_enabled BOOLEAN;

UPDATE companies
SET contractor_invoice_gst_enabled = FALSE
WHERE contractor_invoice_gst_enabled IS NULL;

ALTER TABLE companies
    ALTER COLUMN contractor_invoice_gst_enabled
        SET DEFAULT FALSE;

ALTER TABLE companies
    ALTER COLUMN contractor_invoice_gst_enabled
        SET NOT NULL;


/*
 * ============================================================
 * WORK LOG SNAPSHOT GST FLAG
 * ============================================================
 */

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS
        snapshot_gst_applied BOOLEAN;

/*
 * Preserve the meaning of existing financial snapshots.
 *
 * When an existing snapshot has a GST amount greater than zero,
 * GST was applied.
 *
 * Pending WorkLogs without a snapshot may remain NULL.
 */

UPDATE work_logs
SET snapshot_gst_applied =
        CASE
            WHEN snapshot_gst_amount IS NOT NULL
                AND snapshot_gst_amount > 0
                THEN TRUE

            WHEN snapshot_gst_amount IS NOT NULL
                THEN FALSE

            ELSE NULL
            END;