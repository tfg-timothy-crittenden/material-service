-- Migration to add autogenerating id to material_asset
DO $$
    BEGIN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'material_asset_id_seq'
        ) THEN
            CREATE SEQUENCE material_asset_id_seq;
        END IF;
    END $$;

ALTER TABLE material_asset
    ALTER COLUMN id SET DEFAULT nextval('material_asset_id_seq');
ALTER TABLE material_asset ALTER COLUMN display_order SET DEFAULT 0;

ALTER TABLE material_asset
    ALTER COLUMN id SET NOT NULL;
