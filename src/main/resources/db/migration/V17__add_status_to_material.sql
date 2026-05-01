-- Add publication status for draft/publish workflow.
ALTER TABLE material
    ADD COLUMN IF NOT EXISTS status VARCHAR(20);

-- Existing rows represent already-usable materials.
UPDATE material
SET status = 'PUBLISHED'
WHERE status IS NULL;

-- Enforce domain constraints at DB level.
ALTER TABLE material
    ALTER COLUMN status SET NOT NULL;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'chk_material_status'
    ) THEN
        ALTER TABLE material
            ADD CONSTRAINT chk_material_status
            CHECK (status IN ('DRAFT', 'PUBLISHED'));
    END IF;
END $$;

