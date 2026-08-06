ALTER TABLE app_users
    ADD COLUMN IF NOT EXISTS system_role varchar(30);

UPDATE app_users
SET system_role = 'USER'
WHERE system_role IS NULL;

ALTER TABLE app_users
    ALTER COLUMN system_role SET DEFAULT 'USER',
    ALTER COLUMN system_role SET NOT NULL;

ALTER TABLE app_users
    ADD CONSTRAINT ck_app_user_system_role
        CHECK (system_role IN ('PLATFORM_ADMIN', 'USER'));
