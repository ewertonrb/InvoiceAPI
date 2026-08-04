/*
 * ============================================================
 * WORK LOG REFACTOR
 *
 * Adds:
 * - WorkLogTime fields
 * - Manager notes
 * - Financial snapshot fields
 * - Workflow index
 *
 * Removes:
 * - Legacy active column
 *
 * Existing travel columns are preserved because WorkLogTravel
 * maps to the same columns through @Embedded.
 * ============================================================
 */


/*
 * ============================================================
 * WORK LOG TIME
 * ============================================================
 */

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS start_time TIME;

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS finish_time TIME;

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS unpaid_break_minutes INTEGER;


/*
 * Existing records must receive a valid value before the
 * NOT NULL constraint is added.
 */

UPDATE work_logs
SET unpaid_break_minutes = 0
WHERE unpaid_break_minutes IS NULL;

ALTER TABLE work_logs
    ALTER COLUMN unpaid_break_minutes SET DEFAULT 0;

ALTER TABLE work_logs
    ALTER COLUMN unpaid_break_minutes SET NOT NULL;


/*
 * Prevent negative unpaid break values.
 */

ALTER TABLE work_logs
DROP CONSTRAINT IF EXISTS chk_work_logs_unpaid_break_minutes;

ALTER TABLE work_logs
    ADD CONSTRAINT chk_work_logs_unpaid_break_minutes
        CHECK (unpaid_break_minutes >= 0);


/*
 * ============================================================
 * MANAGER NOTES
 * ============================================================
 */

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS manager_notes VARCHAR(1000);


/*
 * ============================================================
 * FINANCIAL TEXT SNAPSHOT
 * ============================================================
 */

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_company_name VARCHAR(150);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_project_name VARCHAR(150);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_position_name VARCHAR(150);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_worker_name VARCHAR(200);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_worker_abn VARCHAR(11);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_worker_gst_registered BOOLEAN;


/*
 * ============================================================
 * FINANCIAL RATE SNAPSHOT
 * ============================================================
 */

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_regular_rate NUMERIC(12, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_overtime_15_rate NUMERIC(12, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_overtime_20_rate NUMERIC(12, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_saturday_rate NUMERIC(12, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_sunday_rate NUMERIC(12, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_public_holiday_rate NUMERIC(12, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_travel_rate NUMERIC(12, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_kilometre_rate NUMERIC(12, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_lafha_rate NUMERIC(12, 2);


/*
 * ============================================================
 * CALCULATED FINANCIAL AMOUNTS
 * ============================================================
 */

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_regular_amount NUMERIC(14, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_overtime_15_amount NUMERIC(14, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_overtime_20_amount NUMERIC(14, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_saturday_amount NUMERIC(14, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_sunday_amount NUMERIC(14, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_public_holiday_amount NUMERIC(14, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_travel_amount NUMERIC(14, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_kilometre_amount NUMERIC(14, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_lafha_amount NUMERIC(14, 2);


/*
 * ============================================================
 * SNAPSHOT TOTALS
 * ============================================================
 */

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_subtotal_amount NUMERIC(14, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_gst_amount NUMERIC(14, 2);

ALTER TABLE work_logs
    ADD COLUMN IF NOT EXISTS snapshot_total_amount NUMERIC(14, 2);


/*
 * ============================================================
 * STATUS DATA NORMALIZATION
 * ============================================================
 */

/*
 * Protect against legacy rows without a workflow status.
 */

UPDATE work_logs
SET status = 'PENDING_APPROVAL'
WHERE status IS NULL;

ALTER TABLE work_logs
    ALTER COLUMN status SET NOT NULL;


/*
 * ============================================================
 * REMOVE LEGACY ACTIVE COLUMN
 * ============================================================
 */

/*
 * WorkLogStatus now represents the complete lifecycle:
 *
 * PENDING_APPROVAL
 * APPROVED
 * REJECTED
 * INVOICED
 * CANCELLED
 */

ALTER TABLE work_logs
DROP COLUMN IF EXISTS active;


/*
 * ============================================================
 * INDEXES
 * ============================================================
 */

CREATE INDEX IF NOT EXISTS idx_work_log_status
    ON work_logs (status);

CREATE INDEX IF NOT EXISTS idx_work_log_worker_status
    ON work_logs (worker_profile_id, status);

CREATE INDEX IF NOT EXISTS idx_work_log_work_date
    ON work_logs (work_date);

CREATE INDEX IF NOT EXISTS idx_work_log_worker_date
    ON work_logs (worker_profile_id, work_date);

CREATE INDEX IF NOT EXISTS idx_work_log_position_date
    ON work_logs (project_position_id, work_date);