CREATE UNIQUE INDEX IF NOT EXISTS uk_work_log_active_worker_position_date
    ON work_logs (worker_profile_id, project_position_id, work_date)
    WHERE status <> 'CANCELLED';
