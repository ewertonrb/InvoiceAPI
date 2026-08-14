/*
 * WorkLogs are no longer unique by worker, position, and date alone.
 * The application validates overlapping time intervals instead, so a
 * worker may submit multiple non-overlapping WorkLogs on the same day.
 */

ALTER TABLE work_logs
    DROP CONSTRAINT IF EXISTS uk_work_log_active_worker_position_date;

DROP INDEX IF EXISTS uk_work_log_active_worker_position_date;

CREATE INDEX IF NOT EXISTS idx_work_log_worker_position_date
    ON work_logs (worker_profile_id, project_position_id, work_date);
