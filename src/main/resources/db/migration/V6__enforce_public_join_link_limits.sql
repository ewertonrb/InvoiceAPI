-- Public join links must always have a finite usage limit and expiry.
-- Disable malformed legacy links before backfilling so this migration never
-- makes an old unlimited link usable again.
UPDATE public.company_join_links
SET status = 'DISABLED',
    disabled_at = COALESCE(disabled_at, CURRENT_TIMESTAMP),
    updated_at = CURRENT_TIMESTAMP
WHERE max_uses IS NULL
   OR max_uses < 1
   OR expires_at IS NULL
   OR current_uses < 0
   OR current_uses > max_uses;

UPDATE public.company_join_links
SET current_uses = GREATEST(COALESCE(current_uses, 0), 0);

UPDATE public.company_join_links
SET max_uses = GREATEST(COALESCE(max_uses, 1), current_uses, 1);

UPDATE public.company_join_links
SET expires_at = COALESCE(
        expires_at,
        disabled_at,
        updated_at,
        created_at,
        CURRENT_TIMESTAMP
    );

ALTER TABLE public.company_join_links
    ALTER COLUMN max_uses SET NOT NULL,
    ALTER COLUMN expires_at SET NOT NULL;

ALTER TABLE public.company_join_links
    ADD CONSTRAINT ck_join_link_max_uses_positive
        CHECK (max_uses >= 1),
    ADD CONSTRAINT ck_join_link_current_uses_non_negative
        CHECK (current_uses >= 0),
    ADD CONSTRAINT ck_join_link_current_uses_within_limit
        CHECK (current_uses <= max_uses);
