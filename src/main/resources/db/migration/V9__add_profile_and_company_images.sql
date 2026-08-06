ALTER TABLE app_users ADD COLUMN IF NOT EXISTS avatar_data bytea;
ALTER TABLE app_users ADD COLUMN IF NOT EXISTS avatar_content_type varchar(80);
ALTER TABLE companies ADD COLUMN IF NOT EXISTS logo_data bytea;
ALTER TABLE companies ADD COLUMN IF NOT EXISTS logo_content_type varchar(80);
