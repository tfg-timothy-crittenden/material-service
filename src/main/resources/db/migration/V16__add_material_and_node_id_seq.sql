DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'material_id_seq'
    ) THEN
        CREATE SEQUENCE material_id_seq;
    END IF;
END $$;

-- Use 1 as the minimum value if table is empty
SELECT setval('material_id_seq', COALESCE(NULLIF((SELECT MAX(id) FROM material), NULL), 1), true);

ALTER TABLE material
    ALTER COLUMN id SET DEFAULT nextval('material_id_seq');

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.sequences WHERE sequence_name = 'material_node_id_seq'
    ) THEN
        CREATE SEQUENCE material_node_id_seq;
    END IF;
END $$;

-- Use 1 as the minimum value if table is empty
SELECT setval('material_node_id_seq', COALESCE(NULLIF((SELECT MAX(id) FROM material_node), NULL), 1), true);

ALTER TABLE material_node
    ALTER COLUMN id SET DEFAULT nextval('material_node_id_seq');
