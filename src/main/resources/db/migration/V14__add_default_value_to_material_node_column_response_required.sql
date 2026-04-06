ALTER TABLE material_node ALTER COLUMN response_required SET DEFAULT FALSE::boolean;
ALTER TABLE material_node ALTER COLUMN scoring_mode SET DEFAULT 'NONE';
ALTER TABLE material_node ALTER COLUMN version SET DEFAULT 0;