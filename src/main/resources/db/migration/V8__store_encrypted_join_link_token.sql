ALTER TABLE company_join_links ADD COLUMN IF NOT EXISTS encrypted_token character varying(512);
